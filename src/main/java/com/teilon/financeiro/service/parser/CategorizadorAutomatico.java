package com.teilon.financeiro.service.parser;

import com.teilon.financeiro.model.Categoria;
import com.teilon.financeiro.model.RegraCategorizacao;
import com.teilon.financeiro.model.TipoTransacao;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sugere uma categoria com base em palavras-chave na descrição da transação.
 *
 * Regras especiais:
 *  - Transferências entre contas próprias (Inter ↔ C6, nome "teilon") → ignorar
 *  - Receita da Caixa Econômica Federal → Salário
 *  - PIX recebido de outras pessoas → Comissão
 */
@Component
public class CategorizadorAutomatico {

    // Palavras que identificam transferência entre contas próprias do usuário → ignorar
    private static final List<String> TRANSFERENCIAS_PROPRIAS = List.of(
        "teilon oliveira santos",
        "teilon santos",
        "c6 bank",        // PIX Inter → C6 aparece como destino "C6 BANK"
        "banco inter",    // PIX C6 → Inter aparece como destino "BANCO INTER"
        "transf enviada pix",  // C6: coluna Título quando é transferência interna
        "transferencia entre contas"
    );

    // Regras: palavras-chave na descrição → nomes candidatos de categoria (ordem de preferência)
    private static final List<Map.Entry<List<String>, List<String>>> REGRAS = List.of(

        // ── RECEITAS ────────────────────────────────────────────────────────────

        // Salário via Caixa Econômica Federal
        entry(List.of("caixa economica", "caixa federal", "cef ", "cef-", "c.e.f"),
              List.of("salario", "salário", "renda")),

        // Comissão / PIX de outras pessoas (regra de fallback para RECEITA — aplicada depois)
        entry(List.of("pix recebido de", "ted recebida", "doc recebido", "transferencia recebida"),
              List.of("comissao", "comissão", "receita", "renda")),

        // ── SAÚDE ───────────────────────────────────────────────────────────────

        entry(List.of("farmacia", "drogaria", "droga ", "remedios", "medicamento",
                      "ultrafarma", "pacheco", "nissei", "drogasil", "panvel"),
              List.of("farmacia", "saude", "saúde")),

        entry(List.of("medico", "consulta", "hospital", "clinica", "dentista",
                      "plano de saude", "unimed", "amil", "sulamerica", "hapvida"),
              List.of("saude", "saúde", "medico")),

        entry(List.of("academia", "smartfit", "smart fit", "crossfit", "bluefit", "bodytech"),
              List.of("academia", "saude", "saúde")),

        // ── ALIMENTAÇÃO ─────────────────────────────────────────────────────────

        entry(List.of("mercado", "supermercado", "hipermercado", "hortifruti",
                      "atacadao", "carrefour", "assai", "sonda", "condor", "walmart", "varejao"),
              List.of("mercado", "supermercado", "alimentacao", "alimentação")),

        entry(List.of("restaurante", "lanchonete", "pizzaria", "mcdonald", "burger", "subway",
                      "ifood", "rappi", "delivery", "habib", "bobs", "churrasco",
                      "pastelaria", "padaria", "cafe ", "sushi", "hamburguer"),
              List.of("restaurante", "alimentacao", "alimentação", "refeicao", "refeição")),

        // ── TRANSPORTE ──────────────────────────────────────────────────────────

        entry(List.of("posto ", "combustivel", "gasolina", "etanol", "diesel",
                      "shell", "ipiranga", "br distribuidora", "texaco", "petrobras"),
              List.of("combustivel", "combustível", "transporte", "posto")),

        entry(List.of("uber", "99pop", "cabify", "taxi", "taxista",
                      "onibus", "passagem", "bilhete unico", "metro "),
              List.of("transporte", "mobilidade")),

        // ── LAZER / ASSINATURAS ──────────────────────────────────────────────────

        entry(List.of("netflix", "spotify", "amazon prime", "disney", "hbo",
                      "paramount", "globoplay", "apple tv", "youtube premium", "deezer", "telecine"),
              List.of("assinaturas", "streaming", "lazer", "entretenimento")),

        // ── CONTAS / MORADIA ─────────────────────────────────────────────────────

        entry(List.of("tim ", "vivo ", "claro ", "nextel", "sky ", "internet ", "banda larga"),
              List.of("telefone", "internet", "contas", "moradia")),

        entry(List.of("energia", "coelba", "cemig", "cpfl", "enel", "ampla", "celpe", "eletropaulo"),
              List.of("energia", "luz", "contas", "moradia")),

        entry(List.of("saneago", "sabesp", "cedae", "embasa", "caema", "sanepar", "agua "),
              List.of("agua", "água", "contas", "moradia")),

        entry(List.of("aluguel", "condominio", "iptu", "imobiliaria"),
              List.of("moradia", "aluguel", "habitacao")),

        // ── EDUCAÇÃO ─────────────────────────────────────────────────────────────

        entry(List.of("escola", "faculdade", "universidade", "curso ", "mensalidade",
                      "colegio", "uniasselvi", "senai", "senac"),
              List.of("educacao", "educação", "ensino", "escola"))
    );

    /**
     * Retorna true se a transação é uma transferência entre contas próprias e deve ser ignorada.
     */
    public boolean deveIgnorar(String descricao) {
        if (descricao == null) return false;
        String desc = normalizar(descricao);
        return TRANSFERENCIAS_PROPRIAS.stream().anyMatch(desc::contains);
    }

    /**
     * Retorna a categoria sugerida verificando primeiro as regras do usuário,
     * depois as regras genéricas por palavras-chave.
     */
    public Optional<Categoria> sugerir(String descricao, TipoTransacao tipo,
                                       List<Categoria> categorias,
                                       List<RegraCategorizacao> regrasUsuario) {
        if (descricao == null) return Optional.empty();

        String desc = normalizar(descricao);

        // 1. Regras aprendidas pelo usuário (prioridade máxima)
        if (regrasUsuario != null) {
            for (RegraCategorizacao regra : regrasUsuario) {
                if (desc.contains(regra.getChave())) {
                    return Optional.of(regra.getCategoria());
                }
            }
        }

        if (categorias == null || categorias.isEmpty()) return Optional.empty();

        // 2. Regras genéricas por palavras-chave
        for (var regra : REGRAS) {
            List<String> keywords = regra.getKey();
            List<String> nomesCandidatos = regra.getValue();

            boolean bate = keywords.stream().anyMatch(desc::contains);
            if (!bate) continue;

            // Busca categoria do usuário que case com candidato e tipo
            for (String candidato : nomesCandidatos) {
                Optional<Categoria> cat = categorias.stream()
                        .filter(c -> c.getTipo() == tipo)
                        .filter(c -> normalizar(c.getNome()).contains(candidato))
                        .findFirst();
                if (cat.isPresent()) return cat;
            }

            // Fallback sem filtro de tipo
            for (String candidato : nomesCandidatos) {
                Optional<Categoria> cat = categorias.stream()
                        .filter(c -> normalizar(c.getNome()).contains(candidato))
                        .findFirst();
                if (cat.isPresent()) return cat;
            }
        }

        return Optional.empty();
    }

    private String normalizar(String s) {
        return s.toLowerCase()
                .replace("ã", "a").replace("â", "a").replace("á", "a").replace("à", "a")
                .replace("ç", "c").replace("é", "e").replace("ê", "e").replace("è", "e")
                .replace("í", "i").replace("ó", "o").replace("ô", "o").replace("ú", "u")
                .replace("ü", "u");
    }

    private static <K, V> Map.Entry<K, V> entry(K k, V v) {
        return Map.entry(k, v);
    }
}
