package com.tuempresa.tracking;

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v21.services.ConversionUploadServiceClient;
import com.google.ads.googleads.v21.services.ClickConversion;
import com.google.ads.googleads.v21.services.UploadClickConversionsResponse;
import com.google.auth.oauth2.UserCredentials; 

import java.util.Collections;

public class GoogleAdsTest {
    public static void main(String[] args) {
        try {
            // 1. Construimos las credenciales manualmente (Bypass del archivo ads.properties)
            String clientId = System.getenv("GOOGLE_ADS_CLIENT_ID");
String clientSecret = System.getenv("GOOGLE_ADS_CLIENT_SECRET");
String refreshToken = System.getenv("GOOGLE_ADS_REFRESH_TOKEN");

UserCredentials credentials = UserCredentials.newBuilder()
        .setClientId(clientId)
        .setClientSecret(clientSecret)
        .setRefreshToken(refreshToken)
        .build();

            // 2. Construimos el cliente inyectando directamente los IDs críticos
            GoogleAdsClient googleAdsClient = GoogleAdsClient.newBuilder()
                    .setCredentials(credentials)
                    .setDeveloperToken("U9ryAeajsJ2EQwP8psbhjQ")
                    .setLoginCustomerId(2350541056L) 
                    .build();

            // 3. Ejecutamos la petición hacia la Sandbox
            try (ConversionUploadServiceClient client =
                 googleAdsClient.getVersion21().createConversionUploadServiceClient()) {

                String customerId = "1002190594"; // La cuenta hija donde cae la conversión
                String conversionActionId = "7505096121";

                String resourceName = "customers/" + customerId + "/conversionActions/" + conversionActionId;

                ClickConversion conversion = ClickConversion.newBuilder()
                        .setConversionAction(resourceName)
                        .setConversionDateTime("2026-02-17 21:00:00-03:00")
                        .setGclid("EAIaIQobChMIrL3Z-P_h_AIVAh-tBh0_RAAsEAAYASAAEgI_v_D_BwE")
                        .setConversionValue(10.0)
                        .setCurrencyCode("USD")
                        .build();

                UploadClickConversionsResponse response =
                        client.uploadClickConversions(
                                customerId,
                                Collections.singletonList(conversion),
                                true); // partial_failure = true

                System.out.println("🚀 ¡PIPELINE CONECTADO! Respuesta: " + response);
            }

        } catch (Exception e) {
            System.err.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}