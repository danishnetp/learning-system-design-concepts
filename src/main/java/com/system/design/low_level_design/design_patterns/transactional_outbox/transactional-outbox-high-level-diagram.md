# Transactional Outbox - High Level Diagram

This Mermaid diagram renders directly on GitHub and explains the full outbox flow.

```mermaid
flowchart LR
    C[Client] --> SVC[Order Service]

    subgraph DB[Database Transaction Boundary]
      O[(orders table)]
      OB[(outbox_events table)]
    end

    SVC -->|1. Write order| O
    SVC -->|2. Write outbox row PENDING| OB

    SVC -->|3. Return success| C

    subgraph Relay[Background Relay]
      R[Outbox Relay Worker]
    end

    OB -->|4. Poll pending rows| R

    subgraph Broker[Message Broker]
      MQ[[order-events topic]]
      DLQ[[DLQ]]
    end

    R -->|5. Publish event| MQ
    R -->|on success: mark PUBLISHED| OB
    R -->|on failure: retryCount++| OB
    R -->|after max retries| DLQ

    subgraph Consumers[Downstream Consumers]
      INV[Inventory Service]
      NOTIF[Notification Service]
      ANALYTICS[Analytics Service]
    end

    MQ --> INV
    MQ --> NOTIF
    MQ --> ANALYTICS

    INV -->|idempotent consume by eventId| INV
    NOTIF -->|idempotent consume by eventId| NOTIF
    ANALYTICS -->|idempotent consume by eventId| ANALYTICS

    classDef app fill:#e3f2fd,stroke:#1e88e5,color:#1f1f1f;
    classDef data fill:#fff8e1,stroke:#ffb300,color:#1f1f1f;
    classDef async fill:#fce4ec,stroke:#d81b60,color:#1f1f1f;

    class C,SVC,INV,NOTIF,ANALYTICS app;
    class O,OB data;
    class R,MQ,DLQ async;
```

## How to read this quickly

1. `Order Service` writes `orders` and `outbox_events` in the same DB transaction.
2. Client gets success as soon as transaction commits.
3. Relay publishes pending outbox events to broker asynchronously.
4. On failure, relay retries and finally routes poison events to `DLQ`.
5. Consumers process events idempotently (safe on duplicate delivery).

## Interview one-liner

"Transactional outbox avoids dual-write inconsistency by committing business data and event record atomically, then asynchronously publishing with retries and idempotent consumption."
