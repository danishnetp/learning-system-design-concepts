# High-Level Design: Rate Limiting

```mermaid
flowchart LR
    Client --> Filter[Rate Limit Filter]
    Filter --> Engine[Algorithm Engine]
    Engine --> Redis[(Redis Counter Store)]
    Redis --> Engine

    Engine -->|ALLOW| Filter
    Engine -->|DENY| Filter

    Filter -->|Allowed| App[Application Service]
    App --> Client
    Filter -->|429 Too Many Requests| Client
```

## Common Algorithms

- `Token Bucket`: allows bursts up to bucket size.
- `Leaky Bucket`: smooth output at fixed drain rate.
- `Fixed Window`: simple but bursty at boundaries.
- `Sliding Window Log`: accurate, memory-heavy.
- `Sliding Window Counter`: balanced accuracy and cost.

## Typical Headers

- `X-RateLimit-Limit`
- `X-RateLimit-Remaining`
- `X-RateLimit-Reset`
- `Retry-After`

