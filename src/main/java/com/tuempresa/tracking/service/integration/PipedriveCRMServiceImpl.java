package com.tuempresa.tracking.service.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import java.util.Map;

@Service
public class PipedriveCRMServiceImpl implements CRMIntegrationService {

    // Reemplazamos @Slf4j por la instanciación nativa y directa de Java
    private static final Logger log = LoggerFactory.getLogger(PipedriveCRMServiceImpl.class);

    private final WebClient webClient;
    @Value("${pipedrive.api.token}")
    private String apiToken;

    public PipedriveCRMServiceImpl(WebClient.Builder builder, @Value("${pipedrive.domain}") String domain) {
        this.webClient = builder.baseUrl("https://" + domain + ".pipedrive.com/api/v2").build();
    }

    @Override
    public void registrarVenta(String email, double monto) {
        log.info(">>> [SRE DEBUG] Intentando conexión directa con Token: {}...", apiToken.substring(0, 5) + "****");

        Map<String, Object> deal = Map.of(
                "title", "Venta Stripe: " + email,
                "value", monto,
                "currency", "USD",
                "status", "open");
        try {

                Map res = this.webClient.post()
                    .uri("/deals")
                    .header("x-api-token", apiToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(deal)
                    .retrieve()
                    .onStatus(status -> status.isError(),
                            response -> response.bodyToMono(String.class).flatMap(errorBody -> {
                                log.error(">>> [Pipedrive Error] Código: {} - Cuerpo: {}", response.statusCode(),
                                        errorBody);
                                return Mono.error(new RuntimeException("Error en Pipedrive API"));
                            }))
                    .bodyToMono(Map.class)
                    .block(); 

        if (res != null && res.containsKey("data")) {
            log.info(">>> [SUCCESS] Trato creado en Pipedrive con ID: {}", ((Map) res.get("data")).get("id"));
        } else {
            log.warn(">>> [WARNING] Pipedrive respondió, pero no devolvió un ID de Trato válido.");
        }
        } catch (Exception e) {
            log.error("ERROR: " + e.getMessage());
        }
    }
}