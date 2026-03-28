package com.teilon.financeiro.service;

import com.teilon.financeiro.dto.CartaoResponse;
import com.teilon.financeiro.model.CartaoCredito;
import com.teilon.financeiro.model.Usuario;
import com.teilon.financeiro.repository.CartaoCreditoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartaoService {

    private final CartaoCreditoRepository cartaoRepository;
    private final UsuarioService usuarioService;

    public List<CartaoResponse> listar() {
        Usuario usuario = usuarioService.getAutenticado();
        return cartaoRepository.findAllByUsuario(usuario).stream().map(this::toResponse).toList();
    }

    @Transactional
    public CartaoResponse criar(Map<String, Object> body) {
        Usuario usuario = usuarioService.getAutenticado();
        CartaoCredito c = CartaoCredito.builder()
                .nome((String) body.get("nome"))
                .limite(new BigDecimal(body.get("limite").toString()))
                .faturaAtual(body.get("faturaAtual") != null ? new BigDecimal(body.get("faturaAtual").toString()) : BigDecimal.ZERO)
                .diaVencimento(((Number) body.get("diaVencimento")).intValue())
                .cor((String) body.getOrDefault("cor", "#6366f1"))
                .usuario(usuario)
                .build();
        return toResponse(cartaoRepository.save(c));
    }

    @Transactional
    public CartaoResponse atualizar(Long id, Map<String, Object> body) {
        Usuario usuario = usuarioService.getAutenticado();
        CartaoCredito c = cartaoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cartão não encontrado"));
        if (body.get("nome") != null) c.setNome((String) body.get("nome"));
        if (body.get("limite") != null) c.setLimite(new BigDecimal(body.get("limite").toString()));
        if (body.get("faturaAtual") != null) c.setFaturaAtual(new BigDecimal(body.get("faturaAtual").toString()));
        if (body.get("diaVencimento") != null) c.setDiaVencimento(((Number) body.get("diaVencimento")).intValue());
        if (body.get("cor") != null) c.setCor((String) body.get("cor"));
        return toResponse(cartaoRepository.save(c));
    }

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = usuarioService.getAutenticado();
        CartaoCredito c = cartaoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cartão não encontrado"));
        cartaoRepository.delete(c);
    }

    public BigDecimal totalFaturas() {
        Usuario usuario = usuarioService.getAutenticado();
        return cartaoRepository.sumFaturasByUsuario(usuario);
    }

    private CartaoResponse toResponse(CartaoCredito c) {
        BigDecimal fatura = c.getFaturaAtual() != null ? c.getFaturaAtual() : BigDecimal.ZERO;
        return new CartaoResponse(c.getId(), c.getNome(), c.getLimite(), fatura,
                c.getDiaVencimento(), c.getCor(), c.getLimite().subtract(fatura));
    }
}
