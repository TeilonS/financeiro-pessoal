package com.teilon.financeiro.scheduler;

import com.teilon.financeiro.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Envia lembretes de push nos dias 1 e 15 de cada mês às 08:00.
 * O backend precisa estar rodando no horário para disparar.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LembreteScheduler {

    private final PushNotificationService pushService;

    /** Dia 1 de cada mês às 08:00 — atualizar gastos da 2ª quinzena do mês anterior */
    @Scheduled(cron = "0 0 8 1 * *")
    public void lembreteInicioDeMes() {
        log.info("Disparando lembrete push — dia 1 do mês");
        pushService.enviarParaTodos("dia1");
    }

    /** Dia 15 de cada mês às 08:00 — atualizar gastos da 1ª quinzena */
    @Scheduled(cron = "0 0 8 15 * *")
    public void lembreteMetadeDeMes() {
        log.info("Disparando lembrete push — dia 15 do mês");
        pushService.enviarParaTodos("dia15");
    }
}
