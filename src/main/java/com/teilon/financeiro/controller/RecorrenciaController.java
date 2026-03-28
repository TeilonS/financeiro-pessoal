package com.teilon.financeiro.controller;

import com.teilon.financeiro.dto.*;
import com.teilon.financeiro.service.RecorrenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recorrencias")
@RequiredArgsConstructor
@Tag(name = "Recorrências", description = "Lançamentos recorrentes automáticos")
@SecurityRequirement(name = "bearerAuth")
public class RecorrenciaController {

    private final RecorrenciaService recorrenciaService;

    @PostMapping
    @Operation(summary = "Cadastrar recorrência",
               description = "Frequências: MENSAL, SEMANAL, ANUAL. " +
                             "diaReferencia: para MENSAL = dia do mês (1–28); SEMANAL = dia da semana (1=seg..7=dom); ANUAL = mês do ano (1–12).")
    public ResponseEntity<RecorrenciaResponse> criar(@Valid @RequestBody RecorrenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recorrenciaService.criar(request));
    }

    @GetMapping
    @Operation(summary = "Listar recorrências")
    public ResponseEntity<List<RecorrenciaResponse>> listar() {
        return ResponseEntity.ok(recorrenciaService.listar());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar recorrência")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        recorrenciaService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/gerar")
    @Operation(summary = "Gerar lançamentos do mês",
               description = "Percorre todas as recorrências ativas e cria lançamentos para o mês/ano informado, sem duplicar.")
    public ResponseEntity<GerarLancamentosResponse> gerar(
            @RequestParam int mes,
            @RequestParam int ano) {
        return ResponseEntity.ok(recorrenciaService.gerarParaMes(mes, ano));
    }
}
