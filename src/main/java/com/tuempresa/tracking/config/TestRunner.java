package com.tuempresa.tracking.config;

import com.tuempresa.tracking.service.integration.GoogleAdsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TestRunner implements CommandLineRunner {

    private final GoogleAdsService googleAdsService;

    // Spring inyecta tu servicio automáticamente acá
    public TestRunner(GoogleAdsService googleAdsService) {
        this.googleAdsService = googleAdsService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=========================================================");
        System.out.println("🔄 Iniciando prueba de humo: Enviando conversión a Google Ads...");
        
        // El GCLID duro de prueba que usamos para simular una conversión. En un caso real, esto vendría de tu sistema de tracking.
        String gclidPrueba = "EAIaIQobChMIrL3Z-P_h_AIVAh-tBh0_RAAsEAAYASAAEgI_v_D_BwE";
        double montoPrueba = 10.0;

        // Llamamos al servicio
        googleAdsService.sendOfflineConversion(gclidPrueba, montoPrueba);
        
        System.out.println("=========================================================");
    }
}
