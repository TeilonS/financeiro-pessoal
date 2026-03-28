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
 * Parser para extrato CSV do Banco Inter.
 *
 * Formato real exportado pelo Inter (separador ponto-e-vírgula):
 *   Data Lançamento;Histórico;Descrição;Valor;Saldo
 *   01/03/2026;PIX Recebido;Fulano de Tal;100,00;1000,00
 *   02/03/2026;Compra no Débito;Mercado;-50,00;950,00
 *
 * O arquivo pode conter linhas de metadados antes do cabeçalho — são ignoradas.
 * Encoding: ISO-8859-1 ou UTF-8 (detectado automaticamente).
 */
@Component
public class CsvInterParser {

    private static final List<DateTimeFormatter> DATE_FMTS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    );

    public List<TransacaoImportada> parse(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        Charset charset = detectarCharset(bytes);
        String conteudo = new String(bytes, charset);
        if (conteudo.startsWith("\uFEFF")) conteudo = conteudo.substring(1);

        List<TransacaoImportada> transacoes = new ArrayList<>();
        String[] linhas = conteudo.split("\\r?\\n");

        char sep = detectarSeparador(linhas);

        boolean cabecalhoEncontrado = false;
        int colData = -1, colDescricao = -1, colValor = -1;

        for (String linha : linhas) {
            String linhaStrip = linha.trim();
            if (linhaStrip.isEmpty()) continue;

            String[] cols = splitCsv(linhaStrip, sep);

            if (!cabecalhoEncontrado) {
                boolean temData = false, temValor = false;
                for (String col : cols) {
                    String c = normalizar(col);
                    if (c.contains("data")) temData = true;
                    if (c.contains("valor") || c.contains("descri") || c.contains("hist")) temValor = true;
                }
                if (!temData || !temValor) continue;

                for (int i = 0; i < cols.length; i++) {
                    String c = normalizar(cols[i]);
                    if (c.contains("data") && colData < 0) colData = i;
                    if (c.contains("descri")) colDescricao = i;
                    else if (c.contains("hist") && colDescricao < 0) colDescricao = i;
                    if (c.equals("valor")) colValor = i;
                }
                if (colData >= 0 && colValor >= 0) cabecalhoEncontrado = true;
                continue;
            }

            if (cols.length <= Math.max(colData, colValor)) continue;

            String dataStr = cols[colData].trim();
            String valorStr = cols[colValor].trim().replace("\"", "");
            if (dataStr.isEmpty() || valorStr.isEmpty()) continue;

            LocalDate data = parsearData(dataStr);
            if (data == null) continue;

            BigDecimal valorBruto;
            try {
                valorBruto = parsearValorBR(valorStr);
            } catch (NumberFormatException e) {
                continue;
            }

            String descricao = (colDescricao >= 0 && colDescricao < cols.length)
                    ? cols[colDescricao].trim().replace("\"", "")
                    : "Transação importada";
            if (descricao.isEmpty()) descricao = "Transação importada";

            TipoTransacao tipo = valorBruto.compareTo(BigDecimal.ZERO) >= 0
                    ? TipoTransacao.RECEITA : TipoTransacao.DESPESA;

            transacoes.add(new TransacaoImportada(descricao, valorBruto.abs(), data, tipo));
        }

        return transacoes;
    }

    /** Remove acentos/cedilha para comparação de cabeçalhos. */
    private String normalizar(String s) {
        return s.trim().toLowerCase()
                .replace("ã", "a").replace("â", "a").replace("á", "a").replace("à", "a")
                .replace("ç", "c").replace("é", "e").replace("ê", "e")
                .replace("í", "i").replace("ó", "o").replace("ô", "o").replace("ú", "u");
    }

    private char detectarSeparador(String[] linhas) {
        for (String linha : linhas) {
            String l = normalizar(linha);
            if (l.contains("data") && (l.contains("valor") || l.contains("hist"))) {
                long sc = linha.chars().filter(c -> c == ';').count();
                long cc = linha.chars().filter(c -> c == ',').count();
                return sc >= cc ? ';' : ',';
            }
        }
        return ';'; // Inter padrão
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

    /** Valor no formato brasileiro: "-1.234,56" → BigDecimal(-1234.56) */
    private BigDecimal parsearValorBR(String s) {
        s = s.replaceAll("[\\s\"R$]", "").replace("R$", "");
        if (s.contains(",") && s.contains(".")) {
            s = s.replace(".", "").replace(",", ".");
        } else if (s.contains(",") && !s.contains(".")) {
            s = s.replace(",", ".");
        }
        return new BigDecimal(s);
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
