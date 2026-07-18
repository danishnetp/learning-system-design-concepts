# Online Application HLD Diagram

> Companion guide: [`design-hld-online-applications.md`](./design-hld-online-applications.md)

## How this file is synced with the main HLD guide

Use this file as the **deep-dive companion** to the main guide.

1. Read [`design-hld-online-applications.md`](./design-hld-online-applications.md) first for interview flow and structure.
2. Read this file second for detailed architecture explanation, scaling decisions, failure playbooks, and Q&A.

### Scope split (to avoid confusion)

- **Main guide (`design-hld-online-applications.md`)**
  - requirement clarification
  - capacity estimation
  - API-first discussion
  - core architecture and interview checklist
- **This diagram guide (`design-hld-online-applications-diagram.md`)**
  - component-by-component breakdown
  - keyword glossary (`WAF`, `DDoS`, `DLQ`, outbox, etc.)
  - marketplace-scale extension (Amazon/Flipkart style)
  - advanced scalability options, locking, replication, failures, Q&A

---

## How to explain this in interview

1. Requests go through edge security (`CDN`, `WAF`, `Rate Limiter`) and then API gateway.
2. Services are stateless and horizontally scalable.
3. `SQL DB` is source of truth; `Redis` and `Read Replica` optimize read latency.
4. Write reliability uses **Transactional Outbox** + relay + message broker.
5. Async workers decouple slow tasks; failed events go to `DLQ`.
6. Search is handled by separate search index for text/filter heavy queries.

---

## Keyword glossary (interview quick reference)

### Security and edge terms
- **CDN (Content Delivery Network):** Global edge servers that cache static content (images, JS, CSS, videos) close to users to reduce latency.
- **WAF (Web Application Firewall):** Filters malicious HTTP traffic (for example SQL injection, XSS, bot abuse) before it hits application servers.
- **DDoS (Distributed Denial of Service):** Attack where many machines flood your system with traffic; mitigation usually happens at CDN/WAF/network edge.
- **Rate Limiter:** Limits request rate per user/IP/token to prevent abuse and protect backend capacity.
- **TLS:** Encryption for traffic in transit (HTTPS).

### API and service terms
- **API Gateway:** Single entry layer for routing, auth checks, throttling, request validation, and observability.
- **Load Balancer (LB):** Distributes traffic across multiple service instances for availability and scale.
- **Stateless service:** Service that does not store session state in local memory, so any instance can serve any request.
- **Horizontal scaling:** Add more service instances instead of adding a larger single machine.

### Data and reliability terms
- **Primary SQL DB:** Main transactional database (source of truth) for strongly consistent writes.
- **Read Replica:** Read-only copy of primary DB for scaling read traffic.
- **Redis cache:** In-memory store for low-latency hot reads and temporary state.
- **Search Index:** Specialized index (like OpenSearch/Elasticsearch) for full-text search and filtering.
- **Transactional Outbox:** Pattern where business row and event row are written in one DB transaction.
- **Outbox Relay Worker:** Background job that publishes pending outbox events to broker and updates outbox status.
- **Message Broker:** Durable async transport (Kafka/RabbitMQ/SQS) between producers and consumers.
- **DLQ (Dead Letter Queue):** Queue for messages that repeatedly fail processing after max retries.
- **Idempotency:** Ability to safely retry requests/events without duplicate side effects.

### External integration terms
- **Payment Gateway:** External provider for payment authorization/capture/refund.
- **Email/SMS/Push provider:** External communication platform for notifications.

---

## Component-by-component explanation of this diagram

### 1) `U` - Web/Mobile User
- Starts request flow (browse, search, place order/booking).
- Expects low latency and reliable confirmation.

### 2) `CDN`
- Serves static content directly from edge.
- Reduces backend traffic and improves global response time.

### 3) `WAF + DDoS`
- Inspects incoming traffic and blocks suspicious patterns.
- Helps absorb attack traffic before it reaches core services.

### 4) `RL` - Rate Limiter
- Enforces per-user/IP quotas.
- Prevents abuse and controls traffic spikes.

### 5) `GW` - API Gateway / Load Balancer
- Routes request to correct service (`AUTH`, `CAT`, `ORDER`, etc.).
- Common place for auth checks, request IDs, tracing, and throttling.

