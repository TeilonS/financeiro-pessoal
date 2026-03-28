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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser para arquivos OFX 1.x (SGML) e 2.x (XML).
 * Extrai transações dos blocos <STMTTRN>.
 */
@Component
public class OfxParser {

    private static final Pattern BLOCK_PATTERN =
            Pattern.compile("<STMTTRN>(.+?)</STMTTRN>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static final Pattern TAG_PATTERN =
            Pattern.compile("<(\\w+)>([^<\\r\\n]+)");

    public List<TransacaoImportada> parse(MultipartFile file) throws IOException {
        String conteudo = lerConteudo(file);
        List<TransacaoImportada> transacoes = new ArrayList<>();

        Matcher blocos = BLOCK_PATTERN.matcher(conteudo);
        while (blocos.find()) {
            String bloco = blocos.group(1);
            TransacaoImportada t = parsearBloco(bloco);
            if (t != null) transacoes.add(t);
        }

        return transacoes;
    }

    private TransacaoImportada parsearBloco(String bloco) {
        String trnType = extrairTag(bloco, "TRNTYPE");
        String dtPosted = extrairTag(bloco, "DTPOSTED");
        String trnAmt = extrairTag(bloco, "TRNAMT");
        String memo = extrairTag(bloco, "MEMO");

        if (trnAmt == null || dtPosted == null) return null;

        BigDecimal valorBruto;
        try {
            valorBruto = new BigDecimal(trnAmt.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }

        LocalDate data = parsearData(dtPosted.trim());
        if (data == null) return null;

        // Valor absoluto — tipo derivado do sinal ou do TRNTYPE
        BigDecimal valor = valorBruto.abs();
        TipoTransacao tipo;
        if (valorBruto.compareTo(BigDecimal.ZERO) >= 0) {
            tipo = TipoTransacao.RECEITA;
        } else {
            tipo = TipoTransacao.DESPESA;
        }

        // TRNTYPE pode sobrescrever quando valor é zero
        if (trnType != null) {
            String t = trnType.trim().toUpperCase();
            if (t.equals("CREDIT") || t.equals("INT") || t.equals("DIV")) {
                tipo = TipoTransacao.RECEITA;
            } else if (t.equals("DEBIT") || t.equals("CHECK") || t.equals("FEE") || t.equals("SRVCHG")) {
                tipo = TipoTransacao.DESPESA;
            }
        }

        String descricao = memo != null ? memo.trim() : "Transação importada";

        return new TransacaoImportada(descricao, valor, data, tipo);
    }

    private String extrairTag(String bloco, String tag) {
        Pattern p = Pattern.compile("<" + tag + ">([^<\\r\\n]+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(bloco);
        return m.find() ? m.group(1) : null;
    }

    private LocalDate parsearData(String dtPosted) {
        // Pega apenas os primeiros 8 chars: YYYYMMDD
        if (dtPosted.length() < 8) return null;
        String dataStr = dtPosted.substring(0, 8);
        try {
            return LocalDate.parse(dataStr, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (Exception e) {
            return null;
        }
    }

    private String lerConteudo(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        // Tenta detectar encoding pelo header OFX
        String header = new String(bytes, 0, Math.min(200, bytes.length), StandardCharsets.ISO_8859_1);
        Charset charset = StandardCharsets.UTF_8;
        if (header.contains("CHARSET:1252") || header.contains("CHARSET:850")) {
            charset = Charset.forName("Windows-1252");
        } else if (header.contains("CHARSET:ISO-8859")) {
            charset = StandardCharsets.ISO_8859_1;
        }
        return new String(bytes, charset);
    }
}
