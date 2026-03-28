package com.teilon.financeiro.repository;

import com.teilon.financeiro.model.Recorrencia;
import com.teilon.financeiro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecorrenciaRepository extends JpaRepository<Recorrencia, Long> {
    List<Recorrencia> findAllByUsuario(Usuario usuario);
    List<Recorrencia> findAllByUsuarioAndAtivaTrue(Usuario usuario);
    Optional<Recorrencia> findByIdAndUsuario(Long id, Usuario usuario);
}
