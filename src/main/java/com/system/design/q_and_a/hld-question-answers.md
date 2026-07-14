# HLD System Design Interview Q&A

This document collects the current high-level design interview questions and detailed answers for the core system design topics in this repository.

Topics covered:
- Load Balancing
- Caching
- Rate Limiting
- Message Queue
- Database Sharding
- API Gateway
- Circuit Breaker
- Consistent Hashing
- Idempotency
- Network Protocols
- CAP Theorem
- Monolithic vs Microservices

---

## 1) Core HLD Interview Questions

### Q1. What is High-Level Design (HLD)?
**Answer:**
High-Level Design describes the overall architecture of a system: major services, data flow, storage choices, communication patterns, scaling strategy, and failure handling. In interviews, HLD focuses on system structure rather than class-level implementation details.

Key things to cover:
- Main components and their responsibilities
- Data flow between components
- Scalability and availability
- Latency, throughput, and bottlenecks
- Tradeoffs and failure modes

### Q2. What is the difference between HLD and LLD?
**Answer:**
- **HLD** explains the system architecture at a service/component level.
- **LLD** goes deeper into classes, methods, data structures, and implementation details.

In interviews, HLD answers usually include APIs, databases, queues, caches, and deployment topology, while LLD answers focus on code structure and object design.

### Q3. What are the main qualities a system design should satisfy?
**Answer:**
Common non-functional requirements are:
- Scalability
- Availability
- Reliability
- Consistency
- Low latency
- Fault tolerance
- Security
- Observability

### Q4. How do you start a system design interview answer?
**Answer:**
Use this sequence:
1. Clarify requirements
2. Define core use cases
3. Estimate traffic and scale
4. Identify main entities and APIs
5. Design the high-level architecture
6. Deep dive into bottlenecks and tradeoffs
7. Cover failure handling and monitoring

---

## 2) Load Balancing

### Q1. Why do we need load balancing?
**Answer:**
Load balancing distributes traffic across multiple servers so no single instance becomes a bottleneck. It improves throughput, availability, and resilience.

### Q2. What are common load balancing algorithms?
**Answer:**
- **Round Robin:** send requests in rotation
- **Weighted Round Robin:** give more traffic to stronger servers
- **Least Connections:** send to server with fewest active connections
- **IP Hash:** route by client IP for session affinity
- **Random / Power of Two Choices:** choose among a few random nodes for good balance

### Q3. Layer 4 vs Layer 7 load balancing?
**Answer:**
- **Layer 4 (transport):** routes by IP/port, very fast, protocol-agnostic
- **Layer 7 (application):** routes by URL, headers, cookies, content; supports intelligent routing

Use Layer 4 for speed and simplicity. Use Layer 7 when you need routing rules, auth-aware routing, or request inspection.

### Q4. What is health checking?
**Answer:**
The load balancer periodically checks whether a backend is alive and ready. If a server fails checks, traffic is removed from it until recovery.

### Q5. What is session affinity and when should it be avoided?
**Answer:**
Session affinity binds a user to one backend server. It can help with local session state but reduces flexibility and can create imbalance. Prefer shared session storage or stateless services when possible.

---

## 3) Caching

### Q1. Why is caching important?
**Answer:**
Caching reduces repeated reads from slower systems like databases or external services. It improves latency and reduces load.

### Q2. What are common cache strategies?
**Answer:**
- **Cache-Aside:** app reads from cache first, falls back to DB on miss
- **Read-Through:** cache fetches from DB on miss transparently
- **Write-Through:** writes go to cache and DB together
- **Write-Back:** write to cache first and flush later to DB

### Q3. What is cache invalidation and why is it hard?
**Answer:**
Cache invalidation means removing or updating stale data when the source data changes. It is hard because distributed systems can serve stale cache entries, and invalidation must balance correctness, performance, and complexity.

### Q4. What are cache eviction policies?
**Answer:**
Common policies include:
- **LRU:** evict least recently used
- **LFU:** evict least frequently used
- **FIFO:** evict oldest inserted

### Q5. How do you prevent cache stampede?
**Answer:**
Use one or more of these:
- request coalescing / single-flight
- random TTL jitter
- locking around cache fill
- stale-while-revalidate

### Q6. When should you not cache?
**Answer:**
Avoid caching when data changes extremely often, correctness is more important than latency, or the cache value would be too large / too expensive to keep updated.

