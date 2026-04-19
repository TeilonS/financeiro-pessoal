package com.teilon.financeiro.repository;

import com.teilon.financeiro.model.Investimento;
import com.teilon.financeiro.model.SnapshotMensal;
import com.teilon.financeiro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SnapshotMensalRepository extends JpaRepository<SnapshotMensal, Long> {
    List<SnapshotMensal> findAllByInvestimentoAndUsuarioOrderByAnoDescMesDesc(Investimento investimento, Usuario usuario);
    Optional<SnapshotMensal> findByInvestimentoAndMesAndAnoAndUsuario(Investimento investimento, Integer mes, Integer ano, Usuario usuario);

    @Query("SELECT s FROM SnapshotMensal s WHERE s.usuario = :usuario AND (s.ano * 100 + s.mes) = " +
           "(SELECT MAX(s2.ano * 100 + s2.mes) FROM SnapshotMensal s2 WHERE s2.investimento = s.investimento AND s2.usuario = :usuario)")
    List<SnapshotMensal> findUltimosSnapshotsPorUsuario(@Param("usuario") Usuario usuario);
}
