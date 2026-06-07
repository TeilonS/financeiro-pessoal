package com.teilon.financeiro.repository;

import com.teilon.financeiro.model.CartaoCredito;
import com.teilon.financeiro.model.FaturaMensalCartao;
import com.teilon.financeiro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FaturaMensalCartaoRepository extends JpaRepository<FaturaMensalCartao, Long> {

    Optional<FaturaMensalCartao> findByCartaoAndMesAndAno(CartaoCredito cartao, Integer mes, Integer ano);

    List<FaturaMensalCartao> findByCartaoOrderByAnoDescMesDesc(CartaoCredito cartao);

    List<FaturaMensalCartao> findByUsuarioAndMesAndAno(Usuario usuario, Integer mes, Integer ano);

    @Query("""
        SELECT COALESCE(SUM(f.valor), 0) FROM FaturaMensalCartao f
        WHERE f.usuario = :usuario AND f.mes = :mes AND f.ano = :ano
        """)
    BigDecimal sumByUsuarioAndCompetencia(@Param("usuario") Usuario usuario,
                                          @Param("mes") Integer mes,
                                          @Param("ano") Integer ano);
}
