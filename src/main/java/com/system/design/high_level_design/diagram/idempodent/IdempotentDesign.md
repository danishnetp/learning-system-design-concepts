# High-Level Design: Idempotent API / Service

```mermaid
flowchart LR
    Client --> Gateway[API Gateway]
    Gateway --> Filter[Idempotency Filter or Middleware]
    Filter --> KeyStore[(Idempotency Store)]

    KeyStore -- DUPLICATE --> Filter
    Filter -- Return cached response --> Client

    KeyStore -- NEW --> Filter
    Filter --> Service[Business Logic Service]
    Service --> Downstream[Payment or Order Processor]
    Downstream --> DB[(Primary Database)]
    Downstream --> Service
    Service --> KeyStore
    Service --> Client
```

## Idempotency Rules

- Client sends `Idempotency-Key` header (usually UUID).
- Key is unique per logical operation.
- Store key + request hash + response + status + expiry (TTL).
- Duplicate key returns previously stored response safely.

