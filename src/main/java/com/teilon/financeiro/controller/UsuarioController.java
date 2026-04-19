package com.teilon.financeiro.controller;

import com.teilon.financeiro.model.Usuario;
import com.teilon.financeiro.repository.CartaoCreditoRepository;
import com.teilon.financeiro.service.CartaoService;
import com.teilon.financeiro.service.InvestimentoService;
import com.teilon.financeiro.service.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuário")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final CartaoService cartaoService;
    private final InvestimentoService investimentoService;

    @GetMapping("/reserva-emergencia")
    public ResponseEntity<Map<String, BigDecimal>> getReserva() {
        return ResponseEntity.ok(Map.of("valor", usuarioService.getReservaEmergencia()));
    }

    @PutMapping("/reserva-emergencia")
    public ResponseEntity<Map<String, BigDecimal>> atualizarReserva(@RequestBody Map<String, BigDecimal> body) {
        BigDecimal novo = usuarioService.atualizarReservaEmergencia(body.get("valor"));
        return ResponseEntity.ok(Map.of("valor", novo));
    }

    @GetMapping("/patrimonio")
    public ResponseEntity<Map<String, BigDecimal>> patrimonio() {
        Usuario usuario = usuarioService.getAutenticado();
        BigDecimal reserva = usuarioService.getReservaEmergencia();
        BigDecimal faturas = cartaoService.totalFaturas();
        BigDecimal investimentos = investimentoService.calcularTotalInvestido(usuario);
        BigDecimal liquido = reserva.add(investimentos).subtract(faturas);
        
        return ResponseEntity.ok(Map.of(
                "reservaEmergencia", reserva,
                "totalInvestimentos", investimentos,
                "totalFaturas", faturas,
                "patrimonioLiquido", liquido
        ));
    }
}
