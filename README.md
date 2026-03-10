# Ecommerce Attribution Engine

**Webhook-driven multi-channel attribution backend** built with Java 21 and Spring Boot.  
Processes Stripe conversion events in real time and propagates them to Google Ads, Meta CAPI, Pipedrive CRM and PostgreSQL.

> Built and deployed independently as part of the No-Country S02-26 hackathon.  
> Backend architecture, implementation and GCP Cloud Run deployment by **Matías Martínez**.

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![GCP Cloud Run](https://img.shields.io/badge/GCP-Cloud_Run-4285F4?style=flat&logo=google-cloud&logoColor=white)](https://cloud.google.com/run)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-316192?style=flat&logo=postgresql&logoColor=white)](https://neon.tech)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)](https://www.docker.com)

---

## Problema que resuelve

Las empresas de ecommerce pierden visibilidad de sus conversiones cuando dependen únicamente del pixel del navegador — bloqueadores de anuncios, iOS 14+ y navegación privada eliminan hasta el 40% de los eventos.

Este sistema implementa **server-side tracking**: recibe el evento de Stripe directamente en el backend y lo propaga a todas las plataformas de marketing desde el servidor, garantizando trazabilidad completa independientemente del comportamiento del cliente.

---

## Arquitectura

```
Usuario → Stripe Checkout → Webhook POST
                                │
                    ┌───────────▼───────────┐
                    │   Webhook Controller   │
                    │  (Idempotency check)   │
                    └───────────┬───────────┘
                                │
                    ┌───────────▼───────────┐
                    │  TrackingRouterService │
                    │  (Orquestador central) │
                    └──┬────┬────┬────┬─────┘
                       │    │    │    │
              ┌────────▼┐ ┌─▼──┐ ┌▼──────────┐ ┌▼──────────┐
              │PostgreSQL│ │Meta│ │ Google Ads │ │ Pipedrive  │
              │  (Neon)  │ │CAPI│ │  (gRPC)   │ │    CRM     │
              └──────────┘ └────┘ └───────────┘ └───────────┘
```

**Patrón:** Webhook-Driven Orchestration con aislamiento de fallos — si una integración externa falla, las demás continúan ejecutándose de forma independiente.

---

## Principios SRE aplicados

| Principio | Implementación |
|---|---|
| **Aislamiento de fallos** | Cada integración externa se ejecuta de forma desacoplada |
| **Idempotencia** | Verificación por `session_id` antes de procesar — previene duplicados en reintentos de Stripe |
| **Persistencia garantizada** | La conversión se registra en PostgreSQL incluso si alguna integración externa falla |
| **Observabilidad** | Prefijos de log `[SRE MONITOR]`, `[SRE SUCCESS]`, `[SRE DEBUG]` para trazabilidad |
| **Data Seeding** | Endpoint de generación de datos históricos para dashboards de Grafana |

---

## Stack tecnológico

- **Runtime:** Java 21 (Virtual Threads ready)
- **Framework:** Spring Boot 3.x
- **Base de datos:** PostgreSQL en Neon.tech (serverless)
- **Integraciones:** Stripe SDK, Google Ads SDK v21 (gRPC), Meta Conversions API, Pipedrive REST API
- **Infraestructura:** Docker + GCP Cloud Run
- **Build:** Maven

---

## API Reference

### Webhook de Stripe
```
POST /api/v1/webhooks/stripe
Header: Stripe-Signature: <webhook_secret>
```

Procesa eventos `checkout.session.completed` y `charge.succeeded`.  
Metadata requerida en Stripe: `gclid`, `campaign`, `source`, `product_id`.

### Admin — Data Seeder
```
GET /api/v1/admin/seed?days=30
```
Genera datos sintéticos históricos para testing y visualización en Grafana.

---

## Configuración

Todas las credenciales se inyectan via variables de entorno. Copiá `.env.example` como base:

```bash
cp .env.example .env.local
```

Variables requeridas:

```env
DB_URL=jdbc:postgresql://...
DB_USERNAME=
DB_PASSWORD=
STRIPE_API_KEY=
STRIPE_WEBHOOK_SECRET=
META_PIXEL_ID=
META_ACCESS_TOKEN=
GOOGLE_ADS_CLIENT_ID=
GOOGLE_ADS_CLIENT_SECRET=
GOOGLE_ADS_REFRESH_TOKEN=
GOOGLE_ADS_DEVELOPER_TOKEN=
GOOGLE_ADS_CUSTOMER_ID=
PIPEDRIVE_API_TOKEN=
PIPEDRIVE_DOMAIN=
```

---

## Ejecución local

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

Para testing de webhooks locales, instalá [Stripe CLI](https://stripe.com/docs/stripe-cli):

```bash
stripe listen --forward-to localhost:8080/api/v1/webhooks/stripe
```

---

## Deploy en GCP Cloud Run

```bash
# Build de imagen Docker
docker build -t ecommerce-attribution-engine .

# Tag y push a Google Container Registry
docker tag ecommerce-attribution-engine gcr.io/[PROJECT_ID]/ecommerce-attribution-engine
docker push gcr.io/[PROJECT_ID]/ecommerce-attribution-engine

# Deploy en Cloud Run
gcloud run deploy ecommerce-attribution-engine \
  --image gcr.io/[PROJECT_ID]/ecommerce-attribution-engine \
  --platform managed \
  --region us-central1 \
  --set-env-vars DB_URL=...,STRIPE_API_KEY=...
```

---

## Decisiones arquitecturales (ADR)

**¿Por qué Cloud Run y no una VM?**  
Cloud Run escala a cero cuando no hay tráfico y escala automáticamente ante picos de webhooks. Para un sistema event-driven donde el tráfico es impredecible, esto elimina el costo de infraestructura idle y simplifica el operaciones.

**¿Por qué orquestación y no coreografía?**  
Con un orquestador central (`TrackingRouterService`) tenemos visibilidad completa del flujo y podemos implementar fallback y retry por integración. La coreografía habría requerido un message broker adicional (Kafka/Pub-Sub) que excedía el scope del MVP.

**¿Por qué idempotencia por session_id?**  
Stripe reintenta webhooks automáticamente ante timeouts. Sin idempotencia, una conversión podría registrarse múltiples veces en Google Ads y Meta, distorsionando el ROAS. El `session_id` es único por checkout y permite detectar y descartar duplicados.

---

## Autor

**Matías Martínez** — SRE & Backend Engineer  
[linkedin.com/in/ingmarma](https://linkedin.com/in/ingmarma) · [github.com/ingmarma](https://github.com/ingmarma)