---

## 4) Rate Limiting

### Q1. Why do we rate limit?
**Answer:**
Rate limiting protects services from abuse, traffic spikes, and accidental overload. It also enables fair usage across users or tenants.

### Q2. What are the main rate limiting algorithms?
**Answer:**
- **Token Bucket:** allows bursts up to bucket size while enforcing average rate
- **Leaky Bucket:** smooths traffic into a constant outflow
- **Fixed Window:** counts requests in fixed time intervals
- **Sliding Window:** more accurate rolling window counting

### Q3. Token bucket vs leaky bucket?
**Answer:**
- Token bucket allows bursts and is good for user-facing APIs
- Leaky bucket enforces a steady output rate and is good for smoothing spikes

### Q4. Where can rate limiting be enforced?
**Answer:**
- API gateway
- Application service
- Load balancer
- Edge/CDN

Best practice is to enforce at the edge and also protect critical internal endpoints if needed.

### Q5. What should a rate limiter return?
**Answer:**
Usually `HTTP 429 Too Many Requests`, often with headers such as retry-after or remaining quota.

---

## 5) Message Queue

### Q1. Why use a message queue?
**Answer:**
Message queues decouple producers from consumers, smooth bursts, improve reliability, and enable asynchronous processing.

### Q2. What problems do queues solve?
**Answer:**
- request buffering
- asynchronous jobs
- retry and dead-letter handling
- fan-out to multiple consumers
- smoothing traffic spikes

### Q3. What is the difference between pub/sub and queue-based consumption?
**Answer:**
- **Queue:** one message is typically consumed by one worker
- **Pub/Sub:** one published event can be delivered to many subscribers

### Q4. What is at-least-once delivery?
**Answer:**
It guarantees a message will be delivered one or more times. Consumers must be idempotent because duplicates are possible.

### Q5. What is a dead-letter queue (DLQ)?
**Answer:**
A DLQ stores messages that repeatedly fail processing. It prevents poison messages from blocking the main queue and helps with debugging and recovery.

### Q6. What are ordering concerns in queues?
**Answer:**
Ordering may be guaranteed per partition, per key, or not at all depending on the system. If strict ordering is required, design around a key-based partitioning strategy.

---

## 6) Database Sharding

### Q1. Why shard a database?
**Answer:**
Sharding splits data across multiple databases to scale storage and write throughput beyond a single node.

### Q2. What are common sharding strategies?
**Answer:**
- **Range Sharding:** partition by key ranges
- **Hash Sharding:** use a hash of the key
- **Directory Sharding:** use a lookup service to map keys to shards

### Q3. Hash sharding vs range sharding?
**Answer:**
- Hash sharding gives even distribution but harder range queries
- Range sharding supports range scans but can create hotspots

### Q4. What is a shard key?
**Answer:**
The shard key determines where a record lives. Good shard keys distribute load evenly and match query access patterns.

### Q5. What are the downsides of sharding?
**Answer:**
- cross-shard joins are harder
- rebalancing is complex
- transactions become harder
- operational complexity increases

---

## 7) API Gateway

### Q1. What is an API gateway?
**Answer:**
An API gateway is the front door to backend services. It handles routing, auth, throttling, aggregation, logging, SSL termination, and protocol translation.

### Q2. Why use an API gateway instead of exposing services directly?
**Answer:**
It centralizes cross-cutting concerns and hides internal service topology from clients.

### Q3. What features commonly belong in a gateway?
**Answer:**
- authentication and authorization
- request routing
- rate limiting
- request/response transformation
- aggregation
- caching
- observability

### Q4. What is an anti-pattern with API gateways?
**Answer:**
Putting all business logic in the gateway. The gateway should remain a thin orchestration layer, not the core domain owner.

---

## 8) Circuit Breaker

### Q1. Why use a circuit breaker?
**Answer:**
It prevents a failing dependency from causing cascading failures across the system.

### Q2. What are the circuit breaker states?
**Answer:**
- **Closed:** requests pass through normally
- **Open:** requests fail fast without calling the dependency
- **Half-Open:** a small number of trial requests test recovery

### Q3. What triggers the breaker to open?
**Answer:**
Common triggers are high failure rate, high latency, or too many consecutive errors.