### 6) Domain services (`AUTH`, `CAT`, `SEARCH`, `ORDER`, `PAY`, `NOTIF`)
- **`AUTH`:** Login/session/token validation.
- **`CAT`:** Product/content metadata APIs.
- **`SEARCH`:** Query, filter, ranking over index.
- **`ORDER`:** Core write workflow for transaction/order/booking.
- **`PAY`:** Payment state management + gateway integration.
- **`NOTIF`:** Notification orchestration.

### 7) Data stores (`SQL`, `RR`, `REDIS`, `IDX`)
- **`SQL`:** Strongly consistent transactional writes.
- **`RR`:** Scale read-heavy endpoints without overloading primary.
- **`REDIS`:** Cache hot data to improve P95 latency.
- **`IDX`:** Search-optimized views for text/filter/sort queries.

### 8) Transactional outbox path (`OUTBOX` -> `RELAY` -> `MQ`)
- `ORDER` writes business row and outbox event row (`PENDING`) in same transaction.
- `RELAY` polls pending outbox rows.
- On success: publishes event to `MQ` and marks outbox event as published.
- On failure: retries; after repeated failures route handling to `DLQ` path.

### 9) Async processing (`WORKERS`)
- Consumers process events independently.
- Update cache/index/projections without blocking user request path.
- Enables eventual consistency and better write throughput.

### 10) External systems (`PGW`, `COMM`)
- **`PGW`:** Real money operations; requires retries, idempotency keys, and reconciliation jobs.
- **`COMM`:** Send user-facing notifications (email/SMS/push).

### 11) `DLQ`
- Isolates poison/unprocessable events.
- Prevents infinite retries from harming healthy message flow.
- Used with alerting + replay tooling.

---

## End-to-end flows in this diagram

### Read flow
1. User request -> edge (`CDN/WAF/RL`) -> `GW`.
2. `CAT` serves from `REDIS` if available.
3. Fallback to `RR`/`SQL` and repopulate cache.
4. For search queries, `SEARCH` reads from `IDX`.

### Write flow
1. User action -> `ORDER`/`PAY` via `GW`.
2. Write to `SQL` (source of truth).
3. Also write outbox row (`OUTBOX`) in same transaction.
4. `RELAY` publishes to `MQ`.
5. `WORKERS` perform async tasks (notifications, index updates, projections).
6. Failed processing goes to `DLQ` after retry limit.

---

## Interview tips while presenting this diagram

- Start with **user request path**, then separate **read path** and **write path**.
- Explicitly mention where you need **strong consistency** (`SQL`, payments, inventory).
- Highlight where **eventual consistency** is acceptable (`IDX`, notifications, projections).
- Mention **idempotency** for both API retries and event consumers.
- Mention observability hooks: request IDs, tracing, queue lag, retry and DLQ metrics.

---

## Key architectural trade-offs (must discuss in interview)

### 1) Monolith vs microservices
- **Monolith pros:** simpler deploy/debug, fewer moving parts, faster early-stage delivery.
- **Monolith cons:** scaling and team ownership bottlenecks over time.
- **Microservices pros:** independent scaling/deployments, better team autonomy.
- **Microservices cons:** distributed complexity (network failures, tracing, consistency).
- **When to say in interview:** "For v1, I start modular monolith; split high-traffic bounded contexts later."

### 2) Strong consistency vs eventual consistency
- **Strong consistency pros:** prevents incorrect states in money/inventory workflows.
- **Strong consistency cons:** higher latency and tighter coupling.
- **Eventual consistency pros:** better availability and throughput with async processing.
- **Eventual consistency cons:** temporary stale reads and reconciliation needs.
- **When to say in interview:** "I keep strong consistency for order-payment-inventory writes, eventual consistency for search, analytics, and notifications."

### 3) Synchronous calls vs async messaging
- **Sync pros:** simple request-response model, immediate feedback.
- **Sync cons:** cascading failure risk and higher tail latency.
- **Async pros:** decoupling, burst handling, retry capability.
- **Async cons:** operational overhead (broker, retries, DLQ, out-of-order handling).
- **When to say in interview:** "Critical confirmation stays synchronous; non-critical side effects are async through broker."

### 4) Cache-heavy reads vs direct DB reads
- **Cache pros:** low latency, reduced DB load, better peak performance.
- **Cache cons:** invalidation complexity, stale data risk, memory cost.
- **Direct DB pros:** simpler correctness model.
- **Direct DB cons:** poor scalability for read-heavy traffic.
- **When to say in interview:** "I cache hot keys with TTL and event-based invalidation, but fall back to DB as source of truth."

