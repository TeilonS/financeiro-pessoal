package com.teilon.financeiro.repository;

import com.teilon.financeiro.model.Categoria;
import com.teilon.financeiro.model.Meta;
import com.teilon.financeiro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MetaRepository extends JpaRepository<Meta, Long> {
    List<Meta> findAllByUsuario(Usuario usuario);
    Optional<Meta> findByIdAndUsuario(Long id, Usuario usuario);
    boolean existsByCategoriaAndMesAndAnoAndUsuario(Categoria categoria, int mes, int ano, Usuario usuario);
}
