package com.teilon.financeiro.controller;

import com.teilon.financeiro.dto.*;
import com.teilon.financeiro.dto.PrevisaoResponse;
import com.teilon.financeiro.model.TipoTransacao;
import com.teilon.financeiro.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Análises e evolução financeira")
@SecurityRequirement(name = "bearerAuth")
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/evolucao")
    @Operation(summary = "Evolução mensal do ano",
               description = "Retorna receitas, despesas e saldo de cada mês do ano informado.")
    public ResponseEntity<EvolucaoAnualResponse> evolucao(@RequestParam int ano) {
        return ResponseEntity.ok(relatorioService.evolucaoAnual(ano));
    }

    @GetMapping("/top-categorias")
    @Operation(summary = "Top categorias por gasto ou receita",
               description = "Retorna as categorias com maior movimentação no período, ordenadas por valor. " +
                             "Parâmetro tipo: DESPESA ou RECEITA.")
    public ResponseEntity<List<TopCategoriaResponse>> topCategorias(
            @RequestParam int mes,
            @RequestParam int ano,
            @RequestParam(defaultValue = "DESPESA") TipoTransacao tipo) {
        return ResponseEntity.ok(relatorioService.topCategorias(mes, ano, tipo));
    }

    @GetMapping("/previsao")
    @Operation(summary = "Previsão do mês", description = "Projeta receitas e despesas até o fim do mês com base no que já foi lançado.")
    public ResponseEntity<PrevisaoResponse> previsao(@RequestParam int mes, @RequestParam int ano) {
        return ResponseEntity.ok(relatorioService.previsao(mes, ano));
    }

    @GetMapping("/comparativo")
    @Operation(summary = "Comparativo entre dois períodos",
               description = "Compara receitas, despesas e saldo entre dois meses. " +
                             "A variação percentual é null quando o período anterior tem valor zero.")
    public ResponseEntity<ComparativoResponse> comparativo(
            @RequestParam int mesAtual,
            @RequestParam int anoAtual,
            @RequestParam int mesAnterior,
            @RequestParam int anoAnterior) {
        return ResponseEntity.ok(relatorioService.comparativo(mesAtual, anoAtual, mesAnterior, anoAnterior));
    }

    @GetMapping("/exportar.csv")
    @Operation(summary = "Exportar relatório detalhado em CSV")
    public ResponseEntity<byte[]> exportarRelatorio(
            @RequestParam int mes,
            @RequestParam int ano) {
        byte[] csv = relatorioService.exportarRelatorioCsv(mes, ano);
        String filename = "relatorio_" + ano + "_" + String.format("%02d", mes) + ".csv";
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(csv);
    }
}