### 5) SQL vs NoSQL for primary writes
- **SQL pros:** ACID transactions, mature indexing/joins, strong integrity.
- **SQL cons:** sharding complexity at very high scale.
- **NoSQL pros:** flexible schema and horizontal partitioning.
- **NoSQL cons:** weaker transactional guarantees (depending on engine).
- **When to say in interview:** "For transaction-heavy domains, SQL first; add NoSQL for specific high-scale access patterns if needed."

### 6) Read replica scaling vs DB sharding
- **Read replicas pros:** quick way to offload reads.
- **Read replicas cons:** replication lag, no write scaling.
- **Sharding pros:** write/read horizontal scale.
- **Sharding cons:** cross-shard queries/transactions become complex.
- **When to say in interview:** "Scale reads first with replicas; shard only when primary write or storage limits are reached."

### 7) Outbox pattern vs direct publish in request path
- **Outbox pros:** prevents dual-write loss and improves reliability.
- **Outbox cons:** extra table + relay + monitoring complexity.
- **Direct publish pros:** fewer components.
- **Direct publish cons:** message loss/inconsistency risk on partial failure.
- **When to say in interview:** "I prefer transactional outbox for correctness in business-critical events."

### 8) Single region vs multi-region
- **Single region pros:** simpler ops, lower consistency complexity.
- **Single region cons:** weaker disaster resilience and higher latency for distant users.
- **Multi-region pros:** better resilience and lower global latency.
- **Multi-region cons:** data conflict handling, higher cost and ops overhead.
- **When to say in interview:** "Start single region multi-AZ, then move to active-passive multi-region for DR, and active-active only if justified."

### 9) At-least-once vs exactly-once processing
- **At-least-once pros:** practical and robust in distributed systems.
- **At-least-once cons:** duplicates must be handled.
- **Exactly-once pros:** cleaner consumer semantics.
- **Exactly-once cons:** expensive/complex across service boundaries.
- **When to say in interview:** "I design for at-least-once delivery and enforce idempotent consumers."

### 10) Immediate deletion vs soft delete + retention
- **Immediate delete pros:** smaller storage footprint.
- **Immediate delete cons:** weak auditability and recovery.
- **Soft delete pros:** audit, rollback, compliance support.
- **Soft delete cons:** larger storage and data lifecycle jobs required.
- **When to say in interview:** "Use soft-delete for critical entities, then archive/tier old data by retention policy."

---

## 30-second trade-off summary to speak in interview

"My design optimizes for correctness on critical writes and scalability on reads. I use SQL and transactional outbox for reliable state changes, while caches, read replicas, and async workers reduce latency and absorb spikes. This introduces operational complexity, so I mitigate with observability, retries, DLQ, and idempotency. I would evolve from simpler deployment (single region, fewer services) to more distributed architecture only when scale and reliability targets demand it."

## Amazon/Flipkart-grade improvements (marketplace-scale design)

If you want to explain this as a large e-commerce marketplace (Amazon/Flipkart style), add these components and flows.

### What changes from generic HLD to marketplace HLD
- Add **Marketplace domain services**: Seller, Catalog ingestion, Pricing, Promotion, Inventory reservation, Cart, Checkout, Delivery promise, Returns.
- Add **Personalization layer**: Recommendations, ranking, user behavior features.
- Add **High-throughput event streaming** for clickstream, order events, and inventory events.
- Add **Search relevance pipeline**: ingestion, indexing, ranking, A/B experimentation.
- Add **Operational controls**: fraud/risk scoring, anti-abuse, observability-by-default, replay pipelines.

---

## Marketplace-scale architecture diagram

