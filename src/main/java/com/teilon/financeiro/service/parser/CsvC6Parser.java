package com.teilon.financeiro.service.parser;

import com.teilon.financeiro.model.TipoTransacao;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser para extrato CSV do C6 Bank.
 *
 * O arquivo exportado pelo C6 contém:
 *   - Várias linhas de cabeçalho/metadados (agência, conta, data de geração…)
 *   - Uma linha em branco
 *   - Linha de colunas CSV (separador vírgula):
 *       Data Lançamento,Data Transação,Descrição,Tipo,Valor,Saldo
 *   - Linhas de dados no formato:
 *       23/03/2026,23/03/2026,PIX RECEBIDO,Crédito,1200.50,3500.00
 *
 * Tipo: "Crédito" → RECEITA, "Débito" → DESPESA.
 * Valor: ponto como separador decimal, sempre positivo.
 * Data: dd/MM/yyyy.
 */
@Component
public class CsvC6Parser {

    private static final List<DateTimeFormatter> DATE_FMTS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    );

    public List<TransacaoImportada> parse(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        Charset charset = detectarCharset(bytes);
        String conteudo = new String(bytes, charset);
        // Remove BOM UTF-8 se presente
        if (conteudo.startsWith("\uFEFF")) conteudo = conteudo.substring(1);

        List<TransacaoImportada> transacoes = new ArrayList<>();
        String[] linhas = conteudo.split("\\r?\\n");

        // Detecta separador: conta vírgulas vs ponto-e-vírgulas na linha de cabeçalho
        char sep = detectarSeparador(linhas);

        boolean cabecalhoEncontrado = false;
        int colData = -1, colDescricao = -1, colTipo = -1, colValor = -1;
        int colEntrada = -1, colSaida = -1;

        for (String linha : linhas) {
            String linhaStrip = linha.trim();
            if (linhaStrip.isEmpty()) continue;

            String[] cols = splitCsv(linhaStrip, sep);

            if (!cabecalhoEncontrado) {
                // Procura linha de cabeçalho: deve conter "data" E ("valor" OU "descri" OU "entrada")
                boolean temData = false, temValor = false;
                for (String col : cols) {
                    String c = normalizar(col.trim());
                    if (c.contains("data")) temData = true;
                    if (c.contains("valor") || c.contains("descri") || c.contains("entrada")) temValor = true;
                }
                if (!temData || !temValor) continue;

                // Mapeia índices de colunas
                for (int i = 0; i < cols.length; i++) {
                    String c = normalizar(cols[i].trim());
                    if (c.contains("lancamento") || (c.contains("data") && colData < 0)) colData = i;
                    if (c.contains("descri")) colDescricao = i;
                    if (c.equals("tipo") || c.contains("operac")) colTipo = i;
                    if (c.equals("valor")) colValor = i;
                    // Novo formato C6: Entrada(R$) e Saida(R$)
                    if (c.startsWith("entrada")) colEntrada = i;
                    if (c.startsWith("saida")) colSaida = i;
                }
                boolean formatoNovo = colEntrada >= 0 && colSaida >= 0;
                if (colData >= 0 && (colValor >= 0 || formatoNovo)) cabecalhoEncontrado = true;
                continue;
            }

            if (cols.length <= colData) continue;

            String dataStr = cols[colData].trim();
            if (dataStr.isEmpty()) continue;

            LocalDate data = parsearData(dataStr);
            if (data == null) continue;

            TipoTransacao tipo;
            BigDecimal valor;

            if (colEntrada >= 0 && colSaida >= 0) {
                // Novo formato: colunas separadas Entrada(R$) / Saída(R$)
                String entradaStr = colEntrada < cols.length ? cols[colEntrada].trim() : "0";
                String saidaStr = colSaida < cols.length ? cols[colSaida].trim() : "0";
                BigDecimal entrada = parsearValorSeguro(entradaStr);
                BigDecimal saida = parsearValorSeguro(saidaStr);
                if (entrada.compareTo(BigDecimal.ZERO) > 0) {
                    tipo = TipoTransacao.RECEITA;
                    valor = entrada;
                } else if (saida.compareTo(BigDecimal.ZERO) > 0) {
                    tipo = TipoTransacao.DESPESA;
                    valor = saida;
                } else {
                    continue; // linha sem movimento
                }
            } else {
                // Formato antigo: coluna Valor + Tipo
                if (colValor < 0 || colValor >= cols.length) continue;
                String valorStr = cols[colValor].trim().replace("\"", "");
                if (valorStr.isEmpty()) continue;
                try {
                    BigDecimal valorBruto = parsearValor(valorStr);
                    if (colTipo >= 0 && colTipo < cols.length) {
                        String tipoStr = cols[colTipo].trim().toLowerCase();
                        tipo = tipoStr.contains("cr") || tipoStr.contains("recei") || tipoStr.contains("entrada")
                                ? TipoTransacao.RECEITA : TipoTransacao.DESPESA;
                    } else {
                        tipo = valorBruto.compareTo(BigDecimal.ZERO) >= 0
                                ? TipoTransacao.RECEITA : TipoTransacao.DESPESA;
                    }
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
        return s.toLowerCase()
                .replace("ã", "a").replace("ç", "c").replace("é", "e")
                .replace("â", "a").replace("ê", "e").replace("ó", "o")
                .replace("í", "i").replace("ú", "u").replace("á", "a")
                .replaceAll("\\(.*?\\)", "").trim(); // remove (R$) etc
    }

    private BigDecimal parsearValorSeguro(String s) {
        try { return parsearValor(s); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private char detectarSeparador(String[] linhas) {
        for (String linha : linhas) {
            if (linha.toLowerCase().contains("data") && (linha.contains("valor") || linha.contains("descri"))) {
                long virgulas = linha.chars().filter(c -> c == ',').count();
                long pontoVirgulas = linha.chars().filter(c -> c == ';').count();
                return pontoVirgulas > virgulas ? ';' : ',';
            }
        }
        return ','; // C6 padrão
    }

    private String[] splitCsv(String linha, char sep) {
        // Split simples respeitando aspas
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

    private BigDecimal parsearValor(String s) {
        // Remove espaços, R$, aspas
        s = s.replaceAll("[\\s\"R$]", "").replace("R$", "");
        // Se tem vírgula E ponto: formato brasileiro "1.234,56"
        if (s.contains(",") && s.contains(".")) {
            s = s.replace(".", "").replace(",", ".");
        } else if (s.contains(",") && !s.contains(".")) {
            // Só vírgula: decimal brasileiro "1234,56"
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
