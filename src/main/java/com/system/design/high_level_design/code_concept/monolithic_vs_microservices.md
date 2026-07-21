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

#### Why async for Workflows
Long-running processes that span multiple services should not be blocked on a synchronous call chain. A single slow or failing downstream step would stall the entire caller.

- **How it works:** each step publishes an event on completion; the next service picks it up and continues.
- **Pattern:** Saga (choreography or orchestration)
- **Benefit:** each step is independently retryable, rollbackable via compensating actions, and observable.
- **Example:** Place Order → Reserve Inventory → Charge Payment → Send Confirmation. Each step is a separate async event, not a nested sync call chain. If payment fails, a compensating event releases the inventory reservation.

#### Why async for Fanout
A single event needs to trigger multiple independent consumers. Using sync calls would require the publisher to know and wait for all subscribers.

- **How it works:** publisher emits one event to a topic/exchange; multiple consumers subscribe independently.
- **Pattern:** Pub/Sub (Kafka topic, SNS, RabbitMQ fanout exchange)
- **Benefit:** publisher is decoupled from consumers; new consumers can be added without changing the publisher.
- **Example:** `OrderCreated` event is published once. `InventoryService`, `NotificationService`, `AnalyticsService`, and `FraudService` each consume it independently and at their own pace.

#### Why async for Eventual Consistency
Cross-service data synchronization cannot always use a distributed transaction. Async messaging allows each service to update its own state eventually, in its own time.

- **How it works:** source service writes its local state and publishes an event (via outbox or CDC). Consuming services update their own read models or state on receipt.
- **Pattern:** Outbox Pattern, Change Data Capture (CDC), idempotent consumers
- **Benefit:** each service owns its data; no tight coupling or 2-phase commit required.
- **Example:** When `Order` is placed, `OrderService` updates its DB. `SearchIndexService` receives the event and updates its search index asynchronously. The index may lag slightly but will converge — stale reads are acceptable here, but the DB is always correct.

#### When async is the wrong choice
- when the caller needs the result immediately to continue (for example auth check, price lookup at checkout)
- when ordering and exactly-once delivery semantics are complex and not worth the overhead
- when the team does not yet have the tooling/observability to debug async failures

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
Choose data ownership and consistency model for each service or shared boundary.

---

#### Pattern 1: Database Per Service

##### What it is
Each microservice owns its own dedicated database. No other service can access it directly. All cross-service data exchange happens via APIs or events.

```
OrderService      -->  [Orders DB]
PaymentService    -->  [Payments DB]
InventoryService  -->  [Inventory DB]
UserService       -->  [Users DB]
```

##### Advantages
- **Full ownership:** service schema and storage can evolve independently.
- **Technology fit:** each service can choose the best DB type (relational, document, graph, time-series).
- **Fault isolation:** one service's DB failure does not directly impact others.
- **Independent scaling:** scale DB read/write capacity per service based on actual load.
- **No schema coupling:** changes to one service's schema do not break others.

##### Disadvantages
- **No cross-service joins:** querying data across services requires aggregation at the API or event level.
- **Eventual consistency:** cross-service data must be synchronized via events (Outbox, CDC, Saga).
- **Data duplication:** some data fields may be replicated across service DBs as read projections.
- **Operational overhead:** more databases to provision, monitor, backup, and manage.
- **Distributed transaction complexity:** cross-service writes need Saga or Outbox patterns instead of ACID.

##### When to use
- when teams own distinct domains and need autonomy over schema evolution
- when services have very different data access patterns or scale requirements
- when the system is large enough to justify per-service infrastructure cost

##### When NOT to use
- small systems where one shared DB is easier to operate
- when frequent cross-service queries make data duplication impractical
- when team is not yet ready to operate multiple databases

##### Cross-service consistency techniques
| Technique | How it works | Best for |
|---|---|---|
| Outbox Pattern | Write event to outbox table in same local TX; relay publishes async | Reliable at-least-once event publishing |
| Saga Pattern | Chain of local TXs with compensating actions on failure | Long-running cross-service workflows |
| Change Data Capture (CDC) | Stream DB log changes as events (Debezium) | Real-time downstream sync without app changes |
| Idempotent consumers | Consumers safely handle duplicate events | All async event-driven integration |

##### Interview example
`OrderService` writes `orders` table and `outbox_events` in one transaction. Relay publishes `OrderCreated` event. `InventoryService` and `PaymentService` consume independently and update their own DBs.

