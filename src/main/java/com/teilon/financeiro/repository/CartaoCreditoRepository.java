package com.teilon.financeiro.repository;

import com.teilon.financeiro.model.CartaoCredito;
import com.teilon.financeiro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CartaoCreditoRepository extends JpaRepository<CartaoCredito, Long> {
    List<CartaoCredito> findAllByUsuario(Usuario usuario);
    Optional<CartaoCredito> findByIdAndUsuario(Long id, Usuario usuario);

    @Query("SELECT COALESCE(SUM(c.faturaAtual), 0) FROM CartaoCredito c WHERE c.usuario = :usuario")
    BigDecimal sumFaturasByUsuario(@Param("usuario") Usuario usuario);
}
