# High Level Design of Online Applications (Interview Guide)

> GitHub-renderable architecture diagram: [`design-hld-online-applications-diagram.md`](./design-hld-online-applications-diagram.md)

This document is a practical blueprint for system design interviews where the prompt is generic, for example:
- Design an online shopping application
- Design a food delivery application
- Design an online learning platform
- Design a social media application
- Design an online booking platform

Use this guide as a reusable framework. In interviews, you will adapt the same steps to the domain.

---

## 1) Start with requirement clarification (first 5-7 minutes)

Do not jump into architecture. First align scope.

### Functional requirements
Ask and confirm:
1. Core user actions (read/write flows)
   - Sign up/login
   - Search/browse
   - Create/update entity (order, post, booking, class, etc.)
   - Payment/checkout (if relevant)
   - Notifications
2. Actor types
   - End user, admin, partner/vendor, support
3. Must-have vs out-of-scope
   - Example: "No recommendation engine in v1"
4. Real-time requirements
   - Chat, live location, stock updates, live class updates

### Non-functional requirements
Clarify what matters most:
- Availability target (99.9, 99.99)
- Latency SLA (P95/P99 for read and write APIs)
- Consistency need (strong vs eventual)
- Throughput (RPS / QPS)
- Durability and data loss tolerance
- Security and compliance (PII, PCI, GDPR)
- Multi-region requirement (active-active or active-passive)

### Example scope statement
"I will design v1 for core flows: user auth, browse/search, create transaction, payment callback, and notifications. Recommendation and analytics dashboard are out of scope for now."

---

## 2) Convert requirements into numbers (capacity estimation)

Interviewers expect rough, sane math. Keep assumptions explicit.

### Baseline assumptions template
- Daily active users (DAU): 5M
- Peak factor: 5x average
- Read:Write ratio: 80:20
- Avg request payload: 1 KB, response: 5 KB
- Retention: 2 years

### Quick calculations template
1. Requests per second:
   - Avg RPS = total requests/day / 86400
   - Peak RPS = Avg RPS x peak factor
2. Storage:
   - Daily data = writes/day x average record size
   - 2-year storage = daily data x 730
3. Bandwidth:
   - Peak egress ~= peak read RPS x avg response size
4. Cache sizing:
   - Hot set estimated from top 10-20 percent most accessed items

Tip: round numbers for speed and communicate assumptions clearly.

---

## 3) Define APIs before internals

A clean API contract shows product understanding.

### Typical API set for online apps
- `POST /users/signup`
- `POST /users/login`
- `GET /items?query=&page=` or `GET /catalog`
- `GET /items/{id}`
- `POST /orders` or `POST /bookings`
- `GET /orders/{id}`
- `POST /payments/callback`
- `GET /notifications`

### API design basics
- Idempotency key for critical writes (`POST /orders`, payments)
- Pagination with cursor for large lists
- Versioning (`/v1`)
- Standard error codes and retry semantics

---

## 4) Data model and storage choices

Choose storage per access pattern, not by trend.

### Core entities (generic)
- User
- Product/Content/Listing
- Cart/Session (optional)
- Order/Booking/Transaction
- Payment
- Notification
- Audit/Event log

### Database choices
1. OLTP primary DB (usually relational)
   - Good for transactions and constraints
   - Tables: users, orders, payments, inventory
2. Search index (Elasticsearch/OpenSearch)
   - Full-text, faceting, filtering, ranking
3. Cache (Redis)
   - Sessions, hot items, rate limits
4. Object storage (S3-like)
   - Images, documents, videos
5. Data warehouse/lake (optional in v1)
   - BI/reporting/analytics

### Partitioning and indexing
- Start with proper indexes on query paths
- Introduce sharding when a single node saturates
- Partition by tenant/region/entity-id if needed

---

## 5) High-level architecture (core blocks)

A common interview architecture:
1. Client (Web/Mobile)
2. CDN + WAF
3. API Gateway / Load Balancer
4. Stateless application services
5. Datastores (SQL/NoSQL/Search/Cache/Object store)
6. Async backbone (Message queue/event bus)
7. Background workers
8. Observability stack

### Suggested service decomposition
- Auth Service
- Catalog/Content Service
- Transaction Service (Order/Booking)
- Payment Service
- Notification Service
- Search Service

Keep it modular but avoid over-microservice for small scope.

---

## 6) Deep dives interviewers usually ask

