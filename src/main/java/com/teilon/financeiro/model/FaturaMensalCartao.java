package com.teilon.financeiro.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "faturas_mensais_cartao",
    uniqueConstraints = @UniqueConstraint(columnNames = {"cartao_id", "mes", "ano"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FaturaMensalCartao {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartao_id", nullable = false)
    private CartaoCredito cartao;

    @Column(nullable = false) private Integer mes;
    @Column(nullable = false) private Integer ano;
    @Column(nullable = false, precision = 15, scale = 2) private BigDecimal valor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
