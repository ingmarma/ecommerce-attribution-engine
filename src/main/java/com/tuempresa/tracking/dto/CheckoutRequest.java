package com.tuempresa.tracking.dto;

public record CheckoutRequest(
    String productId,    // "STARTER" o "GROWTH"
    String paymentType,  // "ONETIME" o "RECURRING"
    String gclid,
    String fbclid,
    String campaign,
    String source,
    String successUrl,
    String cancelUrl
) {}