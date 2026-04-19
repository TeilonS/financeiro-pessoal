package com.teilon.financeiro.controller;

import com.teilon.financeiro.dto.InvestimentoRequest;
import com.teilon.financeiro.dto.InvestimentoResponse;
import com.teilon.financeiro.dto.SnapshotRequest;
import com.teilon.financeiro.dto.SnapshotResponse;
import com.teilon.financeiro.service.InvestimentoService;
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
@RequestMapping("/investimentos")
@RequiredArgsConstructor
@Tag(name = "Investimentos")
@SecurityRequirement(name = "bearerAuth")
public class InvestimentoController {

    private final InvestimentoService investimentoService;

    @GetMapping
    @Operation(summary = "Listar investimentos")
    public List<InvestimentoResponse> listar() {
        return investimentoService.listar();
    }

    @PostMapping
    @Operation(summary = "Criar novo investimento")
    public ResponseEntity<InvestimentoResponse> criar(@Valid @RequestBody InvestimentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investimentoService.criar(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar investimento")
    public void deletar(@PathVariable Long id) {
        investimentoService.deletar(id);
    }

    @PostMapping("/{id}/snapshot")
    @Operation(summary = "Registrar saldo mensal de um investimento")
    public void registrarSnapshot(@PathVariable Long id, @Valid @RequestBody SnapshotRequest request) {
        investimentoService.registrarSnapshot(id, request);
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Listar histórico de snapshots de um investimento")
    public List<SnapshotResponse> listarHistorico(@PathVariable Long id) {
        return investimentoService.listarHistorico(id);
    }
}
