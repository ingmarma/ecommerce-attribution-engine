package com.tuempresa.tracking.config;

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.auth.oauth2.UserCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleAdsConfig {

    @Value("${google.ads.clientId}")
    private String clientId;

    @Value("${google.ads.clientSecret}")
    private String clientSecret;

    @Value("${google.ads.refreshToken}")
    private String refreshToken;

    @Value("${google.ads.developerToken}")
    private String developerToken;

    @Value("${google.ads.loginCustomerId}")
    private Long loginCustomerId;

    @Bean
    public GoogleAdsClient googleAdsClient() {
        // 1. Construimos las credenciales de manera programática
        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        // 2. Retornamos el cliente listo para ser inyectado en tus servicios
        return GoogleAdsClient.newBuilder()
                .setCredentials(credentials)
                .setDeveloperToken(developerToken)
                .setLoginCustomerId(loginCustomerId)
                .build();
    }
}
