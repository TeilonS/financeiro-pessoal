package com.teilon.financeiro.repository;

import com.teilon.financeiro.model.Categoria;
import com.teilon.financeiro.model.Lancamento;
import com.teilon.financeiro.model.TipoTransacao;
import com.teilon.financeiro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    @Query("SELECT l FROM Lancamento l LEFT JOIN FETCH l.categoria WHERE l.usuario = :usuario")
    List<Lancamento> findAllByUsuario(@Param("usuario") Usuario usuario);

    @Query("SELECT l FROM Lancamento l LEFT JOIN FETCH l.categoria WHERE l.usuario = :usuario AND MONTH(l.data) = :mes AND YEAR(l.data) = :ano")
    List<Lancamento> findAllByUsuarioAndMesAndAno(@Param("usuario") Usuario usuario, @Param("mes") int mes, @Param("ano") int ano);

    @Query("SELECT l FROM Lancamento l LEFT JOIN FETCH l.categoria WHERE l.usuario = :usuario AND MONTH(l.data) = :mes AND YEAR(l.data) = :ano AND l.tipo = :tipo")
    List<Lancamento> findAllByUsuarioAndMesAndAnoAndTipo(@Param("usuario") Usuario usuario, @Param("mes") int mes, @Param("ano") int ano, @Param("tipo") TipoTransacao tipo);

    @Query("SELECT l FROM Lancamento l LEFT JOIN FETCH l.categoria WHERE l.usuario = :usuario AND l.tipo = :tipo")
    List<Lancamento> findAllByUsuarioAndTipo(@Param("usuario") Usuario usuario, @Param("tipo") TipoTransacao tipo);

    @Query("SELECT l FROM Lancamento l LEFT JOIN FETCH l.categoria WHERE l.id = :id AND l.usuario = :usuario")
    Optional<Lancamento> findByIdAndUsuario(@Param("id") Long id, @Param("usuario") Usuario usuario);

    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM Lancamento l WHERE l.usuario = :usuario AND l.categoria = :categoria AND MONTH(l.data) = :mes AND YEAR(l.data) = :ano")
    BigDecimal sumByUsuarioAndCategoriaAndMesAndAno(@Param("usuario") Usuario usuario, @Param("categoria") Categoria categoria, @Param("mes") int mes, @Param("ano") int ano);

    @Query("SELECT MONTH(l.data), COALESCE(SUM(l.valor), 0) FROM Lancamento l WHERE l.usuario = :usuario AND YEAR(l.data) = :ano AND l.tipo = :tipo GROUP BY MONTH(l.data)")
    List<Object[]> sumPorMesNoAno(@Param("usuario") Usuario usuario, @Param("ano") int ano, @Param("tipo") TipoTransacao tipo);

    @Query("SELECT l.categoria, COALESCE(SUM(l.valor), 0) FROM Lancamento l WHERE l.usuario = :usuario AND MONTH(l.data) = :mes AND YEAR(l.data) = :ano AND l.tipo = :tipo GROUP BY l.categoria ORDER BY SUM(l.valor) DESC")
    List<Object[]> topCategoriasPorMes(@Param("usuario") Usuario usuario, @Param("mes") int mes, @Param("ano") int ano, @Param("tipo") TipoTransacao tipo);

    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM Lancamento l WHERE l.usuario = :usuario AND MONTH(l.data) = :mes AND YEAR(l.data) = :ano AND l.tipo = :tipo")
    BigDecimal sumPorTipoEPeriodo(@Param("usuario") Usuario usuario, @Param("mes") int mes, @Param("ano") int ano, @Param("tipo") TipoTransacao tipo);
}
