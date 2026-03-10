package com.tuempresa.tracking.service;

import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.tuempresa.tracking.model.StripeEventRecord;
import com.tuempresa.tracking.repository.StripeEventRepository;
import com.tuempresa.tracking.service.integration.GoogleAdsService;
import com.tuempresa.tracking.service.integration.PipedriveCRMServiceImpl;
import com.tuempresa.tracking.service.integration.MetaCapiService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TrackingRouterService {

    private final StripeEventRepository repository;
    private final GoogleAdsService googleAdsService;
    private final PipedriveCRMServiceImpl pipedriveService;
    private final MetaCapiService metaCapiService;

    public TrackingRouterService(StripeEventRepository repository, 
                                GoogleAdsService googleAdsService, 
                                PipedriveCRMServiceImpl pipedriveService,
                                MetaCapiService metaCapiService) {
        this.repository = repository;
        this.googleAdsService = googleAdsService;
        this.pipedriveService = pipedriveService;
        this.metaCapiService = metaCapiService;
    }

    public void routeEvent(Event event) {
        if ("checkout.session.completed".equals(event.getType())) {
            
            StripeObject dataObject = event.getDataObjectDeserializer().getObject().orElse(null);
            
            if (dataObject instanceof Session) {
                Session session = (Session) dataObject;
                
                String email = (session.getCustomerDetails() != null) ? session.getCustomerDetails().getEmail() : "sin_email@stripe.com";
                double amount = (session.getAmountTotal() != null) ? session.getAmountTotal() / 100.0 : 0.0;
                
                System.out.println(">>> [SRE MONITOR] Procesando venta de: " + email);

                // 1. Guardar en Base de Datos (Neon)
                StripeEventRecord record = new StripeEventRecord(email, amount, "PAID");
                record.setCreatedAt(LocalDateTime.now());
                record.setSessionId(session.getId());

                if (session.getMetadata() != null) {
                    record.setProductId(session.getMetadata().get("product_id"));
                    record.setGclid(session.getMetadata().get("gclid"));
                    record.setFbclid(session.getMetadata().get("fbclid"));
                }
                repository.save(record);

                // 2. Pipedrive (Nombre real: registrarVenta)
                try {
                    pipedriveService.registrarVenta(email, amount);
                } catch (Exception e) {
                    System.err.println("⚠️ [PIPEDRIVE ERROR]: " + e.getMessage());
                }

                // 3. Google Ads (Nombre real: sendOfflineConversion)
                if (record.getGclid() != null && !record.getGclid().isEmpty()) {
                    try {
                        googleAdsService.sendOfflineConversion(record.getGclid(), amount);
                    } catch (Exception e) {
                        System.err.println("⚠️ [GOOGLE ADS ERROR]: " + e.getMessage());
                    }
                }

                // 4. Meta CAPI (Nombre real: sendPurchaseEvent)
                if (record.getFbclid() != null && !record.getFbclid().isEmpty()) {
                    try {
                        metaCapiService.sendPurchaseEvent(email, amount, record.getFbclid());
                    } catch (Exception e) {
                        System.err.println("⚠️ [META ERROR]: " + e.getMessage());
                    }
                }
            }
        }
    }
}