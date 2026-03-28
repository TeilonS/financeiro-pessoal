package com.teilon.financeiro.dto;

import com.teilon.financeiro.model.Categoria;
import com.teilon.financeiro.model.TipoTransacao;

public record CategoriaResponse(
        Long id,
        String nome,
        TipoTransacao tipo,
        String cor,
        Long categoriaPaiId,
        String categoriaPaiNome
) {
    public static CategoriaResponse de(Categoria c) {
        return new CategoriaResponse(
                c.getId(), c.getNome(), c.getTipo(), c.getCor(),
                c.getCategoriaPai() != null ? c.getCategoriaPai().getId() : null,
                c.getCategoriaPai() != null ? c.getCategoriaPai().getNome() : null
        );
    }
}
