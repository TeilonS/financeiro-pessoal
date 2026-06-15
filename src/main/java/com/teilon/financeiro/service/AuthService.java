package com.teilon.financeiro.service;

import com.teilon.financeiro.dto.AuthResponse;
import com.teilon.financeiro.dto.LoginRequest;
import com.teilon.financeiro.dto.RegisterRequest;
import com.teilon.financeiro.model.PasswordResetToken;
import com.teilon.financeiro.model.Usuario;
import com.teilon.financeiro.repository.PasswordResetTokenRepository;
import com.teilon.financeiro.repository.UsuarioRepository;
import com.teilon.financeiro.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

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

        var usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Usuário não encontrado"));
        var token = jwtUtil.gerarToken(usuario.getEmail());
        return new AuthResponse(token, usuario.getEmail(), usuario.getNome());
    }

    @Transactional
    public void solicitarRecuperacaoSenha(String email) {
        var usuarioOpt = usuarioRepository.findByEmail(email);
        // Não revelar se email existe ou não (anti-enumeração)
        if (usuarioOpt.isEmpty()) return;

        Usuario usuario = usuarioOpt.get();

        // Invalida tokens anteriores do mesmo email
        tokenRepository.deleteByEmail(email);

        String token = UUID.randomUUID().toString();
        tokenRepository.save(PasswordResetToken.builder()
                .token(token)
                .email(email)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build());

        String link = frontendUrl + "/reset-password?token=" + token;
        emailService.enviarEmailRecuperacaoSenha(email, usuario.getNome(), link);
    }

    @Transactional
    public void resetarSenha(String token, String novaSenha) {
        var resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Link inválido ou expirado."));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Este link já foi utilizado.");
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Link expirado. Solicite um novo.");
        }

        var usuario = usuarioRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Usuário não encontrado"));

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}
