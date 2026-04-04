package com.teilon.financeiro.service;

import com.teilon.financeiro.model.PushSubscription;
import com.teilon.financeiro.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Envia Web Push sem payload (push vazio).
 * O Service Worker no frontend exibe a notificação com mensagem pré-definida.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final PushSubscriptionRepository subscriptionRepo;
    private final VapidService vapidService;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Envia um push vazio para todas as subscrições registradas.
     * O Service Worker exibe a mensagem recebida no campo `tag`.
     *
     * @param tag identificador da mensagem (ex: "dia1", "dia15")
     */
    public void enviarParaTodos(String tag) {
        List<PushSubscription> subs = subscriptionRepo.findAll();
        if (subs.isEmpty()) {
            log.info("Nenhuma subscrição push registrada — notificação ignorada.");
            return;
        }

        for (PushSubscription sub : subs) {
            try {
                enviar(sub, tag);
            } catch (Exception e) {
                log.warn("Falha ao enviar push para endpoint {}: {}", sub.getEndpoint(), e.getMessage());
                // Se o endpoint retornar 404/410 significa que foi expirado/removido
                if (e.getMessage() != null && e.getMessage().contains("41")) {
                    subscriptionRepo.deleteByEndpoint(sub.getEndpoint());
                    log.info("Subscrição expirada removida: {}", sub.getEndpoint());
                }
            }
        }
    }

    private void enviar(PushSubscription sub, String tag) throws Exception {
        URI uri    = URI.create(sub.getEndpoint());
        String origin = uri.getScheme() + "://" + uri.getHost();

        String jwt = vapidService.buildVapidJwt(origin);
        String vapidAuth = "vapid t=" + jwt + ",k=" + vapidService.getPublicKeyBase64();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", vapidAuth)
                .header("TTL", "2419200")
                .header("Urgency", "normal")
                .header("Topic", tag)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        if (status == 201 || status == 200) {
            log.debug("Push enviado com sucesso para {}", sub.getEndpoint());
        } else if (status == 404 || status == 410) {
            throw new RuntimeException("41" + status + " endpoint expirado");
        } else {
            log.warn("Push retornou status {} para {}", status, sub.getEndpoint());
        }
    }
}
