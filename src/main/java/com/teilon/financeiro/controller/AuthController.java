package com.teilon.financeiro.controller;

import com.teilon.financeiro.dto.AlterarSenhaRequest;
import com.teilon.financeiro.dto.AuthResponse;
import com.teilon.financeiro.dto.EsqueciSenhaRequest;
import com.teilon.financeiro.dto.LoginRequest;
import com.teilon.financeiro.dto.RegisterRequest;
import com.teilon.financeiro.dto.ResetSenhaRequest;
import com.teilon.financeiro.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Registro e login de usuários")
public class AuthController {

    private final AuthService authService;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${jwt.expiration-ms:2592000000}")
    private long jwtExpirationMs;

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.registrar(request);
        adicionarCookieAuth(response, authResponse.token());
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/login")
    @Operation(summary = "Login e obtenção do token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        adicionarCookieAuth(response, authResponse.token());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar link de recuperação de senha por email")
    public ResponseEntity<Void> esqueciSenha(@Valid @RequestBody EsqueciSenhaRequest request) {
        authService.solicitarRecuperacaoSenha(request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir senha usando token recebido por email")
    public ResponseEntity<Void> resetarSenha(@Valid @RequestBody ResetSenhaRequest request) {
        authService.resetarSenha(request.token(), request.novaSenha());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/change-password")
    @Operation(summary = "Alterar senha do usuário autenticado")
    public ResponseEntity<Void> alterarSenha(
            @Valid @RequestBody AlterarSenhaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        authService.alterarSenha(userDetails.getUsername(), request.senhaAtual(), request.novaSenha());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    @Operation(summary = "Encerrar sessão e limpar cookie de autenticação")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie expiredCookie = ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSecure ? "None" : "Lax")
                .secure(cookieSecure)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ping")
    @Operation(summary = "Health check para pré-aquecer o servidor")
    public ResponseEntity<Void> ping() {
        return ResponseEntity.noContent().build();
    }

    private void adicionarCookieAuth(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("auth_token", token)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMillis(jwtExpirationMs))
                .sameSite(cookieSecure ? "None" : "Lax")
                .secure(cookieSecure)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
