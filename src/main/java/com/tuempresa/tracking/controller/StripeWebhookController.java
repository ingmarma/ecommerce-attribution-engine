package com.tuempresa.tracking.controller;

import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.tuempresa.tracking.repository.TransactionRepository;
import com.tuempresa.tracking.service.integration.GoogleAdsService;
import com.tuempresa.tracking.service.integration.MetaCapiService;
import com.tuempresa.tracking.service.integration.PipedriveCRMServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/stripe")
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private GoogleAdsService googleAdsService;
    @Autowired
    private MetaCapiService metaCapiService;
    @Autowired
    private PipedriveCRMServiceImpl pipedriveService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            // 1. Validamos firma
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            System.out.println(">>> [WEBHOOK RECEIVED] Tipo de evento: " + event.getType() + " | ID: " + event.getId()
                    + " | Payload: " + payload);

            if ("checkout.session.completed".equals(event.getType())) {
                // Extraemos el objeto genérico
                // com.stripe.model.StripeObject stripeObject =
                // event.getDataObjectDeserializer().getObject().orElse(null);
                // System.out.println(stripeObject != null ? ">>> [WEBHOOK INFO] Objeto
                // deserializado: " + stripeObject.getClass().getSimpleName() : ">>> [WEBHOOK
                // INFO] No se pudo deserializar el objeto.");
                com.stripe.model.EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
                StripeObject stripeObject = null;
                if (dataObjectDeserializer.getObject().isPresent()) {
                    stripeObject = dataObjectDeserializer.getObject().get();
                    System.out.println(
                            ">>> [WEBHOOK INFO] Objeto deserializado: " + stripeObject.getClass().getSimpleName());
                }

                else {
                    // Deserialization failed, probably due to an API version mismatch.
                    // Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
                    // instructions on how to handle this case, or return an error here.
                    System.err.println(
                            ">>> [WEBHOOK ERROR] No se pudo deserializar el objeto del evento. Posible incompatibilidad de versión de API.");
                }

                // Verificamos si es una Session
                if (stripeObject instanceof Session) {
                    Session session = (Session) stripeObject;
                    System.out.println(">>> [SRE INFO] Procesando Session ID: " + session.getId());
                    procesarPagoAcelerado(session);
                }

                // Si procesamos el pago, respondemos Success
                return ResponseEntity.ok("Success: Processed");
            }

            // 2. Si el evento NO es de pago, igual respondemos 200 para que Stripe no
            // reintente
            return ResponseEntity.ok("Success: Event ignored");

        } catch (Exception e) {
            System.err.println(">>> [WEBHOOK ERROR] " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    private void procesarPagoAcelerado(Session session) {
        String sessionId = session.getId();
        String customerEmail = (session.getCustomerDetails() != null) ? session.getCustomerDetails().getEmail()
                : "no-email@test.com";
        double amount = (session.getAmountTotal() != null) ? session.getAmountTotal() / 100.0 : 0.0;

        String gclid = (session.getMetadata() != null) ? session.getMetadata().get("gclid") : null;
        String fbclid = (session.getMetadata() != null) ? session.getMetadata().get("fbclid") : null;

        // 1. LOCK LÓGICO: Actualización atómica en Base de Datos
        int updated = transactionRepository.markAsPaidIfPending(sessionId, customerEmail);

        System.out.println(sessionId + " - Intentando marcar como PAID. Filas afectadas: " + updated + " | Email: "
                + customerEmail + " | GCLID: " + gclid + " | FBCLID: " + fbclid);

        if (updated == 1) {
            System.out.println(">>> [DB SUCCESS] Transacción " + sessionId + " marcada como PAID de forma segura.");

            // 2. PROCESAMIENTO ASÍNCRONO BLINDADO
            // --- BLOQUE PIPEDRIVE CRM ---
            try {
                pipedriveService.registrarVenta(customerEmail, amount);
            } catch (Exception e) {
                System.err.println(">>> [CRM ERROR] Falló Pipedrive: " + e.getMessage());
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println(">>> [ASYNC WORKER] Iniciando tareas de marketing para: " + customerEmail);

                // --- BLOQUE GOOGLE ADS ---
                try {
                    if (gclid != null && !gclid.isBlank()) {
                        googleAdsService.sendOfflineConversion(gclid, amount);
                        System.out.println(">>> [ADS] Conversión enviada.");
                    } else {
                        System.out.println(">>> [ADS SKIP] No hay GCLID para enviar.");
                    }
                } catch (Exception e) {
                    System.err.println(
                            ">>> [ADS ERROR] Falló Google Ads, pero el sistema continúa. Causa: " + e.getMessage());
                }

            });
            CompletableFuture<Void> futuroMeta = CompletableFuture.runAsync(() -> {// --- BLOQUE META CAPI ---
                try {
                    if (fbclid != null && !fbclid.isBlank()) {
                        metaCapiService.sendPurchaseEvent(customerEmail, amount, fbclid);
                        System.out.println(">>> [META] Evento enviado.");
                    }
                } catch (Exception e) {
                    System.err.println(">>> [META ERROR] Falló Meta CAPI: " + e.getMessage());
                }
            });

            CompletableFuture.allOf(future, futuroMeta).thenRun(() -> System.out.println("TAREA FINALIZADA"));
        } else {
            System.err.println(">>> [DB WARNING] No se pudo actualizar " + sessionId + ". No existe o ya estaba PAID.");
        }
    }
}