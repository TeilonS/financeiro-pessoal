package com.teilon.financeiro.service;

import com.teilon.financeiro.dto.CartaoResponse;
import com.teilon.financeiro.dto.FaturaResponse;
import com.teilon.financeiro.model.CartaoCredito;
import com.teilon.financeiro.model.FaturaMensalCartao;
import com.teilon.financeiro.model.Usuario;
import com.teilon.financeiro.repository.CartaoCreditoRepository;
import com.teilon.financeiro.repository.FaturaMensalCartaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartaoService {

    private final CartaoCreditoRepository cartaoRepository;
    private final FaturaMensalCartaoRepository faturaRepo;
    private final UsuarioService usuarioService;

    public List<CartaoResponse> listar() {
        Usuario u = usuarioService.getAutenticado();
        YearMonth m = YearMonth.now();
        Map<Long, BigDecimal> faturas = faturaRepo
            .findByUsuarioAndMesAndAno(u, m.getMonthValue(), m.getYear()).stream()
            .collect(Collectors.toMap(f -> f.getCartao().getId(), FaturaMensalCartao::getValor));

        return cartaoRepository.findAllByUsuario(u).stream().map(c -> {
            BigDecimal fat = faturas.getOrDefault(c.getId(), BigDecimal.ZERO);
            return new CartaoResponse(c.getId(), c.getNome(), c.getLimite(),
                fat, c.getDiaVencimento(), c.getCor(), c.getLimite().subtract(fat));
        }).toList();
    }

    @Transactional
    public CartaoResponse criar(Map<String, Object> body) {
        if (body.get("nome") == null || body.get("limite") == null || body.get("diaVencimento") == null) {
            throw new IllegalArgumentException("Campos obrigatórios: nome, limite, diaVencimento");
        }
        Usuario usuario = usuarioService.getAutenticado();
        CartaoCredito c = CartaoCredito.builder()
                .nome((String) body.get("nome"))
                .limite(new BigDecimal(body.get("limite").toString()))
                .diaVencimento(((Number) body.get("diaVencimento")).intValue())
                .cor((String) body.getOrDefault("cor", "#6366f1"))
                .usuario(usuario)
                .build();
        CartaoCredito saved = cartaoRepository.save(c);
        return new CartaoResponse(saved.getId(), saved.getNome(), saved.getLimite(),
            BigDecimal.ZERO, saved.getDiaVencimento(), saved.getCor(), saved.getLimite());
    }

    @Transactional
    public CartaoResponse atualizar(Long id, Map<String, Object> body) {
        Usuario usuario = usuarioService.getAutenticado();
        CartaoCredito c = cartaoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cartão não encontrado"));
        if (body.get("nome") != null) c.setNome((String) body.get("nome"));
        if (body.get("limite") != null) c.setLimite(new BigDecimal(body.get("limite").toString()));
        if (body.get("diaVencimento") != null) c.setDiaVencimento(((Number) body.get("diaVencimento")).intValue());
        if (body.get("cor") != null) c.setCor((String) body.get("cor"));
        CartaoCredito saved = cartaoRepository.save(c);
        YearMonth m = YearMonth.now();
        BigDecimal fat = faturaRepo.findByCartaoAndMesAndAno(saved, m.getMonthValue(), m.getYear())
            .map(FaturaMensalCartao::getValor).orElse(BigDecimal.ZERO);
        return new CartaoResponse(saved.getId(), saved.getNome(), saved.getLimite(),
            fat, saved.getDiaVencimento(), saved.getCor(), saved.getLimite().subtract(fat));
    }

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = usuarioService.getAutenticado();
        CartaoCredito c = cartaoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cartão não encontrado"));
        cartaoRepository.delete(c);
    }

    @Transactional
    public void registrarFatura(Long cartaoId, int mes, int ano, BigDecimal valor) {
        Usuario u = usuarioService.getAutenticado();
        CartaoCredito c = cartaoRepository.findByIdAndUsuario(cartaoId, u)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cartão não encontrado"));
        FaturaMensalCartao f = faturaRepo.findByCartaoAndMesAndAno(c, mes, ano)
            .orElseGet(() -> {
                FaturaMensalCartao nova = new FaturaMensalCartao();
                nova.setCartao(c); nova.setUsuario(u); nova.setMes(mes); nova.setAno(ano);
                return nova;
            });
        f.setValor(valor);
        faturaRepo.save(f);
    }

    public List<FaturaResponse> listarFaturas(Long cartaoId) {
        Usuario u = usuarioService.getAutenticado();
        CartaoCredito c = cartaoRepository.findByIdAndUsuario(cartaoId, u)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cartão não encontrado"));
        return faturaRepo.findByCartaoOrderByAnoDescMesDesc(c).stream()
            .map(f -> new FaturaResponse(f.getId(), f.getMes(), f.getAno(), f.getValor())).toList();
    }

    public BigDecimal totalFaturas(Usuario usuario) {
        YearMonth m = YearMonth.now();
        return faturaRepo.sumByUsuarioAndCompetencia(usuario, m.getMonthValue(), m.getYear());
    }
}