---

#### Pattern 2: Shared Database

##### What it is
Multiple services share the same database instance and sometimes the same tables or schemas.

```
OrderService  --|
PaymentService--|--> [Shared DB]
InventoryService|
```

##### Advantages
- **Simple cross-service queries:** services can join tables directly.
- **ACID transactions across services:** a single DB transaction can span multiple service operations.
- **Lower operational cost:** one DB to provision, monitor, and back up.
- **Easier to start:** no need to manage multiple DB instances in early stage.

##### Disadvantages
- **Tight coupling:** schema changes by one service can break others.
- **Single point of failure:** DB outage affects all services simultaneously.
- **Scaling bottleneck:** all services compete for the same DB connection pool and I/O.
- **No independent deployability:** schema migrations require coordination across teams.
- **Violates service autonomy:** any service can read or modify another service's data.

##### When to use
- very early product stage where simplicity matters more than autonomy
- read-heavy reporting services that legitimately need to join multiple service datasets
- internal tooling or admin dashboards where coupling is acceptable
- migration interim state — temporarily shared DB while splitting a monolith

##### When NOT to use
- production microservices needing independent scaling and deployability
- when services are owned by different teams
- when schema change velocity is high and cross-team coordination is a bottleneck

##### Interview example
Admin reporting service reads from `orders`, `payments`, and `inventory` tables in one shared DB. Acceptable here because it is read-only and not part of the transactional service boundary.

---

#### Database Strategy Comparison

| Dimension | Database Per Service | Shared Database |
|---|---|---|
| Service autonomy | High | Low |
| Schema independence | Full | None — changes are shared |
| Cross-service queries | Hard (API/event aggregation) | Easy (SQL join) |
| ACID across services | Requires Saga/Outbox | Native DB transaction |
| Fault isolation | High | Low — single point of failure |
| Scalability | Per-service scale | Shared bottleneck |
| Operational cost | High | Low |
| Best fit | Production microservices | Early stage or reporting |

#### Interview-ready answer
"I prefer database-per-service in microservices because it gives teams ownership, allows independent scaling, and avoids schema coupling. Cross-service consistency is handled via Outbox and Saga patterns. I only allow shared DB in early-stage products or for read-only reporting layers where autonomy is not yet a concern."

### Monolith perspective
- often one shared DB
- easier joins and transactions
- risk: schema changes affect many modules as system grows

### Key tradeoffs
- stronger consistency vs autonomy
- query convenience vs ownership boundaries
- operational simplicity vs independent scalability

### Interview example
`Order` writes order state and outbox event in same transaction; event is published asynchronously for downstream services consuming their own separate DBs.

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

---

## 8) Microservices Design Patterns

Key patterns for designing and migrating microservices systems, with advantages, disadvantages, and when to use each.

---

### 8.1 Decomposition Pattern

#### What it is
Decomposition is the strategy for splitting a monolith (or designing a greenfield system) into microservices by identifying the right service boundaries.

#### Two main approaches

**By Business Capability**
Each service owns one distinct business function.
- `OrderService` manages order lifecycle
- `PaymentService` manages payment processing
- `InventoryService` manages stock and reservations

**By Subdomain (Domain-Driven Design)**
Use bounded contexts from DDD. Each subdomain maps to one or more services with its own ubiquitous language.

#### Advantages
- **Alignment with business:** services map to how the business thinks about capabilities.
- **Team ownership:** one team owns one bounded domain end-to-end.
- **Independent deployability:** boundaries reduce cross-team coupling.
- **Focused responsibility:** each service has one clear reason to change.

#### Disadvantages
- **Wrong cut causes tight coupling:** badly chosen boundaries create chatty inter-service calls.
- **Data duplication:** each service may need its own projection of shared data.
- **Hard to get right early:** domain boundaries are often discovered, not designed upfront.
- **Over-decomposition risk:** too many tiny services create unnecessary operational overhead.

#### When to use
- when a monolith has clearly distinct domains that change at different rates
- when different teams own different parts of the system
- when specific domains have clearly different scaling needs

#### When NOT to use
- at project start before domain knowledge is stable
- when the team is small and cannot support multiple service deployments
- when splitting would require complex distributed transactions across every workflow

