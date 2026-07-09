package com.teilon.financeiro.service;

import com.teilon.financeiro.dto.ProventoRequest;
import com.teilon.financeiro.dto.ProventoResponse;
import com.teilon.financeiro.model.Investimento;
import com.teilon.financeiro.model.Provento;
import com.teilon.financeiro.model.Usuario;
import com.teilon.financeiro.repository.InvestimentoRepository;
import com.teilon.financeiro.repository.ProventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProventoService {

    private final ProventoRepository proventoRepository;
    private final InvestimentoRepository investimentoRepository;
    private final UsuarioService usuarioService;

    @Transactional
    public ProventoResponse criar(ProventoRequest request) {
        Usuario usuario = usuarioService.getAutenticado();
        Investimento investimento = investimentoRepository.findByIdAndUsuario(request.investimentoId(), usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Investimento não encontrado"));

        Provento p = Provento.builder()
                .investimento(investimento)
                .usuario(usuario)
                .mes(request.mes())
                .ano(request.ano())
                .valor(request.valor())
                .build();

        return ProventoResponse.of(proventoRepository.save(p));
    }

    @Transactional(readOnly = true)
    public List<ProventoResponse> listar() {
        Usuario usuario = usuarioService.getAutenticado();
        return proventoRepository.findByUsuarioOrderByAnoDescMesDesc(usuario)
                .stream().map(ProventoResponse::of).toList();
    }

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = usuarioService.getAutenticado();
        Provento p = proventoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provento não encontrado"));
        proventoRepository.delete(p);
    }
}
