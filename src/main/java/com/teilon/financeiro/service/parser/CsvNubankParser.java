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
 * Parser para extrato CSV do Nubank.
 * Formato esperado: Data,Valor,Identificador,Descrição
 * Aceita datas em yyyy-MM-dd (formato antigo) ou dd/MM/yyyy (formato atual).
 */
@Component
public class CsvNubankParser {

    private static final DateTimeFormatter DATE_FMT_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_FMT_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public List<TransacaoImportada> parse(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        Charset charset = detectarCharset(bytes);
        String conteudo = new String(bytes, charset);
        if (conteudo.startsWith("﻿")) conteudo = conteudo.substring(1);

        List<TransacaoImportada> transacoes = new ArrayList<>();
        String[] linhas = conteudo.split("\\r?\\n");

        boolean cabecalhoEncontrado = false;

        for (String linha : linhas) {
            if (linha.trim().isEmpty()) continue;
            String[] cols = splitCsv(linha, ',');

            if (!cabecalhoEncontrado) {
                if (linha.toLowerCase().contains("data") && linha.toLowerCase().contains("valor")) {
                    cabecalhoEncontrado = true;
                }
                continue;
            }

            if (cols.length < 4) continue;

            try {
                LocalDate data = parseData(cols[0].trim());
                BigDecimal valorBruto = new BigDecimal(cols[1].trim());
                String descricao = cols[3].trim().replace("\"", "");

                TipoTransacao tipo = valorBruto.compareTo(BigDecimal.ZERO) >= 0
                        ? TipoTransacao.RECEITA : TipoTransacao.DESPESA;

                transacoes.add(new TransacaoImportada(descricao, valorBruto.abs(), data, tipo));
            } catch (Exception e) {
                continue;
            }
        }
        return transacoes;
    }

    private LocalDate parseData(String texto) {
        if (texto.contains("/")) {
            return LocalDate.parse(texto, DATE_FMT_BR);
        }
        return LocalDate.parse(texto, DATE_FMT_ISO);
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
