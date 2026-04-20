package com.teilon.financeiro.service.parser;

import com.teilon.financeiro.model.TipoTransacao;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser para extrato CSV do Itaú.
 * Formato esperado: data;lançamento;valor;
 * 15/03/2026;COMPRA CARTAO;-50,00
 */
@Component
public class CsvItauParser {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public List<TransacaoImportada> parse(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        String conteudo = new String(bytes, StandardCharsets.ISO_8859_1);
        List<TransacaoImportada> transacoes = new ArrayList<>();
        String[] linhas = conteudo.split("\\r?\\n");

        for (String linha : linhas) {
            if (linha.trim().isEmpty()) continue;
            String[] cols = linha.split(";");

            if (cols.length < 3) continue;

            try {
                LocalDate data = LocalDate.parse(cols[0].trim(), DATE_FMT);
                String valorStr = cols[2].trim().replace(".", "").replace(",", ".");
                BigDecimal valorBruto = new BigDecimal(valorStr);
                String descricao = cols[1].trim().replace("\"", "");

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
