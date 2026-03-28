package com.teilon.financeiro.repository;

import com.teilon.financeiro.model.OrcamentoMensal;
import com.teilon.financeiro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrcamentoMensalRepository extends JpaRepository<OrcamentoMensal, Long> {
    List<OrcamentoMensal> findAllByUsuario(Usuario usuario);
    List<OrcamentoMensal> findAllByUsuarioAndMesAndAno(Usuario usuario, int mes, int ano);
    Optional<OrcamentoMensal> findByIdAndUsuario(Long id, Usuario usuario);
}
