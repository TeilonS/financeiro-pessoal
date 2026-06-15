package com.teilon.financeiro.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "investimentos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Investimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String instituicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoInvestimento tipo;

    /** Código do ativo na B3 (ex: PETR4, HGLG11). Preenchido apenas para ACOES e FII. */
    @Column
    private String ticker;

    /** Quantidade de cotas/ações em carteira. */
    @Column(precision = 15, scale = 6)
    private BigDecimal cotas;

    /** Último preço unitário obtido via cotação automática. */
    @Column(name = "preco_unitario", precision = 15, scale = 6)
    private BigDecimal precoUnitario;

    /** Data/hora da última atualização de preço. */
    @Column(name = "ultima_atualizacao_preco")
    private LocalDateTime ultimaAtualizacaoPreco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
