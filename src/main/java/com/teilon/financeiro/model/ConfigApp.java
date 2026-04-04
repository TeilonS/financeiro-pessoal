package com.teilon.financeiro.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "config_app")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigApp {

    @Id
    @Column(name = "chave", length = 100)
    private String chave;

    @Column(name = "valor", length = 1000, nullable = false)
    private String valor;
}
