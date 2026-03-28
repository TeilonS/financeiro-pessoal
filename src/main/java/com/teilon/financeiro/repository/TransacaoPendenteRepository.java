package com.teilon.financeiro.repository;

import com.teilon.financeiro.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransacaoPendenteRepository extends JpaRepository<TransacaoPendente, Long> {
    List<TransacaoPendente> findAllByUsuarioAndStatus(Usuario usuario, StatusTransacao status);
    List<TransacaoPendente> findAllByExtrato(ExtratoBruto extrato);
    Optional<TransacaoPendente> findByIdAndUsuario(Long id, Usuario usuario);
}
