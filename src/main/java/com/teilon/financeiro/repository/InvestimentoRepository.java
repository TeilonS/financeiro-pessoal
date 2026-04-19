package com.teilon.financeiro.repository;

import com.teilon.financeiro.model.Investimento;
import com.teilon.financeiro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvestimentoRepository extends JpaRepository<Investimento, Long> {
    List<Investimento> findAllByUsuario(Usuario usuario);
    Optional<Investimento> findByIdAndUsuario(Long id, Usuario usuario);
}
