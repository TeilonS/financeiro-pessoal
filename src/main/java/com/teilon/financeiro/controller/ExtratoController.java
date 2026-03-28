package com.teilon.financeiro.controller;

import com.teilon.financeiro.dto.ConfirmarTransacaoRequest;
import com.teilon.financeiro.dto.ExtratoUploadResponse;
import com.teilon.financeiro.dto.PreviewItemResponse;
import com.teilon.financeiro.dto.TransacaoPendenteResponse;
import com.teilon.financeiro.model.FormatoExtrato;
import com.teilon.financeiro.service.ExtratoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/extrato")
@RequiredArgsConstructor
@Tag(name = "Extrato", description = "Importação de extrato bancário")
@SecurityRequirement(name = "bearerAuth")
public class ExtratoController {

    private final ExtratoService extratoService;

    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Pré-visualizar extrato sem importar",
               description = "Analisa o arquivo e retorna as transações que seriam importadas, com categorias sugeridas e flags de transações ignoradas.")
    public ResponseEntity<List<PreviewItemResponse>> preview(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("formato") FormatoExtrato formato) {
        return ResponseEntity.ok(extratoService.preview(arquivo, formato));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar extrato bancário",
               description = "Faz upload de um arquivo OFX ou CSV e cria transações pendentes para revisão. " +
                             "Formatos aceitos: OFX, CSV_INTER, CSV_C6")
    public ResponseEntity<ExtratoUploadResponse> upload(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("formato") FormatoExtrato formato) {

        return ResponseEntity.ok(extratoService.importar(arquivo, formato));
    }

    @GetMapping("/pendentes")
    @Operation(summary = "Listar transações pendentes de revisão")
    public ResponseEntity<List<TransacaoPendenteResponse>> listarPendentes() {
        return ResponseEntity.ok(extratoService.listarPendentes());
    }

    @PostMapping("/pendentes/{id}/confirmar")
    @Operation(summary = "Confirmar transação pendente",
               description = "Cria um lançamento a partir da transação importada. Requer categoriaId.")
    public ResponseEntity<Void> confirmar(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmarTransacaoRequest request) {

        extratoService.confirmar(id, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/pendentes/confirmar-lote")
    @Operation(summary = "Confirmar múltiplas transações pendentes de uma vez")
    public ResponseEntity<Void> confirmarLote(@RequestBody List<Map<String, Long>> itens) {
        extratoService.confirmarLote(itens);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/pendentes/{id}/ignorar")
    @Operation(summary = "Ignorar transação pendente")
    public ResponseEntity<Void> ignorar(@PathVariable Long id) {
        extratoService.ignorar(id);
        return ResponseEntity.noContent().build();
    }
}
