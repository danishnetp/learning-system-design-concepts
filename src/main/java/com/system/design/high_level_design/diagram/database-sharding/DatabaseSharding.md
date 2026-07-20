# High-Level Design: Database Sharding and Replication

```mermaid
flowchart LR
    App[Application Service] --> Router[Shard Router or Proxy]

    subgraph S0[Shard 0: userIds 0-33%]
        P0[(Primary DB 0)]
        R0A[(Replica DB 0a)]
        R0B[(Replica DB 0b)]
    end

    subgraph S1[Shard 1: userIds 33-66%]
        P1[(Primary DB 1)]
        R1A[(Replica DB 1a)]
    end

    subgraph S2[Shard 2: userIds 66-100%]
        P2[(Primary DB 2)]
        R2A[(Replica DB 2a)]
    end

    Router --> P0
    Router --> P1
    Router --> P2
    Router --> R0A
    Router --> R1A
    Router --> R2A

    P0 --> R0A
    P0 --> R0B
    P1 --> R1A
    P2 --> R2A
```

## Notes

- Writes and strong reads usually go to primaries.
- Eventual reads can go to replicas.
- Common sharding options: `Hash`, `Range`, `Directory`, `Geo`.
- Cross-shard joins and resharding need careful design.