```mermaid
flowchart LR
    U[Customer App/Web] --> EDGE[CDN + WAF + Bot Shield + Rate Limit]
    EDGE --> GW[API Gateway]

    GW --> AUTH[Auth/Identity]
    GW --> CAT[Catalog API]
    GW --> SEARCH[Search API]
    GW --> CART[Cart Service]
    GW --> CHECKOUT[Checkout Service]
    GW --> ORDER[Order Service]
    GW --> PAY[Payment Service]
    GW --> USER[Profile/Address Service]

    subgraph MarketDomain[Marketplace Domain]
      SELLER[Seller Service]
      INGEST[Catalog Ingestion]
      PRICE[Pricing Service]
      PROMO[Promotion/Coupon Service]
      INV[Inventory Service]
      RESV[Inventory Reservation]
      ETA[Delivery Promise/ETA Service]
      SHIP[Shipping/Logistics Service]
      RETURN[Returns/Refund Service]
    end

    CAT --> PRICE
    CAT --> PROMO
    CHECKOUT --> ETA
    CHECKOUT --> RESV
    ORDER --> INV
    ORDER --> SHIP
    ORDER --> RETURN

    subgraph DataLayer[Data + Search + Cache]
      SQL[(Order/Payment SQL)]
      INVDB[(Inventory DB)]
      REDIS[(Redis Cache)]
      IDX[(Search Index)]
      OBJ[(Object Storage)]
      FEAT[(Feature Store)]
    end

    CAT --> REDIS
    SEARCH --> IDX
    ORDER --> SQL
    PAY --> SQL
    INV --> INVDB
    CAT --> OBJ

    subgraph Personalization[Relevance + Personalization]
      REC[Recommendation Service]
      RANK[Ranking Service]
      EXP[Experimentation/A-B Service]
    end

    GW --> REC
    SEARCH --> RANK
    REC --> FEAT
    RANK --> FEAT
    EXP --> RANK

    subgraph AsyncBackbone[Async Backbone]
      OUTBOX[Transactional Outbox]
      RELAY[Outbox Relay]
      MQ[[Event Bus / Kafka]]
      WORKERS[Async Workers]
      DLQ[[DLQ]]
      STREAM[Stream Processing]
    end

    ORDER --> OUTBOX
    OUTBOX --> RELAY --> MQ
    MQ --> WORKERS
    MQ --> STREAM
    WORKERS --> REDIS
    WORKERS --> IDX
    WORKERS --> INVDB
    WORKERS --> DLQ

    subgraph External[External Integrations]
      PGW[Payment Gateway]
      NOTIF[Email/SMS/Push Provider]
      LOGI[3P Logistics / Courier]
      FRAUD[Fraud Detection]
    end

    PAY --> PGW
    WORKERS --> NOTIF
    SHIP --> LOGI
    CHECKOUT --> FRAUD

    classDef edge fill:#e8f5e9,stroke:#4caf50,color:#1f1f1f;
    classDef svc fill:#e3f2fd,stroke:#1e88e5,color:#1f1f1f;
    classDef data fill:#fff8e1,stroke:#ffb300,color:#1f1f1f;
    classDef async fill:#fce4ec,stroke:#d81b60,color:#1f1f1f;
    classDef ext fill:#e0f7fa,stroke:#00897b,color:#1f1f1f;

    class EDGE edge;
    class GW,AUTH,CAT,SEARCH,CART,CHECKOUT,ORDER,PAY,USER,SELLER,INGEST,PRICE,PROMO,INV,RESV,ETA,SHIP,RETURN,REC,RANK,EXP svc;
    class SQL,INVDB,REDIS,IDX,OBJ,FEAT data;
    class OUTBOX,RELAY,MQ,WORKERS,DLQ,STREAM async;
    class PGW,NOTIF,LOGI,FRAUD ext;
```

---

## What to say for Amazon/Flipkart interview depth

### 1) Catalog and seller scale
- Separate **seller onboarding** and **catalog ingestion** from customer-facing read APIs.
- Pre-validate and normalize product data before indexing.
- Use async pipelines for media processing and catalog enrichment.

### 2) Price and promotion correctness
- Price is often dynamic (base + seller + campaign + coupon + user segment).
- Compute final payable price in checkout with deterministic rule ordering.
- Keep audit trail for price decisions and promotion application.

### 3) Inventory consistency
- Use **reservation** during checkout with TTL (soft lock).
- Confirm reservation on successful payment; release on timeout/failure.
- Strong consistency for stock decrement path, eventual consistency for stock display.

### 4) Checkout reliability
- Use idempotency keys for `place-order` and payment callbacks.
- Keep order + outbox in single transaction.
- Make downstream operations async (invoice, notifications, analytics).

### 5) Search and relevance
- Separate online serving (`SEARCH API`) from offline/stream indexing.
- Relevance ranking combines text score + popularity + availability + personalization.
- A/B testing service controls ranking models and rollout.

