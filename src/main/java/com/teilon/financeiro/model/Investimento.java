package com.teilon.financeiro.model;

import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
