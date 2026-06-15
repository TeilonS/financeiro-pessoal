package com.teilon.financeiro.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${resend.api-key:}")
    private String apiKey;

    @Value("${resend.from:Financeiro Intel <onboarding@resend.dev>}")
    private String from;

    public void enviarEmailRecuperacaoSenha(String toEmail, String nome, String link) {
        if (apiKey.isBlank()) {
            log.info("[DEV] RESEND_API_KEY não configurado. Link de recuperação para {}: {}", toEmail, link);
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
            "from", from,
            "to", List.of(toEmail),
            "subject", "Recuperação de senha — Financeiro Intel",
            "html", montarHtml(nome, link)
        );

        try {
            restTemplate.postForEntity(
                "https://api.resend.com/emails",
                new HttpEntity<>(body, headers),
                Map.class
            );
        } catch (Exception e) {
            log.error("Erro ao enviar email para {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Não foi possível enviar o email. Tente novamente em instantes.");
        }
    }

    private String montarHtml(String nome, String link) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family:sans-serif;background:#f4f4f5;margin:0;padding:32px">
              <div style="max-width:480px;margin:0 auto;background:#fff;border-radius:16px;padding:40px;box-shadow:0 2px 16px rgba(0,0,0,.08)">
                <div style="text-align:center;margin-bottom:32px">
                  <div style="display:inline-flex;width:48px;height:48px;background:#f97316;border-radius:14px;align-items:center;justify-content:center;font-weight:700;font-size:22px;color:#fff">F</div>
                  <h2 style="margin:8px 0 0;color:#18181b;font-size:20px">Financeiro Intel</h2>
                </div>
                <h3 style="color:#18181b;margin-top:0">Recuperação de senha</h3>
                <p style="color:#71717a;line-height:1.6">Olá, <strong>%s</strong>. Recebemos uma solicitação para redefinir a senha da sua conta.</p>
                <p style="color:#71717a;line-height:1.6">Clique no botão abaixo. O link é válido por <strong>1 hora</strong>.</p>
                <div style="text-align:center;margin:32px 0">
                  <a href="%s" style="background:#f97316;color:#fff;padding:14px 32px;border-radius:12px;text-decoration:none;font-weight:700;font-size:15px;display:inline-block">
                    Redefinir minha senha
                  </a>
                </div>
                <p style="color:#a1a1aa;font-size:13px">Se você não solicitou a redefinição, ignore este email.</p>
                <hr style="border:none;border-top:1px solid #f4f4f5;margin:24px 0">
                <p style="color:#a1a1aa;font-size:12px;text-align:center;margin:0">Financeiro Intel</p>
              </div>
            </body>
            </html>
            """.formatted(nome, link);
    }
}
