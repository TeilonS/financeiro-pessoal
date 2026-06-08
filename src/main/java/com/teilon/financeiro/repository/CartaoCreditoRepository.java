package com.teilon.financeiro.repository;

import com.teilon.financeiro.model.CartaoCredito;
import com.teilon.financeiro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartaoCreditoRepository extends JpaRepository<CartaoCredito, Long> {
    List<CartaoCredito> findAllByUsuario(Usuario usuario);
    Optional<CartaoCredito> findByIdAndUsuario(Long id, Usuario usuario);
}
