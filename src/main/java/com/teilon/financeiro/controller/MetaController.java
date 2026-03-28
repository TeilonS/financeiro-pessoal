package com.teilon.financeiro.controller;

import com.teilon.financeiro.dto.AlertaMetaResponse;
import com.teilon.financeiro.dto.MetaRequest;
import com.teilon.financeiro.dto.MetaResponse;
import com.teilon.financeiro.service.MetaService;
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
@RequestMapping("/metas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Metas")
public class MetaController {

    private final MetaService metaService;

    @GetMapping
    @Operation(summary = "Listar metas do usuário")
    public List<MetaResponse> listar() {
        return metaService.listar();
    }

    @PostMapping
    @Operation(summary = "Criar meta mensal para uma categoria")
    public ResponseEntity<MetaResponse> criar(@Valid @RequestBody MetaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(metaService.criar(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar meta")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        metaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alertas")
    @Operation(summary = "Categorias que ultrapassaram o limite mensal")
    public List<AlertaMetaResponse> alertas(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano) {
        return metaService.alertas(mes, ano);
    }
}
