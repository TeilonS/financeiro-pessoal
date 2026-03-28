package com.teilon.financeiro.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "regras_categorizacao",
       uniqueConstraints = @UniqueConstraint(columnNames = {"chave", "usuario_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegraCategorizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Palavra-chave normalizada extraída da descrição confirmada pelo usuário. */
    @Column(nullable = false, length = 300)
    private String chave;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
