package com.teilon.financeiro.service.parser;

import com.teilon.financeiro.model.TipoTransacao;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser para extrato CSV do Bradesco.
 *
 * Formato típico exportado pelo Bradesco (separador ponto-e-vírgula):
 *   Data;Lançamento;Débito (R$);Crédito (R$);Saldo (R$)
 *   02/01/2026;PIX RECEBIDO - PESSOA;;+100,00;+1000,00
 *   03/01/2026;TED/DOC;50,00;;+950,00
 *
 * Crédito → RECEITA, Débito → DESPESA.
 * Valores podem vir com + ou - na frente; sempre absolutos por coluna.
 * Encoding: ISO-8859-1 ou UTF-8.
 */
@Component
public class CsvBradescoParser {

    private static final List<DateTimeFormatter> DATE_FMTS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    );

    public List<TransacaoImportada> parse(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        Charset charset = detectarCharset(bytes);
        String conteudo = new String(bytes, charset);
        if (conteudo.startsWith("﻿")) conteudo = conteudo.substring(1);

        List<TransacaoImportada> transacoes = new ArrayList<>();
        String[] linhas = conteudo.split("\\r?\\n");

        char sep = detectarSeparador(linhas);

        boolean cabecalhoEncontrado = false;
        int colData = -1, colDescricao = -1;
        int colDebito = -1, colCredito = -1;
        int colValor = -1;

        for (String linha : linhas) {
            String linhaStrip = linha.trim();
            if (linhaStrip.isEmpty()) continue;

            String[] cols = splitCsv(linhaStrip, sep);

            if (!cabecalhoEncontrado) {
                boolean temData = false, temValor = false;
                for (String col : cols) {
                    String c = normalizar(col);
                    if (c.contains("data")) temData = true;
                    if (c.contains("debito") || c.contains("credito") || c.contains("valor")
                            || c.contains("lancamento") || c.contains("hist")) temValor = true;
                }
                if (!temData || !temValor) continue;

                for (int i = 0; i < cols.length; i++) {
                    String c = normalizar(cols[i]);
                    if (c.contains("data") && colData < 0) colData = i;
                    if (c.contains("lancamento") || c.contains("historico") || c.contains("descri"))
                        colDescricao = i;
                    if (c.startsWith("debito")) colDebito = i;
                    if (c.startsWith("credito")) colCredito = i;
                    if (c.equals("valor")) colValor = i;
                }

                boolean formatoColunas = colCredito >= 0 && colDebito >= 0;
                if (colData >= 0 && (formatoColunas || colValor >= 0)) cabecalhoEncontrado = true;
                continue;
            }

            if (cols.length <= colData) continue;
            String dataStr = cols[colData].trim().replace("\"", "");
            if (dataStr.isEmpty()) continue;

            LocalDate data = parsearData(dataStr);
            if (data == null) continue;

            TipoTransacao tipo;
            BigDecimal valor;

            if (colCredito >= 0 && colDebito >= 0) {
                String creditoStr = colCredito < cols.length ? cols[colCredito].trim().replace("\"", "") : "";
                String debitoStr = colDebito < cols.length ? cols[colDebito].trim().replace("\"", "") : "";
                BigDecimal credito = parsearValorSeguro(creditoStr);
                BigDecimal debito = parsearValorSeguro(debitoStr);
                if (credito.compareTo(BigDecimal.ZERO) > 0) {
                    tipo = TipoTransacao.RECEITA;
                    valor = credito;
                } else if (debito.compareTo(BigDecimal.ZERO) > 0) {
                    tipo = TipoTransacao.DESPESA;
                    valor = debito;
                } else {
                    continue;
                }
            } else {
                if (colValor < 0 || colValor >= cols.length) continue;
                String valorStr = cols[colValor].trim().replace("\"", "");
                if (valorStr.isEmpty()) continue;
                try {
                    BigDecimal valorBruto = parsearValorBR(valorStr);
                    tipo = valorBruto.compareTo(BigDecimal.ZERO) >= 0
                            ? TipoTransacao.RECEITA : TipoTransacao.DESPESA;
                    valor = valorBruto.abs();
                } catch (NumberFormatException e) {
                    continue;
                }
            }

            String descricao = (colDescricao >= 0 && colDescricao < cols.length)
                    ? cols[colDescricao].trim().replace("\"", "")
                    : "Transação importada";
            if (descricao.isEmpty()) descricao = "Transação importada";

            transacoes.add(new TransacaoImportada(descricao, valor, data, tipo));
        }

        return transacoes;
    }

    private String normalizar(String s) {
        return s.trim().toLowerCase()
                .replace("ã", "a").replace("â", "a").replace("á", "a").replace("à", "a")
                .replace("ç", "c").replace("é", "e").replace("ê", "e")
                .replace("í", "i").replace("ó", "o").replace("ô", "o").replace("ú", "u")
                .replaceAll("\\(.*?\\)", "").trim();
    }

    private char detectarSeparador(String[] linhas) {
        for (String linha : linhas) {
            String l = normalizar(linha);
            if (l.contains("data") && (l.contains("debito") || l.contains("credito") || l.contains("lancamento"))) {
                long sc = linha.chars().filter(c -> c == ';').count();
                long cc = linha.chars().filter(c -> c == ',').count();
                return sc >= cc ? ';' : ',';
            }
        }
        return ';';
    }

    private String[] splitCsv(String linha, char sep) {
        List<String> cols = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuote = false;
        for (char c : linha.toCharArray()) {
            if (c == '"') { inQuote = !inQuote; }
            else if (c == sep && !inQuote) { cols.add(sb.toString()); sb = new StringBuilder(); }
            else sb.append(c);
        }
        cols.add(sb.toString());
        return cols.toArray(new String[0]);
    }

    private LocalDate parsearData(String s) {
        for (DateTimeFormatter fmt : DATE_FMTS) {
            try { return LocalDate.parse(s, fmt); } catch (Exception ignored) {}
        }
        return null;
    }

    private BigDecimal parsearValorBR(String s) {
        s = s.replaceAll("[\\s\"R$+]", "").replace("R$", "");
        if (s.contains(",") && s.contains(".")) s = s.replace(".", "").replace(",", ".");
        else if (s.contains(",")) s = s.replace(",", ".");
        return new BigDecimal(s);
    }

    private BigDecimal parsearValorSeguro(String s) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        try { return parsearValorBR(s).abs(); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private Charset detectarCharset(byte[] bytes) {
        try {
            StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                    .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
                    .decode(java.nio.ByteBuffer.wrap(bytes));
            return StandardCharsets.UTF_8;
        } catch (Exception e) {
            return StandardCharsets.ISO_8859_1;
        }
    }
}
