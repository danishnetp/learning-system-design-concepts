# Monolithic vs Microservices for HLD Interviews (Topic-Wise)

This document provides a topic-wise comparison of monolithic and microservices architecture with interview-focused explanations.

---

## 1) Quick Definitions

### Monolithic architecture
Single deployable application where most modules (UI, business logic, data access) are built and shipped together.

### Microservices architecture
System split into multiple independently deployable services, each owning a bounded business capability.

---

## 2) Advantages and Disadvantages of Microservices

### Advantages of microservices
- **Independent deployment:** deploy one service without redeploying whole system.
- **Scalability by hotspot:** scale only heavy services (for example payments, search).
- **Technology flexibility:** teams can choose best stack per service (with governance).
- **Fault isolation:** failure in one service does not always bring whole system down.
- **Team autonomy:** supports parallel development by smaller domain-aligned teams.

### Disadvantages of microservices
- **Operational complexity:** service discovery, config, retries, timeouts, tracing, mesh, CI/CD.
- **Distributed debugging:** failures are harder to trace across service boundaries.
- **Data consistency challenges:** cross-service transactions need saga/outbox patterns.
- **Network overhead:** inter-service calls add latency and failure points.
- **Higher platform cost:** infra, observability stack, and platform engineering effort.

### Interview one-liner
"Microservices improve scaling and team autonomy, but add distributed-system complexity that must be managed with strong platform and observability practices."

---

## 3) Advantages and Disadvantages of Monolithic

### Advantages of monolithic
- **Simple development start:** one codebase, one build, one deployment pipeline.
- **Easy local debugging:** single process and straightforward call stack.
- **Lower initial operational cost:** fewer moving parts and less orchestration.
- **Strong transactional simplicity:** easier ACID transactions in one DB boundary.
- **Good for small teams/early stage:** faster feature delivery initially.

### Disadvantages of monolithic
- **Tight coupling over time:** changes in one module can impact unrelated areas.
- **Scale inefficiency:** must scale whole app even if one module is hot.
- **Slower deployment velocity:** larger regression risk and long release cycles.
- **Tech lock-in:** harder to evolve stack for one domain without impacting all.
- **Blast radius:** a severe bug can affect the entire application.

### Interview one-liner
"Monoliths optimize for simplicity and speed early, but can struggle with scaling, release agility, and team autonomy as the system grows."

---

## 4) Topic-Wise Phases

## 4.1 Phase: Decomposition

### Goal
Split system by business capability (not by technical layer).

### Monolith perspective
- Keep modular boundaries internally (packages/modules).
- Prepare extraction candidates by domain ownership and change frequency.

### Microservices perspective
- Use bounded contexts (for example User, Order, Payment).
- Start with coarse-grained services, then split further only when needed.

### Common mistakes
- splitting too early into too many tiny services
- decomposing by CRUD tables instead of business capabilities

### Interview example
Extract `Notification` from monolith first because it has clear boundary, async behavior, and independent scaling needs.

---

## 4.2 Phase: Communication

### Goal
Define reliable interaction between components/services.

### Monolith perspective
- mostly in-process calls (low latency, easier transactions)
- fewer network failures within same process

### Microservices perspective
- synchronous APIs (HTTP/gRPC) for request-response paths
- asynchronous messaging (Kafka/RabbitMQ) for decoupling and resilience
- must implement timeouts, retries with backoff, circuit breakers, idempotency

### Key design choices
- choose sync only when immediate response is required
- prefer async for workflows, fanout, and eventual consistency

### Interview example
`Order` service publishes `OrderCreated` event; `Inventory` and `Notification` consume independently.

---

## 4.3 Phase: Observability

### Goal
Make failures and performance issues diagnosable in production.

### Monolith perspective
- centralized logs and metrics often simpler
- stack traces easier to correlate

### Microservices perspective
- require three pillars: logs, metrics, traces
- distributed tracing (trace-id/span-id) is mandatory
- need SLOs, alerting, service dashboards, error budgets

### Minimum observability checklist
- structured logs with correlation id
- RED metrics (rate, errors, duration)
- tracing across ingress -> services -> datastore/queue

### Interview example
When checkout latency spikes, trace shows bottleneck at payment adapter call instead of DB.

---

## 4.4 Phase: Database

### Goal
Choose data ownership and consistency model.

### Monolith perspective
- often one shared DB
- easier joins and transactions
- risk: schema changes affect many modules

### Microservices perspective
- database-per-service preferred for autonomy
- avoid direct cross-service DB access
- cross-service consistency via saga, outbox, CDC, idempotent consumers

### Key tradeoffs
- stronger consistency vs autonomy
- query convenience vs ownership boundaries

### Interview example
`Order` writes order state and outbox event in same transaction; event is published asynchronously for downstream services.

---

## 4.5 Phase: Integration

### Goal
Integrate safely with internal/external systems while preserving reliability.

### Monolith perspective
- integrations often embedded in one codebase
- easier at start, harder to isolate failures later

### Microservices perspective
- API Gateway for routing, auth, throttling
- anti-corruption layer for legacy systems
- contract testing (consumer-driven contracts) for service compatibility

### Integration resilience controls
- circuit breaker + fallback
- bulkheads
- retry with jitter
- dead-letter queues for failed async messages

### Interview example
Payment provider outage: breaker opens, order remains in pending state, retry workflow resumes later.

---

## 5) Migration Strategy (Monolith -> Microservices)

### Recommended path
1. Modularize monolith first.
2. Identify high-value extraction candidates.
3. Apply strangler pattern at API boundary.
4. Introduce event-driven integration where useful.
5. Build platform foundations (CI/CD, observability, service templates).
6. Migrate incrementally and measure outcomes.

### Do not migrate if
- team cannot support platform/ops overhead yet
- domain is small and scaling pressure is low
- release cadence and reliability are already acceptable

---

## 6) Decision Matrix

| Dimension | Monolithic | Microservices |
|---|---|---|
| Initial development speed | High | Medium |
| Operational complexity | Low | High |
| Independent scaling | Low | High |
| Team autonomy at scale | Low/Medium | High |
| Debugging simplicity | High | Medium/Low |
| Data consistency simplicity | High | Medium/Low |
| Blast radius control | Low | Medium/High |

---

## 7) Interview-Ready Summary

"I choose monolith when product/team is early and simplicity wins. I choose microservices when domain and scale justify independent deployability and scaling. During migration, I focus on decomposition, communication reliability, observability, data ownership, and integration resilience."

