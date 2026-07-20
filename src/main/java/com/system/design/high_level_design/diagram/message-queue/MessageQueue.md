# High-Level Design: Message Queue / Async Messaging

```mermaid
flowchart LR
    subgraph Producers
        P1[Order Service]
        P2[Payment Service]
        P3[User Service]
    end

    subgraph Broker[Message Broker]
        Q1[order.created topic or queue]
        Q2[payment.processed topic or queue]
        DLQ[Dead Letter Queue]
    end

    subgraph Consumers
        C1[Notification Service]
        C2[Inventory Service]
        C3[Analytics Service]
        C4[DLQ Processor or Alert Service]
    end

    Offset[(Consumer Offset Store)]

    P1 --> Q1
    P2 --> Q2
    P3 --> Q1

    Q1 --> C1
    Q1 --> C2
    Q2 --> C3

    Q1 --> DLQ
    Q2 --> DLQ
    DLQ --> C4

    C1 --> Offset
    C2 --> Offset
    C3 --> Offset
```

## Delivery Semantics

- `At-most-once`: may lose messages.
- `At-least-once`: may duplicate messages.
- `Exactly-once`: strongest but hardest.

