# High-Level Design: Circuit Breaker Pattern

## State Machine

```mermaid
stateDiagram-v2
    [*] --> CLOSED
    CLOSED --> OPEN: Failure threshold reached
    OPEN --> HALF_OPEN: Reset timeout expires
    HALF_OPEN --> CLOSED: Trial request succeeds
    HALF_OPEN --> OPEN: Trial request fails
```

## Component Flow

```mermaid
flowchart LR
    Client --> CB[Circuit Breaker Proxy]
    CB --> SM[State Manager]
    SM -- CLOSED --> DS[Downstream Service]
    SM -- OPEN --> FB[Fallback Handler]
    SM -- HALF_OPEN --> DS

    DS -- SUCCESS --> CB
    DS -- FAILURE --> FC[Failure Counter]
    FC --> SM

    CB -. metrics .-> MET[(Metrics Store)]
    FB --> Client
```

## Typical Configuration

- `failureRateThreshold`: 50%
- `minimumCallCount`: 10
- `slidingWindowSize`: 60 sec
- `openStateDuration`: 30 sec
- `halfOpenPermittedCalls`: 3

