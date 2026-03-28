package com.teilon.financeiro.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "extratos_brutos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExtratoBruto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeArquivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormatoExtrato formato;

    @Column(nullable = false)
    private LocalDateTime dataImportacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
