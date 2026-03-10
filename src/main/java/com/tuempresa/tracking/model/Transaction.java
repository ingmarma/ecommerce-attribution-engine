package com.tuempresa.tracking.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data 
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stripeSessionId;
    private String plan;
    private String email;    // Para guardar el mail que capture Stripe
    private String gclid;
    private String fbclid; 
    private String campaign; // Para "perfil_bio"
    private String source;   // Para "instagram"
    private String status;
    
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }
}