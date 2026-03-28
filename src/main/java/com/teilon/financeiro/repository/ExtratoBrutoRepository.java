package com.teilon.financeiro.repository;

import com.teilon.financeiro.model.ExtratoBruto;
import com.teilon.financeiro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExtratoBrutoRepository extends JpaRepository<ExtratoBruto, Long> {
    List<ExtratoBruto> findAllByUsuarioOrderByDataImportacaoDesc(Usuario usuario);
}
