package com.teilon.financeiro.service;

import com.teilon.financeiro.dto.AuthResponse;
import com.teilon.financeiro.dto.LoginRequest;
import com.teilon.financeiro.dto.RegisterRequest;
import com.teilon.financeiro.model.Usuario;
import com.teilon.financeiro.repository.UsuarioRepository;
import com.teilon.financeiro.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthResponse registrar(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("Email já cadastrado");
        }

        var usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .build();

        usuarioRepository.save(usuario);

        var token = jwtUtil.gerarToken(usuario.getEmail());
        return new AuthResponse(token, usuario.getEmail(), usuario.getNome());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        var usuario = usuarioRepository.findByEmail(request.email()).orElseThrow();
        var token = jwtUtil.gerarToken(usuario.getEmail());
        return new AuthResponse(token, usuario.getEmail(), usuario.getNome());
    }
}
