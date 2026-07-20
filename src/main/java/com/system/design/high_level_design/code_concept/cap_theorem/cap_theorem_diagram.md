rem# CAP Theorem - Simple Deployment View

```mermaid
flowchart LR
    App[Application]
    DB1[(DB1\nDeployed in India)]
    DB2[(DB2\nDeployed in US)]

    App --> DB1
    App --> DB2
```

## Meaning

- The `Application` is connected to two databases in different regions.
- This kind of layout is often used to discuss CAP tradeoffs during network partition.
- When the India and US sites cannot communicate reliably, the system must choose between consistency and availability.