### Q4. What should happen when the breaker is open?
**Answer:**
Return a fast failure, fallback response, or cached data. Do not keep hammering the unhealthy dependency.

### Q5. Circuit breaker vs retry?
**Answer:**
Retries try again after temporary failure; circuit breakers stop repeated calls to a clearly failing dependency. They are complementary, not substitutes.

---

## 9) Consistent Hashing

### Q1. Why use consistent hashing?
**Answer:**
It reduces remapping when nodes are added or removed, which is useful for caches, distributed storage, and routing.

### Q2. How does consistent hashing work?
**Answer:**
Both keys and nodes are hashed onto a ring. Each key maps to the next node clockwise on the ring.

### Q3. What are virtual nodes?
**Answer:**
Virtual nodes give each physical node multiple positions on the hash ring, improving balance and smoothing uneven distribution.

### Q4. Why is consistent hashing better than modulo hashing?
**Answer:**
With modulo hashing, adding or removing a node can reshuffle most keys. With consistent hashing, only a small subset of keys moves.

---

## 10) Idempotency

### Q1. What is idempotency?
**Answer:**
An operation is idempotent if repeating it with the same intent produces the same result without unintended side effects.

### Q2. Why is idempotency critical for POST requests?
**Answer:**
POST is often used for resource creation, which is naturally non-idempotent. Idempotency keys let clients safely retry without creating duplicates.

### Q3. What is an idempotency key?
**Answer:**
It is a unique client-generated token, usually a UUID, attached to a request so the server can recognize duplicates.

### Q4. What should the server store for an idempotent operation?
**Answer:**
Typically:
- idempotency key
- request hash
- status (`CREATED`, `CONSUMED`)
- response body
- response status code
- timestamps / TTL

### Q5. What is the typical lifecycle of an idempotency key?
**Answer:**
1. Request arrives with key
2. Server checks the key store
3. If absent, server claims key with `CREATED`
4. Server performs the business action
5. Server stores the response and marks key `CONSUMED`
6. Retries return the stored response

### Q6. Why can idempotency fail in parallel requests?
**Answer:**
Because check-then-insert is not atomic. Two requests can both see the key as missing and both proceed.

### Q7. Why can idempotency fail in a multi-server setup?
**Answer:**
Because one server may not see another server’s latest state if replication is slow. A retry can land on a stale node and be treated as new.

### Q8. How do you make idempotency safe across servers?
**Answer:**
Use a shared low-latency store such as Redis as the source of truth, and claim keys atomically with `SETNX` or an equivalent unique insert operation.

### Q9. What responses are common for idempotency flows?
**Answer:**
- `400 Bad Request` for missing key
- `409 Conflict` for request already in progress
- `201 Created` for the first successful create
- `200 OK` for replayed cached response

---

## 11) Common Interview Follow-Up Questions

### Q1. How would you handle a sudden traffic spike in a write-heavy service?
**Answer:**
Combine load balancing, rate limiting, queueing, backpressure, and autoscaling. For read-heavy parts, add caching. For writes, buffer with queues if asynchronous processing is acceptable.

### Q2. How do you choose between caching and sharding?
**Answer:**
Caching improves read latency and reduces repeated work. Sharding increases write and storage capacity. They solve different bottlenecks and are often used together.

### Q3. How do you prevent cascading failures in a microservice architecture?
**Answer:**
Use circuit breakers, timeouts, retries with jitter, bulkheads, queues, and graceful degradation.

### Q4. What is your default strategy for designing any system in an interview?
**Answer:**
Start with requirements, estimate scale, identify bottlenecks, design the happy path, then add resilience: caching, queues, rate limiting, retries, circuit breakers, and observability.

---

## 12) Quick Interview Cheat Sheet

- Use **load balancing** to distribute traffic
- Use **caching** to speed up reads
- Use **rate limiting** to protect the system
- Use **queues** to decouple work and handle bursts
- Use **sharding** to scale storage and writes
- Use an **API gateway** to centralize cross-cutting concerns
- Use a **circuit breaker** to stop cascading failures
- Use **consistent hashing** to reduce rebalancing cost
- Use **idempotency** to prevent duplicate writes
- Use **HTTP** for standard APIs
- Use **WebSocket** for real-time bidirectional updates
- Use **SMTP** for sending email
- Use **FTP** for legacy file transfer
- Use **WebRTC** for peer-to-peer real-time media
- Use **CAP Theorem** to explain consistency vs availability decisions under partition
- Use **Monolith vs Microservices** tradeoffs based on team size, scale, and operational maturity

