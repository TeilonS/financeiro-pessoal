package com.teilon.financeiro.controller;

import com.teilon.financeiro.dto.CartaoResponse;
import com.teilon.financeiro.service.CartaoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cartoes")
@RequiredArgsConstructor
@Tag(name = "Cartões de Crédito")
@SecurityRequirement(name = "bearerAuth")
public class CartaoController {

    private final CartaoService cartaoService;

    @GetMapping
    public ResponseEntity<List<CartaoResponse>> listar() {
        return ResponseEntity.ok(cartaoService.listar());
    }

    @PostMapping
    public ResponseEntity<CartaoResponse> criar(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(cartaoService.criar(body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CartaoResponse> atualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(cartaoService.atualizar(id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        cartaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
