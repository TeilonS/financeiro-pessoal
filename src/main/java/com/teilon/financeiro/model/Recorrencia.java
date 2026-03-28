package com.teilon.financeiro.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "recorrencias")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Recorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FrequenciaRecorrencia frequencia;

    /** Dia do mês (1–28) para MENSAL; dia da semana (1=seg..7=dom) para SEMANAL; dia do ano (1–365) não usado. */
    @Column(nullable = false)
    private Integer diaReferencia;

    /** Data a partir da qual a recorrência está ativa. */
    @Column(nullable = false)
    private LocalDate dataInicio;

    /** Data de encerramento (null = sem fim). */
    private LocalDate dataFim;

    @Column(nullable = false)
    private Boolean ativa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
