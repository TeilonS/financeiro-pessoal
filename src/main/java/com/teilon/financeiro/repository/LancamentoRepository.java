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
    List<Lancamento> findAllByUsuario(Usuario usuario);

    @Query("SELECT l FROM Lancamento l WHERE l.usuario = :usuario AND MONTH(l.data) = :mes AND YEAR(l.data) = :ano")
    List<Lancamento> findAllByUsuarioAndMesAndAno(@Param("usuario") Usuario usuario, @Param("mes") int mes, @Param("ano") int ano);

    List<Lancamento> findAllByUsuarioAndTipo(Usuario usuario, TipoTransacao tipo);
    Optional<Lancamento> findByIdAndUsuario(Long id, Usuario usuario);

    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM Lancamento l WHERE l.usuario = :usuario AND l.categoria = :categoria AND MONTH(l.data) = :mes AND YEAR(l.data) = :ano")
    BigDecimal sumByUsuarioAndCategoriaAndMesAndAno(@Param("usuario") Usuario usuario, @Param("categoria") Categoria categoria, @Param("mes") int mes, @Param("ano") int ano);

    // Relatórios: soma por mês agrupada (retorna [mes, total])
    @Query("SELECT MONTH(l.data), COALESCE(SUM(l.valor), 0) FROM Lancamento l WHERE l.usuario = :usuario AND YEAR(l.data) = :ano AND l.tipo = :tipo GROUP BY MONTH(l.data)")
    List<Object[]> sumPorMesNoAno(@Param("usuario") Usuario usuario, @Param("ano") int ano, @Param("tipo") TipoTransacao tipo);

    // Relatórios: top categorias num período (retorna [categoria, total])
    @Query("SELECT l.categoria, COALESCE(SUM(l.valor), 0) FROM Lancamento l WHERE l.usuario = :usuario AND MONTH(l.data) = :mes AND YEAR(l.data) = :ano AND l.tipo = :tipo GROUP BY l.categoria ORDER BY SUM(l.valor) DESC")
    List<Object[]> topCategoriasPorMes(@Param("usuario") Usuario usuario, @Param("mes") int mes, @Param("ano") int ano, @Param("tipo") TipoTransacao tipo);

    // Relatórios: soma total por tipo num período
    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM Lancamento l WHERE l.usuario = :usuario AND MONTH(l.data) = :mes AND YEAR(l.data) = :ano AND l.tipo = :tipo")
    BigDecimal sumPorTipoEPeriodo(@Param("usuario") Usuario usuario, @Param("mes") int mes, @Param("ano") int ano, @Param("tipo") TipoTransacao tipo);
}