### 6) Personalization and recommendations
- Feature store provides low-latency user/item features.
- Candidate generation + ranking stage architecture.
- Fall back to popular/trending when personalization signals are missing.

### 7) Returns and reverse logistics
- Model return eligibility rules (window, category, condition, seller policy).
- Decouple reverse pickup/refund workflows using events and state machine.
- Refund path should be idempotent and auditable.

---

## Marketplace-specific trade-offs interviewers ask

### A) Pre-compute vs real-time compute (price, ETA, recommendations)
- **Pre-compute:** lower latency, higher storage/staleness risk.
- **Real-time:** fresh results, higher compute cost/latency.
- Practical answer: hybrid (pre-compute base features, real-time final adjustments).

### B) Inventory reservation strictness
- **Strict lock:** fewer oversells, may reduce conversion during high contention.
- **Soft lock with TTL:** better UX throughput, needs compensating logic.
- Practical answer: soft lock + reconciliation + seller-side safety buffers.

### C) Search freshness vs indexing cost
- **Near real-time indexing:** fresher search, higher infra cost.
- **Batch indexing:** cheaper, stale search window.
- Practical answer: high-priority updates (price/stock) near real-time, others batched.

### D) Single checkout orchestration service vs distributed saga
- **Central orchestrator:** simpler control flow, possible bottleneck.
- **Saga/choreography:** scalable/flexible, harder debugging.
- Practical answer: start orchestrator-led saga, evolve based on team/service maturity.

### E) Own logistics vs 3P logistics integration
- **Own logistics:** better control and potentially better SLA, higher capex/ops complexity.
- **3P logistics:** faster launch, less control over delivery quality.
- Practical answer: hybrid by region/category and SLA requirement.

---

## 60-second Amazon/Flipkart style summary

"At marketplace scale, I split the system into customer-facing APIs and heavy back-office pipelines like seller onboarding, catalog ingestion, and search indexing. Checkout is reliability-first: idempotent APIs, transactional outbox, and event-driven downstream processing. Inventory uses reservation with TTL to minimize oversell while maintaining conversion. Search and recommendations are separate relevance systems with feature store + A/B testing. I keep strong consistency for payment/order/inventory commit paths and eventual consistency for projections, personalization, and notifications."

## Additional scalable options (beyond current design)

Use these as "next evolution" points in interview once baseline architecture is clear.

### 1) Edge and API scaling
- **Multi-CDN strategy:** Route traffic across two CDN providers for regional performance and resilience.
- **Global traffic steering:** Geo DNS / Anycast routing to nearest healthy region.
- **Adaptive rate limiting:** Per endpoint + user tier + behavior risk score.
- **API response compression + partial response fields:** Reduce payload and network cost.

**When to use:** High global traffic, latency-sensitive markets, or regional outage risk.

### 2) Service-layer scaling
- **Cell-based architecture (cell/partition per tenant or geography):** Limits blast radius during failures.
- **Queue-based load leveling for bursty endpoints:** Convert expensive sync work into buffered async jobs.
- **Auto-scaling by SLO signals:** Scale on p95 latency + queue lag, not only CPU.
- **Request coalescing (single-flight):** Prevent duplicate backend work for same hot key.

**When to use:** Frequent traffic spikes (sales events, flash deals, peak shopping windows).

### 3) Catalog/search scaling
- **Split read/write search clusters:** Isolate indexing load from query serving.
- **Hierarchical caching:** Edge cache + gateway cache + service cache + Redis.
- **Hot product pre-warming:** Preload top SKUs into cache before campaigns.
- **Search query federation:** Separate clusters per category/language/region.

**When to use:** Large catalogs, heavy search/filter traffic, sale-time spikes.

### 4) Order and payment scaling
- **Order ID partitioning / range bucketing:** Enables efficient horizontal sharding.
- **Saga orchestration with compensations:** Scales distributed transaction workflows safely.
- **Payment retry orchestration service:** Centralized policy for callback retries and reconciliation.
- **Idempotency store with TTL + dedupe index:** High-throughput duplicate protection.

**When to use:** High write volume with strict correctness on order/payment lifecycle.

### 5) Inventory scaling options
- **Regional inventory partitions:** Keep stock operations local to region for low latency.
- **Two-level inventory model:** Available-to-promise cache + strongly consistent commit layer.
- **Seller safety stock buffers:** Reduce oversell during high concurrency.
- **Reservation queue per SKU group:** Avoid hotspot lock contention on viral items.

