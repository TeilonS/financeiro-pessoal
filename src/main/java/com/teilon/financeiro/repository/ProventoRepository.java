package com.teilon.financeiro.repository;

import com.teilon.financeiro.model.Provento;
import com.teilon.financeiro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProventoRepository extends JpaRepository<Provento, Long> {
    List<Provento> findByUsuarioOrderByAnoDescMesDesc(Usuario usuario);
    Optional<Provento> findByIdAndUsuario(Long id, Usuario usuario);
}
