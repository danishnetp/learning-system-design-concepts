# High-Level Design - Topic Map

```mermaid
flowchart TB
    subgraph HLD[High-Level Design Topics]
        subgraph TM[Traffic Management]
            LB[Load Balancing]
            AG[API Gateway]
            RL[Rate Limiting]
        end

        subgraph DL[Data Layer]
            C[Caching]
            DS[Database Sharding]
            CH[Consistent Hashing]
        end

        subgraph AM[Async and Messaging]
            MQ[Message Queue]
        end

        subgraph R[Reliability]
            CB[Circuit Breaker]
            ID[Idempotency]
        end
    end
```

## Notes

- `Load Balancing`: distributes traffic across multiple servers.
- `API Gateway`: centralized auth, routing, throttling, and aggregation.
- `Rate Limiting`: protects services from overuse and abuse.
- `Caching`: reduces DB load and latency.
- `Database Sharding`: partitions data across DB nodes.
- `Consistent Hashing`: minimizes key remapping when nodes change.
- `Message Queue`: decouples producers and consumers asynchronously.
- `Circuit Breaker`: prevents cascading failures.
- `Idempotency`: ensures retries do not duplicate effects.

