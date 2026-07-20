# CAP Theorem

## Core Properties

- **C - Consistency:** Every read receives the most recent write or an error.
- **A - Availability:** Every request receives a non-error response, without guarantee that it contains the most recent write.
- **P - Partition Tolerance:** The system continues to operate despite an arbitrary number of messages being dropped or delayed by the network between nodes.

## Important

**Desirable properties of distributed systems with replicated data.**

```mermaid
flowchart LR
    App[Application]
    DB1[(DB1\nDeployed in India)]
    DB2[(DB2\nDeployed in US)]

    App --> DB1
    App --> DB2
    DB1 <--> |replication| DB2
```

## Meaning 

- The `Application` is connected to two databases in different regions.
- `DB1` and `DB2` replicate data between each other.
- This kind of layout is often used to discuss CAP tradeoffs during network partition.
- When the India and US sites cannot communicate reliably, the system must choose between consistency and availability.

## CAP Circle View

<svg width="100%" viewBox="0 0 720 460" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="CAP Theorem Venn diagram">
  <defs>
    <style>
      .label { font: 600 26px sans-serif; fill: #1f2937; }
      .small { font: 500 20px sans-serif; fill: #1f2937; }
      .center { font: 700 24px sans-serif; fill: #111827; }
      .outline { stroke: #4b5563; stroke-width: 3; }
    </style>
  </defs>

  <rect width="100%" height="100%" fill="#ffffff"/>

  <circle cx="270" cy="170" r="135" fill="#60a5fa" fill-opacity="0.35" class="outline"/>
  <circle cx="450" cy="170" r="135" fill="#34d399" fill-opacity="0.35" class="outline"/>
  <circle cx="360" cy="305" r="135" fill="#f87171" fill-opacity="0.35" class="outline"/>

  <text x="225" y="160" class="label">C</text>
  <text x="470" y="160" class="label">A</text>
  <text x="350" y="360" class="label">P</text>

  <text x="295" y="235" text-anchor="middle" class="small">C ∩ A</text>
  <text x="315" y="280" text-anchor="middle" class="small">C ∩ P</text>
  <text x="405" y="280" text-anchor="middle" class="small">A ∩ P</text>
  <text x="360" y="235" text-anchor="middle" class="center">C ∩ A ∩ P</text>
</svg>

The center intersection represents the practical design space where a distributed system must choose how to behave under partition.
