package com.teilon.financeiro.controller;

import com.teilon.financeiro.dto.LancamentoRequest;
import com.teilon.financeiro.dto.LancamentoResponse;
import com.teilon.financeiro.dto.ResumoResponse;
import com.teilon.financeiro.model.TipoTransacao;
import com.teilon.financeiro.service.LancamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Lançamentos")
public class LancamentoController {

    private final LancamentoService lancamentoService;

    @GetMapping("/lancamentos")
    @Operation(summary = "Listar lançamentos com filtros opcionais")
    public List<LancamentoResponse> listar(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) TipoTransacao tipo) {
        return lancamentoService.listar(mes, ano, tipo);
    }

    @PostMapping("/lancamentos")
    @Operation(summary = "Criar lançamento")
    public ResponseEntity<LancamentoResponse> criar(@Valid @RequestBody LancamentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lancamentoService.criar(request));
    }

    @PutMapping("/lancamentos/{id}")
    @Operation(summary = "Atualizar lançamento")
    public LancamentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody LancamentoRequest request) {
        return lancamentoService.atualizar(id, request);
    }

    @DeleteMapping("/lancamentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar lançamento")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        lancamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/lancamentos/exportar.csv", produces = "text/csv")
    @Operation(summary = "Exportar lançamentos em CSV",
               description = "Aceita os mesmos filtros de /lancamentos: mes, ano, tipo.")
    public ResponseEntity<byte[]> exportarCsv(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) TipoTransacao tipo) {

        byte[] csv = lancamentoService.exportarCsv(mes, ano, tipo);
        String filename = "lancamentos" +
                (mes != null && ano != null ? "_" + ano + "_" + String.format("%02d", mes) : "") +
                ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/resumo")
    @Operation(summary = "Resumo mensal: totais e breakdown por categoria")
    public ResumoResponse resumo(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano) {
        var hoje = LocalDate.now();
        return lancamentoService.resumo(
                mes != null ? mes : hoje.getMonthValue(),
                ano != null ? ano : hoje.getYear());
    }
}
