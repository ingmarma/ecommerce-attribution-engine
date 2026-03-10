package com.tuempresa.tracking.service.integration;

import com.tuempresa.tracking.model.StripeEventRecord;
import com.tuempresa.tracking.repository.StripeEventRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DataSeederService {
    private final StripeEventRepository repository;

    public DataSeederService(StripeEventRepository repository) {
        this.repository = repository;
    }

    public void seedHistoricalData(int days) {
        String[] campañas = {"BLACK_FRIDAY_2026", "SOCIAL_ADS_PROMO", "ORGANIC_SEARCH", "MVP_LAUNCH"};
        
        for (int i = 0; i < days; i++) {
            // Generamos registros con fechas decrementales
            StripeEventRecord record = new StripeEventRecord(
                "historico_" + i + "@test.com",
                (Math.random() * 50) + 100,
                "COMPLETED",
                LocalDateTime.now().minusDays(i) 
            );

            // --- AGREGAMOS METADATA PARA QUE EL DASHBOARD NO QUEDE VACÍO ---
            record.setSessionId("sess_hist_" + UUID.randomUUID().toString().substring(0, 8));
            record.setCampaign(campañas[i % campañas.length]); // Alterna entre campañas
            record.setSource(i % 2 == 0 ? "google" : "facebook");
            record.setGclid("GCLID_HIST_" + i + "_XYZ");
            
            repository.save(record);
        }
        System.out.println(">>> [SRE INFO] " + days + " días de datos con trazabilidad inyectados en Neon.");
    }
}
