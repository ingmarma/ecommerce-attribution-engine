package com.tuempresa.tracking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stripe_events")
public class StripeEventRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;
    private String email;
    private Double amount;
    private String status;
    private String productId;
    private String gclid;
    private String fbclid;
    private String campaign;
    private String source;
    private LocalDateTime createdAt;

    // 1. Constructor vacío (Obligatorio para JPA)
    public StripeEventRecord() {}

    // 2. Constructor de 3 parámetros (Para TrackingRouterService)
    public StripeEventRecord(String email, Double amount, String status) {
        this.email = email;
        this.amount = amount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // 3. Constructor de 4 parámetros (Para DataSeederService)
    public StripeEventRecord(String email, Double amount, String status, LocalDateTime createdAt) {
        this.email = email;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    // --- GETTERS Y SETTERS MANUALES (Para evitar errores de Lombok) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getGclid() { return gclid; }
    public void setGclid(String gclid) { this.gclid = gclid; }
    public String getFbclid() { return fbclid; }
    public void setFbclid(String fbclid) { this.fbclid = fbclid; }
    public String getCampaign() { return campaign; }
    public void setCampaign(String campaign) { this.campaign = campaign; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}