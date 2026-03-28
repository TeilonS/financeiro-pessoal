package com.teilon.financeiro.repository;

import com.teilon.financeiro.model.Categoria;
import com.teilon.financeiro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findAllByUsuario(Usuario usuario);
    Optional<Categoria> findByIdAndUsuario(Long id, Usuario usuario);
}
