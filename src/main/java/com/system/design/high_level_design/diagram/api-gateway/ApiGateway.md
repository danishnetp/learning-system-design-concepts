# High-Level Design: API Gateway

```mermaid
flowchart LR
    Mobile[Mobile Client]
    Web[Web Client]
    Third[Third-Party Client]

    subgraph GW[API Gateway]
        SSL[SSL Termination]
        Auth[Authentication and AuthZ]
        RL[Rate Limiter]
        Router[Request Router and Path Rewriting]
        Agg[Request Aggregator]
        CB[Circuit Breaker]
        Transform[Response Transformer]
        Log[Logging and Tracing]
        Cache[Gateway Cache]
    end

    subgraph MS[Microservices]
        US[User Service]
        OS[Order Service]
        PS[Payment Service]
        PRS[Product Service]
        NS[Notification Service]
    end

    IDP[Identity Provider]
    SR[Service Registry]

    Mobile --> GW
    Web --> GW
    Third --> GW

    SSL --> Auth --> RL --> Router --> Agg --> CB
    Auth --> IDP
    Router --> SR
    CB --> US
    CB --> OS
    CB --> PS
    CB --> PRS
    CB --> NS
    GW -. telemetry .-> Log
```

## Responsibilities

- SSL/TLS termination
- Authentication and authorization
- Rate limiting and throttling
- Routing and load balancing
- Request/response transformation
- Circuit breaking and fallback
- Caching and observability