---

## 13) Network Protocols

### Q1. What is the application layer and why does it matter in system design?
**Answer:**
The application layer defines how software applications structure requests, responses, headers, metadata, authentication, and message semantics. It matters because it determines how clients and servers communicate at the API level.

### Q2. How do application-layer protocols work in general?
**Answer:**
1. Client creates a message in protocol format.
2. Lower layers transport the bytes.
3. Server receives and parses the protocol fields.
4. Server performs business logic.
5. Server returns a protocol-compliant response.

### Q3. What is HTTP and why is it the default choice for most APIs?
**Answer:**
HTTP is a request/response protocol used by browsers and services. It is widely supported, easy to debug, stateless by default, and works well for CRUD APIs and service-to-service communication.

### Q4. What are the most important HTTP concepts to remember for interviews?
**Answer:**
- Methods: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`
- Status codes: `200`, `201`, `400`, `401`, `403`, `404`, `409`, `429`, `500`
- Headers: `Authorization`, `Content-Type`, `Accept`, `Cache-Control`
- HTTP is stateless unless you add cookies, sessions, or tokens

### Q5. When would you choose WebSocket over HTTP?
**Answer:**
Use WebSocket when the application needs a persistent, low-latency, bidirectional channel such as chat, live dashboards, collaborative editing, or notifications.

### Q6. What is the key difference between HTTP polling and WebSocket?
**Answer:**
HTTP polling repeatedly opens requests to ask for updates, which adds overhead and latency. WebSocket upgrades one HTTP connection into a persistent channel, allowing the server and client to push messages instantly.

### Q7. What is FTP and where is it used?
**Answer:**
FTP is a file transfer protocol used to upload and download files between a client and server. It uses separate control and data channels and is often considered legacy in modern systems.

### Q8. What is SMTP used for?
**Answer:**
SMTP is used to send email from clients to mail servers and between mail servers. It is for outbound mail delivery, not mailbox reading.

### Q9. What is the difference between SMTP, IMAP, and POP3?
**Answer:**
- **SMTP:** sends email
- **IMAP:** synchronizes and reads mailbox content on the server
- **POP3:** downloads email to a local client, often with less server synchronization

### Q10. What is WebRTC?
**Answer:**
WebRTC is a peer-to-peer communication framework used for real-time audio, video, and data channels between browsers or applications.

### Q11. How does WebRTC establish a connection?
**Answer:**
WebRTC typically uses:
- signaling to exchange offers/answers
- ICE candidate exchange
- STUN to discover public addresses
- TURN when direct peer-to-peer connectivity is not possible

### Q12. Why is WebRTC not the same as HTTP?
**Answer:**
HTTP is request/response client-server communication. WebRTC is designed for direct real-time peer connectivity, usually for media and data exchange, and uses signaling only to establish the session.

### Q13. What should you mention when comparing protocols in an interview?
**Answer:**
You should mention the communication model, persistence, directionality, latency, reliability, and the best-fit use case for each protocol.

---

## 14) CAP Theorem

### Q1. What is CAP Theorem in simple terms?
**Answer:**
CAP says that in a distributed data system, during a network partition, you can prioritize at most one of:
- **Consistency (C):** all clients see the same latest value
- **Availability (A):** every request receives a non-error response

Partition tolerance is generally mandatory in real distributed systems.

### Q2. Why is partition tolerance treated as non-negotiable?
**Answer:**
Because network splits, packet loss, or inter-zone connectivity failures happen in production. A distributed system must define behavior for those failures, so the practical decision is usually CP vs AP under partition.

### Q3. What is a CP system behavior during partition?
**Answer:**
CP systems preserve correctness by rejecting/delaying operations that cannot meet consistency guarantees.

Example:
- payment or inventory systems often reject writes in isolated partitions rather than risk conflicting updates.

### Q4. What is an AP system behavior during partition?
**Answer:**
AP systems preserve responsiveness by continuing to serve requests from available replicas, then reconciling later.

Example:
- social feed, likes, and analytics counters can accept temporary inconsistency.

### Q5. Is CA possible?
**Answer:**
Only in environments where partition is not part of the model (for example single-node setups). In real distributed deployments, partition can occur, so CAP tradeoffs must be designed explicitly.

### Q6. How do you decide CP vs AP in interviews?
**Answer:**
Use business criticality:
- choose CP when wrong data is unacceptable (money, stock, identity state)
- choose AP when temporary staleness is acceptable but uptime/latency is critical (feeds, dashboards, recommendations)

### Q7. What techniques support CP design?
**Answer:**
- leader-based writes
- synchronous replication
- read/write quorum
- reject-on-uncertain-state behavior

### Q8. What techniques support AP design?
**Answer:**
- async replication
- eventual consistency
- conflict resolution rules
- idempotency + retries + reconciliation jobs

### Q9. How does CAP relate to microservices?
**Answer:**
Different endpoints can adopt different CAP bias.

Example:
- `/payments/authorize` can be CP-leaning
- `/profile/badge` can be AP-leaning with cached fallback

### Q10. What are common CAP misconceptions?
**Answer:**
- CAP is about behavior under partition, not only normal operation.
- CAP availability is not the same as SLA uptime percentage.
- CAP consistency is distributed visibility correctness, not only ACID wording.
- Modern systems often mix CP/AP per workflow.

### Q11. What is a strong one-liner answer for CAP in interviews?
**Answer:**
"I assume partition is inevitable, then choose CP or AP per workflow based on business risk, and add controls like quorum, idempotency, retries, and reconciliation to make that tradeoff safe."

---

## 15) Monolithic vs Microservices

### Q1. What is a monolithic architecture?
**Answer:**
A monolith is a single deployable application where most modules run in one process and are released together.

### Q2. What is a microservices architecture?
**Answer:**
Microservices split the system into independently deployable services aligned to business capabilities.

### Q3. What are the advantages of microservices?
**Answer:**
- independent deployment
- scale only hot services
- better team autonomy
- fault isolation
- technology flexibility when governed well

### Q4. What are the disadvantages of microservices?
**Answer:**
- higher operational complexity
- distributed debugging/tracing challenges
- network latency/failure handling required
- harder data consistency across service boundaries
- higher platform and infrastructure cost

### Q5. What are the advantages of monolithic architecture?
**Answer:**
- simple to start
- easier local debugging
- low initial infra complexity
- easier in-transaction consistency in one DB
- good fit for small teams and early products

### Q6. What are the disadvantages of monolithic architecture?
**Answer:**
- tight coupling as system grows
- slower releases with larger regression risk
- inefficient scaling of hot modules
- larger blast radius
- harder long-term team autonomy

### Q7. How do you handle the Decomposition phase?
**Answer:**
Decompose by business capabilities (bounded contexts), not technical layers. Start coarse-grained, extract high-value domains first, and avoid creating too many tiny services early.

### Q8. How do you handle the Communication phase?
**Answer:**
Use synchronous calls (HTTP/gRPC) for immediate responses and asynchronous messaging for decoupling and resilience. Add timeouts, retries, circuit breakers, and idempotency.

### Q9. How do you handle the Observability phase?
**Answer:**
Adopt logs + metrics + traces with correlation ids. Define SLOs and alerts. Distributed tracing is mandatory in microservices to diagnose cross-service latency and failures.

### Q10. How do you handle the Database phase?
**Answer:**
Prefer database-per-service in microservices for ownership autonomy. Avoid direct cross-service DB access. Use outbox, saga, CDC, and idempotent consumers for cross-service consistency.

### Q11. How do you handle the Integration phase?
**Answer:**
Use API gateway, contract testing, and anti-corruption layers for legacy boundaries. Protect integrations with circuit breakers, retries with jitter, and DLQ for async failure recovery.

### Q12. When should you stay monolithic instead of migrating?
**Answer:**
Stay monolithic when domain complexity is still low, team is small, scaling pressure is limited, and operational maturity for distributed systems is not yet ready.

### Q13. What is a strong migration approach from monolith to microservices?
**Answer:**
Use incremental strangler pattern migration: modularize monolith, extract high-value domains, introduce event-driven integration, and build platform capabilities (CI/CD, observability, templates) before large-scale extraction.

---

## 16) Closing Guidance

In HLD interviews, the strongest answers do not just name components; they explain:
- why the component exists,
- what problem it solves,
- what tradeoffs it introduces,
- and how it behaves under failure.

That is the standard expected for a production-grade system design discussion.

