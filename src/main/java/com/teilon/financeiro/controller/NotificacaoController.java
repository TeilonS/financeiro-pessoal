package com.teilon.financeiro.controller;

import com.teilon.financeiro.model.PushSubscription;
import com.teilon.financeiro.model.Usuario;
import com.teilon.financeiro.repository.PushSubscriptionRepository;
import com.teilon.financeiro.service.UsuarioService;
import com.teilon.financeiro.service.VapidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
@Tag(name = "Notificações", description = "Web Push — lembretes de atualização de gastos")
@SecurityRequirement(name = "bearerAuth")
public class NotificacaoController {

    private final PushSubscriptionRepository subscriptionRepo;
    private final VapidService vapidService;
    private final UsuarioService usuarioService;

    @GetMapping("/vapid-public-key")
    @Operation(summary = "Retorna a chave pública VAPID para registro de push no browser")
    public ResponseEntity<Map<String, String>> vapidPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", vapidService.getPublicKeyBase64()));
    }

    @PostMapping("/subscribe")
    @Operation(summary = "Registra uma subscrição push do browser")
    public ResponseEntity<Map<String, String>> subscribe(@RequestBody SubscribeRequest req) {
        Usuario usuario = usuarioService.getAutenticado();

        subscriptionRepo.findByEndpoint(req.endpoint()).ifPresentOrElse(
                existing -> {
                    existing.setP256dh(req.p256dh());
                    existing.setAuthKey(req.auth());
                    subscriptionRepo.save(existing);
                },
                () -> subscriptionRepo.save(PushSubscription.builder()
                        .usuario(usuario)
                        .endpoint(req.endpoint())
                        .p256dh(req.p256dh())
                        .authKey(req.auth())
                        .build())
        );

        return ResponseEntity.ok(Map.of("status", "subscribed"));
    }

    @DeleteMapping("/subscribe")
    @Transactional
    @Operation(summary = "Remove uma subscrição push do browser")
    public ResponseEntity<Map<String, String>> unsubscribe(@RequestBody Map<String, String> body) {
        String endpoint = body.get("endpoint");
        if (endpoint != null) {
            subscriptionRepo.deleteByEndpoint(endpoint);
        }
        return ResponseEntity.ok(Map.of("status", "unsubscribed"));
    }

    record SubscribeRequest(String endpoint, String p256dh, String auth) {}
}