#### Which is better in which condition
| Condition                                             | Better approach                                 |
|-------------------------------------------------------|-------------------------------------------------|
| Early product, unclear domain                         | Modular monolith with clear internal boundaries |
| Clear domain boundaries, multiple teams               | Decompose by business capability or subdomain   |
| Single hot feature (for example search, notification) | Extract that one service first                  |
| Complex cross-domain transactions everywhere          | Stay monolith, defer decomposition              |

---

### 8.2 Strangler Pattern (Strangler Fig)

#### What it is
The Strangler Fig pattern incrementally replaces a monolith by routing specific traffic to new microservices while the monolith handles the rest. The new system grows around the old one until the monolith is fully replaced or no longer needed.

#### How it works
1. Place a routing layer (API Gateway, proxy, or façade) in front of the monolith.
2. Implement one feature/domain in a new microservice.
3. Route that feature's traffic to the new service.
4. Repeat for other features until the monolith is retired.

```
Client
  |
  v
[API Gateway / Façade]
  |              |
  v              v
[New Service]  [Legacy Monolith]
(feature A)    (features B, C, D)
```

#### Advantages
- **Zero big-bang risk:** no cutover day; traffic shifts gradually.
- **Rollback safety:** routing can revert to monolith instantly.
- **Incremental validation:** each extracted service is tested in production before expanding.
- **Business continuity:** monolith continues serving while migration happens.

#### Disadvantages
- **Dual maintenance period:** old and new code coexist for a long time.
- **Routing complexity:** routing logic must stay in sync with migration state.
- **Shared state problem:** new service and monolith may need to access the same DB during transition.
- **Risk of partial migration stall:** teams may stop halfway and maintain a permanently fragmented hybrid.

#### When to use
- migrating a large live monolith to microservices with no acceptable downtime
- when new services need to be validated in production before full cutover
- when a strangled feature has a clean external API boundary

#### When NOT to use
- greenfield projects (no monolith to strangle)
- when monolith has no clean API layer to intercept traffic
- when the monolith's DB is too entangled for service-level separation

#### Which is better in which condition
| Condition                                    | Recommendation                                    |
|----------------------------------------------|---------------------------------------------------|
| Live production monolith with active traffic | Strangler — safest migration path                 |
| New system with no legacy                    | Direct microservices design — no need to strangle |
| Monolith with tight DB coupling              | Modularize DB first, then strangle                |
| Team wants quick migration                   | Strangler + aggressive routing shift per domain   |

---

### 8.3 SAGA Pattern

#### What it is
SAGA manages long-running, distributed business transactions that span multiple microservices. Instead of a two-phase commit (2PC), each service executes a local transaction and publishes an event. If a step fails, compensating transactions undo completed steps.

#### Two styles

**Choreography Saga**
Each service listens for events and reacts autonomously. No central coordinator.

```
OrderService  ---> [OrderCreated event]
                         |
               InventoryService ---> [InventoryReserved event]
                                           |
                               PaymentService ---> [PaymentCharged event]
                                                         |
                                           NotificationService ---> [EmailSent]
```

On failure:
```
PaymentService fails ---> [PaymentFailed event]
                                |
               InventoryService listens ---> [InventoryReleased compensating action]
```

**Orchestration Saga**
A central saga orchestrator calls each service step-by-step and handles failures.

```
SagaOrchestrator
  --> calls InventoryService.reserve()
  --> calls PaymentService.charge()
  --> calls NotificationService.send()
  --> on failure: calls InventoryService.release()
```

#### Advantages
- **No distributed transaction:** avoids 2PC and its locking/availability cost.
- **Resilient workflows:** each step is independently retryable.
- **Audit trail:** each event is a record of what happened.
- **Loose coupling:** in choreography, services do not know about each other.

#### Disadvantages
- **Compensating transactions are not rollbacks:** side effects (email sent, charge attempted) may not be undoable.
- **Hard to debug choreography:** event chains are implicit; hard to trace overall flow.
- **Idempotency required:** each consumer must handle duplicate events safely.
- **Eventual consistency:** intermediate states are visible between steps.
- **Orchestration adds a single point of complexity:** orchestrator logic can become a god object.

#### Choreography vs Orchestration
| Aspect              | Choreography             | Orchestration                  |
|---------------------|--------------------------|--------------------------------|
| Central coordinator | No                       | Yes                            |
| Coupling            | Loose                    | Moderate                       |
| Visibility of flow  | Hard                     | Easy (orchestrator owns state) |
| Debuggability       | Complex (event tracing)  | Easier                         |
| Best for            | Simple, autonomous flows | Complex, observable workflows  |

