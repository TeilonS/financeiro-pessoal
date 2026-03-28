package com.teilon.financeiro.service;

import com.teilon.financeiro.model.Usuario;
import com.teilon.financeiro.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public Usuario getAutenticado() {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }

    public BigDecimal getReservaEmergencia() {
        Usuario u = getAutenticado();
        return u.getReservaEmergencia() != null ? u.getReservaEmergencia() : BigDecimal.ZERO;
    }

    public BigDecimal atualizarReservaEmergencia(BigDecimal valor) {
        Usuario u = getAutenticado();
        u.setReservaEmergencia(valor != null ? valor : BigDecimal.ZERO);
        usuarioRepository.save(u);
        return u.getReservaEmergencia();
    }
}
