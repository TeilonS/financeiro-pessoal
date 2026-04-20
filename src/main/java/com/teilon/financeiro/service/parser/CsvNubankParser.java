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
 * 2026-03-15,-50.00,id,Compra no Mercado
 */
@Component
public class CsvNubankParser {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<TransacaoImportada> parse(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        String conteudo = new String(bytes, StandardCharsets.UTF_8);
        List<TransacaoImportada> transacoes = new ArrayList<>();
        String[] linhas = conteudo.split("\\r?\\n");

        boolean cabecalhoEncontrado = false;

        for (String linha : linhas) {
            if (linha.trim().isEmpty()) continue;
            String[] cols = linha.split(",");

            if (!cabecalhoEncontrado) {
                if (linha.toLowerCase().contains("data") && linha.toLowerCase().contains("valor")) {
                    cabecalhoEncontrado = true;
                }
                continue;
            }

            if (cols.length < 4) continue;

            try {
                LocalDate data = LocalDate.parse(cols[0].trim(), DATE_FMT);
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
}
