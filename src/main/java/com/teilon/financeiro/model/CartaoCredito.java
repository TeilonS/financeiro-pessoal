package com.teilon.financeiro.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cartoes_credito")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CartaoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal limite;

    @Column(precision = 15, scale = 2)
    private BigDecimal faturaAtual;

    /** Dia do mês em que vence a fatura (1–31). */
    @Column(nullable = false)
    private Integer diaVencimento;

    @Column(length = 7)
    private String cor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