**When to use:** Frequently changing stock, multiple seller warehouses, hot SKUs.

### 6) Data-store scaling
- **Sharding strategy evolution:**
  - Stage 1: single primary + replicas
  - Stage 2: shard by `user_id` / `order_id`
  - Stage 3: regional shards + federated query layer
- **CQRS read models:** Dedicated denormalized read stores for high-volume query APIs.
- **Materialized views with async refresh:** Faster reads for dashboards and order history.
- **Cold/hot tiering:** Move old immutable data to cheaper storage.

**When to use:** Primary DB saturation, storage growth, heavy mixed read/write workloads.

### 7) Event and async pipeline scaling
- **Partitioned event topics:** Partition by order/user/seller key for parallel consumption.
- **Consumer groups with autoscaling:** Scale workers based on consumer lag.
- **Replayable event logs:** Rebuild caches/indexes/projections from event history.
- **Priority queues:** Separate customer-critical events from batch analytics events.

**When to use:** High event throughput, backlogs during campaigns, reprocessing needs.

### 8) Multi-region scaling patterns
- **Active-passive for checkout path:** Simple failover for critical writes.
- **Active-active for read-heavy services:** Search/catalog/personalization served in-region.
- **Data sovereignty split:** Region-local storage for compliance constraints.
- **Cross-region async replication with conflict policy:** Last-write-wins or domain-specific merge.

**When to use:** Global user base, compliance constraints, low-latency regional UX goals.

### 9) Personalization/recommendation scaling
- **Feature store online/offline split:** Low-latency serving + batch training pipeline.
- **Approximate nearest-neighbor vector search:** Scalable candidate retrieval for recommendations.
- **Model inference gateway:** Standardized deployment/versioning for ranking models.
- **Fallback strategy chain:** Personalized -> category popular -> global popular.

**When to use:** Large recommendation workloads and strict page latency budgets.

### 10) Operations and reliability scaling
- **Progressive delivery at scale:** Canary by region/cohort + automated rollback on SLO breach.
- **SLO-aware traffic shedding:** Drop low-priority workloads first during overload.
- **Synthetic traffic probes:** Continuous validation of critical customer journeys.
- **Game days/chaos testing:** Validate resilience assumptions regularly.

**When to use:** Large deployments with frequent releases and strict uptime goals.

---

## Scaling trigger matrix (what to add and when)

| Symptom                   | First move                               | Next move                                    |
|---------------------------|------------------------------------------|----------------------------------------------|
| p95 read latency rising   | Add Redis + query/index tuning           | Add read replicas + CQRS read models         |
| DB write CPU saturation   | Batch non-critical writes + tune indexes | Shard write path by key                      |
| Queue lag growing         | Scale consumer groups                    | Partition topics + priority queues           |
| Overselling during sales  | Add reservation TTL + safety stock       | SKU-level serialization / reservation queues |
| Global latency complaints | Multi-CDN + geo routing                  | Multi-region active-active for reads         |
| Search staleness          | Priority indexing for stock/price        | Split ingest/search clusters                 |

---

## 45-second "scalability" answer for interviewer

"Beyond the base design, I scale in layers: edge scaling with multi-CDN and geo-routing, service scaling with cell architecture and SLO-driven autoscaling, and data scaling with replicas first then sharding and CQRS read models. For event-heavy flows I use partitioned topics, autoscaled consumers, and replayable logs. For marketplace-specific hotspots like inventory and checkout, I use reservation TTL, idempotency, and saga orchestration. I evolve to multi-region patterns based on latency, compliance, and availability goals."

## Interview questions and model answers (with this diagram)

### 1) Why do you need both `CDN` and `Redis`?
**Answer:** `CDN` caches static/edge-cacheable content near users; `Redis` caches dynamic application data (hot product details, session fragments, computed responses). They solve different latency layers.

### 2) Why put `WAF` and `Rate Limiter` before `API Gateway`?
**Answer:** It blocks malicious/abusive traffic early, protecting expensive backend resources and reducing attack blast radius.

### 3) Why is `SQL` the source of truth here?
**Answer:** Order/payment/inventory commit paths need ACID guarantees and strong consistency. SQL provides reliable transactions and integrity constraints.

