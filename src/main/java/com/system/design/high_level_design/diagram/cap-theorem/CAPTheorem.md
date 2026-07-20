# High-Level Design: CAP Theorem Decision Matrix

```mermaid
flowchart TB
    C[Consistency]
    A[Availability]
    P[Partition Tolerance]

    C --- A
    A --- P
    P --- C

    D1[1. Identify business risk]
    D2[2. Assume partition]
    D3[3. Choose behavior under split]
    CP[CP path: prioritize correctness]
    AP[AP path: prioritize uptime and latency]

    D1 --> D2 --> D3
    D3 --> CP
    D3 --> AP

    CPX[CP examples: payments, inventory, balances]
    APX[AP examples: feeds, analytics, recommendations]
    MIX[Mixed strategy: commands CP, queries AP]

    CPX --> CP
    APX --> AP
    MIX --> D3

    CTRL[(Controls: quorum, idempotency, retries, reconciliation)]
    CP --> CTRL
    AP --> CTRL
```

## Interview One-Liner

Under partition, choose `CP` or `AP` per workflow based on business risk, then apply controls like quorum, idempotency, retries, and reconciliation.

