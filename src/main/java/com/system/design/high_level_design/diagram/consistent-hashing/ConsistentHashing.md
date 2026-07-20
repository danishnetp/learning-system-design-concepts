# High-Level Design: Consistent Hashing

```mermaid
flowchart LR
    Client[Client or Request Router] --> CHR[Consistent Hash Router]

    subgraph Cluster[Cache or DB Cluster]
        SA[Server A\nvirtual nodes: 30, 150, 270]
        SB[Server B\nvirtual nodes: 60, 180, 330]
        SC[Server C\nvirtual nodes: 90, 210, 30]
    end

    CHR --> SA
    CHR --> SB
    CHR --> SC
```

## Key Ideas

- Hash keys onto a circular ring.
- Route each key to the first server clockwise on the ring.
- Use virtual nodes for better distribution.
- Adding/removing a server remaps only a fraction of keys (about `1/N`).

