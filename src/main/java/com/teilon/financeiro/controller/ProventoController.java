package com.teilon.financeiro.controller;

import com.teilon.financeiro.dto.ProventoRequest;
import com.teilon.financeiro.dto.ProventoResponse;
import com.teilon.financeiro.service.ProventoService;
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
@RequestMapping("/proventos")
@RequiredArgsConstructor
@Tag(name = "Proventos")
@SecurityRequirement(name = "bearerAuth")
public class ProventoController {

    private final ProventoService proventoService;

    @GetMapping
    @Operation(summary = "Listar proventos recebidos")
    public List<ProventoResponse> listar() {
        return proventoService.listar();
    }

    @PostMapping
    @Operation(summary = "Lançar provento recebido de um ativo")
    public ResponseEntity<ProventoResponse> criar(@Valid @RequestBody ProventoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proventoService.criar(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover provento lançado")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        proventoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