#### When to use
- any long-running workflow spanning multiple services (for example order fulfillment, user onboarding)
- when 2PC is not feasible (distributed DBs, different tech stacks)
- when partial failures need defined compensating behavior

#### When NOT to use
- when strict ACID transactions are mandatory and a single DB is viable
- when workflows are trivially short (one service, one action)
- when team has no observability/tooling to trace saga state

#### Which is better in which condition
| Condition                                   | Recommendation                                     |
|---------------------------------------------|----------------------------------------------------|
| Simple 2–3 step workflow, loose services    | Choreography                                       |
| Complex 5+ step workflow needing visibility | Orchestration                                      |
| Strict correctness requirement              | Consider if 2PC on a single DB is feasible instead |
| Team new to distributed patterns            | Orchestration (explicit, easier to debug)          |

---

### 8.4 CQRS (Command Query Responsibility Segregation)

#### What it is
CQRS separates the **write model (Command)** from the **read model (Query)**. Commands mutate state; queries read state from an optimized projection. The two sides can use different data models, storage, and scaling strategies.

#### How it works
```
Client
  |
  |--- Command (create order, update profile) ---> [Command Handler] ---> [Write DB]
  |                                                                              |
  |                                                              [Event / CDC published]
  |                                                                              |
  |--- Query (get order, search) ------------> [Query Handler] <--- [Read Model / Projection DB]
```

#### Advantages
- **Optimized reads:** read models are denormalized for specific query patterns (fast, no joins).
- **Independent scaling:** read-heavy and write-heavy workloads scale separately.
- **Domain clarity:** commands carry intent and validation logic; queries are pure reads.
- **Event history:** combined with Event Sourcing, full audit trail of all state changes.
- **Multiple projections:** same command stream feeds multiple tailored read views.

#### Disadvantages
- **Eventual consistency between write and read side:** read model may lag behind writes.
- **Complexity:** two models to maintain, synchronize, and test.
- **Operational overhead:** separate stores, separate deployments, projection rebuild logic.
- **Overkill for simple domains:** most CRUD apps do not need this separation.

#### When to use
- high read/write ratio with different query patterns than write patterns
- complex domain with rich commands and multiple tailored read views
- when read performance matters and denormalized projections help significantly
- alongside Event Sourcing for full audit/replay capability

#### When NOT to use
- simple CRUD where reads and writes have the same shape
- small teams without capacity to maintain two models
- when eventual consistency between write and read sides is not acceptable for the use case

#### CQRS vs Standard CRUD
| Aspect              | Standard CRUD       | CQRS                              |
|---------------------|---------------------|-----------------------------------|
| Model               | Single shared model | Separate command/query models     |
| Read optimization   | Limited             | Strong (denormalized projections) |
| Write optimization  | Shared              | Focused command model             |
| Consistency         | Immediate           | Eventual (read side lags)         |
| Complexity          | Low                 | High                              |
| Best for            | Simple domains      | Complex, read-heavy domains       |

#### Which is better in which condition
| Condition                                  | Recommendation                                              |
|--------------------------------------------|-------------------------------------------------------------|
| Simple create/read/update/delete only      | Standard CRUD                                               |
| Different query shapes than write shapes   | CQRS                                                        |
| Heavy read traffic, needs fast projections | CQRS + read replica/cache                                   |
| Audit/replay required                      | CQRS + Event Sourcing                                       |
| Small team, early stage product            | Start with CRUD; introduce CQRS on bottlenecked domain only |

---

### 8.5 Pattern Comparison Summary

| Pattern       | Problem it solves                                          | Key tradeoff                         | Best fit                                                    |
|---------------|------------------------------------------------------------|--------------------------------------|-------------------------------------------------------------|
| Decomposition | How to split system into right-sized services              | Wrong cut causes coupling            | When domain is stable and teams own clear capabilities      |
| Strangler Fig | How to migrate live monolith without big-bang cutover      | Dual maintenance period              | Live monolith migration with active traffic                 |
| SAGA          | Cross-service long-running workflows without 2PC           | Compensations are not true rollbacks | Any multi-step distributed workflow                         |
| CQRS          | Separate read and write models for performance and clarity | Eventual consistency + complexity    | Complex domains with heavy reads and different query shapes |

```

