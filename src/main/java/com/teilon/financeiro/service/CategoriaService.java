package com.teilon.financeiro.service;

import com.teilon.financeiro.dto.CategoriaRequest;
import com.teilon.financeiro.dto.CategoriaResponse;
import com.teilon.financeiro.model.Categoria;
import com.teilon.financeiro.repository.CategoriaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioService usuarioService;

    public List<CategoriaResponse> listar() {
        var usuario = usuarioService.getAutenticado();
        return categoriaRepository.findAllByUsuario(usuario).stream()
                .map(CategoriaResponse::de)
                .toList();
    }

    public CategoriaResponse criar(CategoriaRequest request) {
        var usuario = usuarioService.getAutenticado();
        Categoria pai = null;
        if (request.categoriaPaiId() != null) {
            pai = categoriaRepository.findByIdAndUsuario(request.categoriaPaiId(), usuario)
                    .orElseThrow(() -> new EntityNotFoundException("Categoria pai não encontrada"));
        }
        var categoria = Categoria.builder()
                .nome(request.nome())
                .tipo(request.tipo())
                .cor(request.cor())
                .categoriaPai(pai)
                .usuario(usuario)
                .build();
        return CategoriaResponse.de(categoriaRepository.save(categoria));
    }

    public void deletar(Long id) {
        var usuario = usuarioService.getAutenticado();
        var categoria = categoriaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
        categoriaRepository.delete(categoria);
    }

    // Usado internamente por outros services
    public Categoria buscarPorIdEUsuario(Long id) {
        var usuario = usuarioService.getAutenticado();
        return categoriaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
    }
}
