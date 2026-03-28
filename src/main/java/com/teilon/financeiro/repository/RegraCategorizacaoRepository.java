package com.teilon.financeiro.repository;

import com.teilon.financeiro.model.RegraCategorizacao;
import com.teilon.financeiro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegraCategorizacaoRepository extends JpaRepository<RegraCategorizacao, Long> {
    List<RegraCategorizacao> findAllByUsuario(Usuario usuario);
    Optional<RegraCategorizacao> findByChaveAndUsuario(String chave, Usuario usuario);
}
