package com.teilon.financeiro.service;

import com.teilon.financeiro.dto.CartaoResponse;
import com.teilon.financeiro.dto.FaturaResponse;
import com.teilon.financeiro.model.CartaoCredito;
import com.teilon.financeiro.model.FaturaMensalCartao;
import com.teilon.financeiro.model.Usuario;
import com.teilon.financeiro.repository.CartaoCreditoRepository;
import com.teilon.financeiro.repository.FaturaMensalCartaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartaoServiceTest {

    @Mock private CartaoCreditoRepository cartaoRepository;
    @Mock private FaturaMensalCartaoRepository faturaRepo;
    @Mock private UsuarioService usuarioService;

    @InjectMocks private CartaoService cartaoService;

    private Usuario usuario;
    private CartaoCredito cartao;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(1L).nome("Teilon").email("t@t.com").senha("x").build();
        cartao = CartaoCredito.builder()
                .id(10L).nome("Nubank").limite(new BigDecimal("5000.00"))
                .diaVencimento(10).cor("#8A05BE").usuario(usuario).build();
    }

    @Test
    void listar_combinaCartaoComFaturaDoMesAtual() {
        YearMonth m = YearMonth.now();
        FaturaMensalCartao fat = FaturaMensalCartao.builder()
                .cartao(cartao).mes(m.getMonthValue()).ano(m.getYear())
                .valor(new BigDecimal("1234.56")).usuario(usuario).build();
        when(usuarioService.getAutenticado()).thenReturn(usuario);
        when(faturaRepo.findByUsuarioAndMesAndAno(usuario, m.getMonthValue(), m.getYear()))
                .thenReturn(List.of(fat));
        when(cartaoRepository.findAllByUsuario(usuario)).thenReturn(List.of(cartao));

        List<CartaoResponse> result = cartaoService.listar();

        assertThat(result).hasSize(1);
        CartaoResponse r = result.get(0);
        assertThat(r.faturaMesAtual()).isEqualByComparingTo("1234.56");
        assertThat(r.limiteDisponivel()).isEqualByComparingTo("3765.44");
    }

    @Test
    void listar_semFaturaNoMesRetornaZero() {
        YearMonth m = YearMonth.now();
        when(usuarioService.getAutenticado()).thenReturn(usuario);
        when(faturaRepo.findByUsuarioAndMesAndAno(usuario, m.getMonthValue(), m.getYear()))
                .thenReturn(List.of());
        when(cartaoRepository.findAllByUsuario(usuario)).thenReturn(List.of(cartao));

        List<CartaoResponse> result = cartaoService.listar();

        assertThat(result.get(0).faturaMesAtual()).isEqualByComparingTo("0");
        assertThat(result.get(0).limiteDisponivel()).isEqualByComparingTo("5000.00");
    }

    @Test
    void registrarFatura_quandoNaoExisteCria() {
        when(usuarioService.getAutenticado()).thenReturn(usuario);
        when(cartaoRepository.findByIdAndUsuario(10L, usuario)).thenReturn(Optional.of(cartao));
        when(faturaRepo.findByCartaoAndMesAndAno(cartao, 6, 2026)).thenReturn(Optional.empty());

        cartaoService.registrarFatura(10L, 6, 2026, new BigDecimal("999.99"));

        verify(faturaRepo, times(1)).save(any(FaturaMensalCartao.class));
    }

    @Test
    void registrarFatura_quandoJaExisteAtualizaValor() {
        FaturaMensalCartao existente = FaturaMensalCartao.builder()
                .id(99L).cartao(cartao).mes(6).ano(2026)
                .valor(new BigDecimal("100.00")).usuario(usuario).build();
        when(usuarioService.getAutenticado()).thenReturn(usuario);
        when(cartaoRepository.findByIdAndUsuario(10L, usuario)).thenReturn(Optional.of(cartao));
        when(faturaRepo.findByCartaoAndMesAndAno(cartao, 6, 2026)).thenReturn(Optional.of(existente));

        cartaoService.registrarFatura(10L, 6, 2026, new BigDecimal("888.88"));

        assertThat(existente.getValor()).isEqualByComparingTo("888.88");
        verify(faturaRepo).save(existente);
    }

    @Test
    void registrarFatura_cartaoDeOutroUsuarioLancaNotFound() {
        when(usuarioService.getAutenticado()).thenReturn(usuario);
        when(cartaoRepository.findByIdAndUsuario(10L, usuario)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartaoService.registrarFatura(10L, 6, 2026, BigDecimal.TEN))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cartão não encontrado");
        verify(faturaRepo, never()).save(any());
    }

    @Test
    void listarFaturas_ordenacaoDecrescenteDoRepo() {
        FaturaMensalCartao f1 = FaturaMensalCartao.builder().id(1L).cartao(cartao).mes(6).ano(2026).valor(BigDecimal.TEN).usuario(usuario).build();
        FaturaMensalCartao f2 = FaturaMensalCartao.builder().id(2L).cartao(cartao).mes(5).ano(2026).valor(BigDecimal.ONE).usuario(usuario).build();
        when(usuarioService.getAutenticado()).thenReturn(usuario);
        when(cartaoRepository.findByIdAndUsuario(10L, usuario)).thenReturn(Optional.of(cartao));
        when(faturaRepo.findByCartaoOrderByAnoDescMesDesc(cartao)).thenReturn(List.of(f1, f2));

        List<FaturaResponse> result = cartaoService.listarFaturas(10L);

        assertThat(result).extracting(FaturaResponse::mes).containsExactly(6, 5);
    }

    @Test
    void totalFaturas_delegaSomaParaQueryDoMesCorrente() {
        YearMonth m = YearMonth.now();
        when(faturaRepo.sumByUsuarioAndCompetencia(usuario, m.getMonthValue(), m.getYear()))
                .thenReturn(new BigDecimal("3210.00"));

        BigDecimal total = cartaoService.totalFaturas(usuario);

        assertThat(total).isEqualByComparingTo("3210.00");
    }
}