### A) Read path optimization
- CDN for static content
- Redis for hot data and precomputed responses
- Read replicas for heavy read traffic
- Search engine for query-heavy endpoints

### B) Write path reliability
- DB transaction boundaries
- Idempotency for retries
- Outbox pattern for reliable event publishing
- Retry with backoff + dead letter queue for failed jobs

### C) Consistency strategy
- Strong consistency where money/inventory is involved
- Eventual consistency for non-critical views (feeds, counters, analytics)
- Explain tradeoff explicitly

### D) Scaling strategy
- Horizontal scale stateless services
- DB read replicas first, then sharding
- Queue-based async processing for burst absorption
- Backpressure and rate limiting

### E) Failure handling
- Timeouts, retries, circuit breakers
- Graceful degradation (serve stale cache)
- Bulkhead isolation between services
- Disaster recovery plan (RPO, RTO)

---

## 7) Reliability and availability design

### Reliability controls
- Multi-AZ deployment
- Health checks + auto-healing
- Blue-green or canary deploys
- Data backups and restore drills

### Availability patterns
- Active-passive across regions for simpler ops
- Active-active for very high availability (with conflict strategy)

### SLO examples
- API availability: 99.95%
- P95 read latency: < 200 ms
- P95 write latency: < 300 ms

---

## 8) Security and compliance (must mention)

- TLS everywhere
- OAuth/JWT session strategy
- Password hashing (Argon2/bcrypt)
- Encryption at rest for PII
- RBAC for admin operations
- Audit logs for sensitive actions
- Secrets management (no plaintext keys in code)
- Rate limit + bot protection

If payments are in scope: mention PCI boundary and tokenization.

---

## 9) Observability and operations

### Metrics
- RPS, error rate, latency percentiles
- Queue lag, retry counts, DLQ size
- Cache hit ratio
- DB CPU, connection pool, slow queries

### Logging
- Structured logs with correlation ID / request ID

### Tracing
- Distributed tracing for cross-service latency debugging

### Alerts
- SLO breach alerts
- High error spikes
- Payment callback failures

---

## 10) Cost-aware design points

Interview bonus if you discuss cost tradeoffs.

- Cache hot data to reduce DB cost
- Use object storage + CDN for media
- Tiered storage for old data
- Use autoscaling for variable load
- Avoid over-provisioning early

---

## 11) Common interview pitfalls

1. Starting with microservices without requirement clarity
2. No capacity estimates
3. Ignoring idempotency and retries
4. Ignoring data model and indexes
5. No failure and disaster recovery story
6. No security discussion
7. Over-complicated design for v1

---

## 12) 45-minute interview flow (recommended)

1. 0-7 min: clarify scope + NFRs
2. 7-12 min: rough capacity estimates
3. 12-20 min: high-level architecture diagram
4. 20-30 min: data model + APIs
5. 30-38 min: deep dive on 1 critical flow (write or real-time)
6. 38-43 min: scaling, reliability, security
7. 43-45 min: tradeoffs + future improvements

---

## 13) Reusable answer template (speak in interview)

"I will start by confirming functional and non-functional requirements, then estimate scale to justify architecture choices. I will design stateless services behind a load balancer, use a relational DB for transactions, Redis for hot reads, and a message queue for asynchronous workflows. For reliability, I will use idempotent APIs, retries with backoff, and outbox-based event publishing. For scale, I will add read replicas and partitioning as traffic grows. I will also cover observability, security, and disaster recovery tradeoffs."

---

## 14) Domain adaptation cheat-sheet

Use the same architecture skeleton and only replace domain components:

- E-commerce: Catalog, Cart, Order, Inventory, Payment
- Food delivery: Restaurant, Menu, Order, Driver Dispatch, Live Tracking
- Learning platform: Course, Enrollment, Video Streaming, Progress Tracking
- Ticket booking: Inventory lock, Payment, Booking confirmation, cancellation
- Social app: Post, Feed generation, Like/Comment, Notification fanout

---

## 15) Final checklist before you finish your interview answer

- Did I clearly define scope and assumptions?
- Did I estimate RPS/storage with simple math?
- Did I show APIs and data model?
- Did I explain read and write paths?
- Did I cover consistency tradeoffs?
- Did I include reliability/failure handling?
- Did I include security and observability?
- Did I discuss cost and future evolution?

If yes, your HLD answer is usually interview-ready.
