package com.teilon.financeiro.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CotacaoService {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Busca o preço atual de um ativo da B3 via brapi.dev.
     * Retorna null se o ticker for inválido ou a API estiver indisponível.
     */
    @SuppressWarnings("unchecked")
    public BigDecimal buscarPreco(String ticker) {
        if (ticker == null || ticker.isBlank()) return null;
        String url = "https://brapi.dev/api/quote/" + ticker.toUpperCase().trim();
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<?, ?> body = response.getBody();
            if (body == null) return null;

            List<Map<?, ?>> results = (List<Map<?, ?>>) body.get("results");
            if (results == null || results.isEmpty()) return null;

            Object price = results.get(0).get("regularMarketPrice");
            if (price == null) return null;

            return new BigDecimal(price.toString());
        } catch (Exception e) {
            log.warn("Erro ao buscar cotação para {}: {}", ticker, e.getMessage());
            return null;
        }
    }
}
