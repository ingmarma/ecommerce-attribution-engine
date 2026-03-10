package com.tuempresa.tracking.service.integration;

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v21.services.ClickConversion;
import com.google.ads.googleads.v21.services.ConversionUploadServiceClient;
import com.google.ads.googleads.v21.services.UploadClickConversionsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Service
public class GoogleAdsService {

    private final GoogleAdsClient googleAdsClient;

    @Value("${google.ads.customerId}")
    private String customerId;

    @Value("${google.ads.conversionActionId}")
    private String conversionActionId;

    public GoogleAdsService(GoogleAdsClient googleAdsClient) {
        this.googleAdsClient = googleAdsClient;
    }

    public void sendOfflineConversion(String gclid, double amount) {
        if (gclid == null || gclid.isEmpty()) {
            System.err.println("⚠️ [GOOGLE ADS] No se envió conversión: GCLID nulo o vacío.");
            return;
        }

        // Formato exacto: yyyy-MM-dd HH:mm:ssZZZZZ (Ej: 2026-03-03 01:17:07-03:00)
        // Usamos OffsetDateTime para asegurar que el offset sea el de Paraguay (-03:00)
        String conversionTime = OffsetDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX"));
        
        System.out.println(">>> [DEBUG SRE] GCLID recibido: " + gclid);
        System.out.println(">>> [DEBUG SRE] Enviando fecha exacta: " + conversionTime);

        String resourceName = "customers/" + customerId + "/conversionActions/" + conversionActionId;

        try (ConversionUploadServiceClient client = 
                googleAdsClient.getVersion21().createConversionUploadServiceClient()) {

            ClickConversion conversion = ClickConversion.newBuilder()
                    .setConversionAction(resourceName)
                    .setConversionDateTime(conversionTime)
                    .setGclid(gclid)
                    .setConversionValue(amount)
                    .setCurrencyCode("USD")
                    .build();

            UploadClickConversionsResponse response = client.uploadClickConversions(
                    customerId, Collections.singletonList(conversion), true);

            if (response.hasPartialFailureError()) {
                // Esto nos dirá el error exacto si Google vuelve a quejarse
                System.err.println("❌ [GOOGLE ADS ERROR DETAIL] " + response.getPartialFailureError().getMessage());
            } else {
                System.out.println("🚀 [GOOGLE ADS SUCCESS] Conversión aceptada para GCLID: " + gclid);
            }

        } catch (Exception e) {
            System.err.println("❌ [SPRING ERROR] Error crítico en la llamada a Google Ads: " + e.getMessage());
        }
    }
}