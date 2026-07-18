# Online Application HLD Diagram (GitHub Renderable)

This file uses a **Mermaid** diagram so it renders directly on GitHub after commit.

```mermaid
flowchart LR
    U[Web/Mobile User] --> CDN[CDN]
    CDN --> WAF[WAF + DDoS]
    WAF --> RL[Rate Limiter]
    RL --> GW[API Gateway / Load Balancer]

    GW --> AUTH[Auth Service]
    GW --> CAT[Catalog/Content Service]
    GW --> SEARCH[Search Service]
    GW --> ORDER[Order/Booking Service]
    GW --> PAY[Payment Service]
    GW --> NOTIF[Notification Service]

    CAT --> REDIS[(Redis Cache)]
    CAT --> RR[(Read Replica)]
    CAT --> SQL[(Primary SQL DB)]
    SEARCH --> IDX[(Search Index)]
    ORDER --> SQL
    PAY --> SQL

    ORDER --> OUTBOX[Write Outbox Row PENDING]
    OUTBOX --> RELAY[Outbox Relay Worker]
    RELAY --> MQ[[Message Broker]]
    RELAY --> SQL

    MQ --> WORKERS[Async Workers]
    WORKERS --> REDIS
    WORKERS --> IDX
    WORKERS --> SQL
    WORKERS --> DLQ[[DLQ]]

    PAY --> PGW[External Payment Gateway]
    NOTIF --> COMM[Email/SMS/Push Provider]
    WORKERS --> COMM

    SQL --> RR

    classDef edge fill:#e8f5e9,stroke:#4caf50,color:#1f1f1f;
    classDef app fill:#e3f2fd,stroke:#1e88e5,color:#1f1f1f;
    classDef data fill:#fff8e1,stroke:#ffb300,color:#1f1f1f;
    classDef async fill:#fce4ec,stroke:#d81b60,color:#1f1f1f;
    classDef ext fill:#e0f7fa,stroke:#00897b,color:#1f1f1f;

    class CDN,WAF,RL edge;
    class GW,AUTH,CAT,SEARCH,ORDER,PAY,NOTIF app;
    class SQL,RR,REDIS,IDX data;
    class OUTBOX,RELAY,MQ,WORKERS,DLQ async;
    class PGW,COMM ext;
```

## How to explain this in interview

1. Requests go through edge security (`CDN`, `WAF`, `Rate Limiter`) and then API gateway.
2. Services are stateless and horizontally scalable.
3. `SQL DB` is source of truth; `Redis` and `Read Replica` optimize read latency.
4. Write reliability uses **Transactional Outbox** + relay + message broker.
5. Async workers decouple slow tasks; failed events go to `DLQ`.
6. Search is handled by separate search index for text/filter heavy queries.

