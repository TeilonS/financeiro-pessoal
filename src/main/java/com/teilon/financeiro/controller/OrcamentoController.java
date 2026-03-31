package com.teilon.financeiro.controller;

import com.teilon.financeiro.dto.OrcamentoResponse;
import com.teilon.financeiro.service.OrcamentoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orcamentos")
@RequiredArgsConstructor
@Tag(name = "Orçamentos")
@SecurityRequirement(name = "bearerAuth")
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    @GetMapping
    public ResponseEntity<List<OrcamentoResponse>> listar(@RequestParam int mes, @RequestParam int ano) {
        return ResponseEntity.ok(orcamentoService.listar(mes, ano));
    }

    @PostMapping
    public ResponseEntity<OrcamentoResponse> salvar(@RequestBody Map<String, Object> body) {
        if (body.get("categoriaId") == null || body.get("mes") == null
                || body.get("ano") == null || body.get("valorLimite") == null) {
            throw new IllegalArgumentException("Campos obrigatórios: categoriaId, mes, ano, valorLimite");
        }
        Long categoriaId = ((Number) body.get("categoriaId")).longValue();
        int mes = ((Number) body.get("mes")).intValue();
        int ano = ((Number) body.get("ano")).intValue();
        BigDecimal limite = new BigDecimal(body.get("valorLimite").toString());
        return ResponseEntity.ok(orcamentoService.salvar(categoriaId, mes, ano, limite));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        orcamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