### 4) What consistency model do you use?
**Answer:** Strong consistency for order-payment-inventory commit path (`ORDER`, `PAY`, `SQL`). Eventual consistency for projections (`IDX`, notification, analytics, cache refresh).

### 5) Why use `Read Replica` if `Redis` already exists?
**Answer:** `Redis` serves hot keys; replicas handle broader read workloads and cache misses. Replicas reduce pressure on primary for query-heavy but less-hot traffic.

### 6) How do you handle cache invalidation?
**Answer:** Write-through or event-driven invalidation from async workers; plus TTL fallback. On miss/stale, fallback to DB and repopulate.

### 7) Why introduce `Transactional Outbox` instead of direct broker publish?
**Answer:** Direct dual-write can lose events on partial failure. Outbox guarantees business row + event row are committed atomically, then relay publishes safely.

### 8) How do you avoid duplicate event processing?
**Answer:** At-least-once delivery is expected. Consumers are idempotent using event IDs + dedupe store/unique constraint.

### 9) What is the role of `DLQ`?
**Answer:** Isolates poison messages after retry limit, prevents queue clogging, and enables controlled replay with alerting.

### 10) How does this design scale during flash sales?
**Answer:** Multi-layer caching, autoscaled stateless services, queue-based load leveling, partitioned topics, and inventory reservation with TTL to reduce contention.

### 11) How do you prevent overselling inventory?
**Answer:** Reservation (soft lock) at checkout with TTL, confirm on payment success, release on failure/timeout, plus reconciliation and safety stock buffers.

### 12) Why separate `Search Index` from primary DB?
**Answer:** Search needs full-text, filtering, ranking, and high-QPS query patterns that are inefficient on OLTP DB. Index serves fast search independently.

### 13) How do you keep search results fresh?
**Answer:** Consume product/price/inventory events, prioritize critical updates (price/stock) near real-time, batch non-critical enrichments.

### 14) How do you handle payment gateway timeout but later success callback?
**Answer:** Use idempotency keys + state machine transitions; accept callback as source of payment truth, reconcile asynchronously, avoid double charge/order.

### 15) Why use async workers for notifications/analytics?
**Answer:** These are non-critical to immediate user confirmation. Async processing reduces request latency and improves resilience under spikes.

### 16) What observability signals would you monitor first?
**Answer:** p95 latency, error rate, primary DB CPU/locks, cache hit ratio, broker lag, retry counts, DLQ depth, payment callback failure rate.

### 17) How would you evolve from v1 to global scale?
**Answer:** Start single-region multi-AZ, add geo-routing + multi-CDN, move read-heavy services to active-active multi-region, keep write path controlled (active-passive or conflict strategy).

### 18) How do you decide monolith vs microservices initially?
**Answer:** Start modular monolith for speed and simplicity, split by bounded contexts when team scale, deploy cadence, or traffic profile requires independent scaling.

### 19) Where would you apply CQRS in this architecture?
**Answer:** High-read domains like order history/search/list pages: write to SQL; build denormalized read models asynchronously for fast query APIs.

### 20) What are your top failure scenarios and mitigations?
**Answer:**
- Broker down -> retry + backoff + DLQ.
- DB primary pressure -> replicas, query tuning, sharding plan.
- Payment callback duplication -> idempotent state transitions.
- Cache outage -> graceful DB fallback + controlled degradation.

---

## Rapid-fire interviewer follow-ups (one-line answers)

- **Why not exactly-once globally?** Too costly/complex across boundaries; at-least-once + idempotency is practical.
- **Why not only NoSQL?** Critical transactional workflows need strong relational guarantees.
- **How do you roll out safely?** Canary + SLO-based rollback + feature flags.
- **How do you reduce blast radius?** Cell-based partitions + bulkheads + circuit breakers.
- **How do you recover from bad deployment?** Automated rollback, replay events from log, rebuild projections.

---

## 2-minute interview script using this Q&A

"I start at edge with CDN, WAF, and rate limiting to protect latency and security. API gateway routes to stateless services. For correctness, SQL is source of truth for order/payment paths. For scale, reads are optimized through Redis, replicas, and a separate search index. For reliability, I use transactional outbox so DB writes and events stay consistent; relay publishes to broker and workers process side effects asynchronously. Failures are handled with retries, idempotency, and DLQ. As traffic grows, I evolve with sharding, CQRS read models, partitioned topics, and multi-region strategies based on latency and availability goals."
