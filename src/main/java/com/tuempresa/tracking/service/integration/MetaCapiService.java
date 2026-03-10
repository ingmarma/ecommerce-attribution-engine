package com.tuempresa.tracking.service.integration;

import com.tuempresa.tracking.dto.MetaEventDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@Service
public class MetaCapiService {
    private final WebClient webClient;

    @Value("${meta.pixel.id}")
    private String pixelId;

    @Value("${meta.access.token}")
    private String accessToken;

    public MetaCapiService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://graph.facebook.com/v19.0").build();
    }

    public void sendPurchaseEvent(String email, double amount, String fbclid) {
        MetaEventDTO.UserData userData = new MetaEventDTO.UserData();
        if (email != null) {
            userData.setEm(hashSha256(email));
        }

        if (fbclid != null) {
            userData.setFbc("fb.1." + (System.currentTimeMillis()/1000) + "." + fbclid);
        }
        userData.setClient_ip_address("127.0.0.1"); 

        MetaEventDTO.CustomData customData = new MetaEventDTO.CustomData();
        customData.setValue(amount);
        customData.setCurrency("USD");

        MetaEventDTO event = new MetaEventDTO();
        event.setEvent_name("Purchase");
        event.setEvent_time(System.currentTimeMillis() / 1000L);
        event.setAction_source("email");
        event.setUser_data(userData);
        event.setCustom_data(customData);

        // FLUJO DE WEBCLIENT ASÍNCRONO PARA ENVIAR EL EVENTO A META CAPI
        webClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/{pixelId}/events")
                .queryParam("access_token", accessToken)
                .queryParam("test_event_code", "TEST44672")
                .build(pixelId))
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("data", java.util.List.of(event)))
            .retrieve()
            .bodyToMono(String.class)
            .subscribe(
                res -> System.out.println(">>> [SRE SUCCESS] Meta CAPI procesÃ³ la venta: " + res),
                err -> System.err.println(">>> [SRE ERROR] Meta CAPI fallÃ³: " + err.getMessage())
            );
    }

    // Utilidad de Hashing SHA-256 (Obligatorio para Meta)
    private String hashSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al hashear datos", e);
        }
    }
}
