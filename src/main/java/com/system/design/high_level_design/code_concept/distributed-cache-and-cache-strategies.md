# Distributed Cache and Cache Strategies

A **distributed cache** stores frequently accessed data across multiple cache nodes (instead of one machine) to reduce database load and improve response time.

## Why Distributed Cache is Needed

Single-node cache works for small systems, but it has limits:
- memory is limited to one machine
- single point of failure
- hard to scale under growing traffic

A distributed cache solves this by spreading keys across many nodes.

**Benefits**
- Lower latency for repeated reads.
- Higher throughput (database is protected from hot read traffic).
- Horizontal scalability by adding cache nodes.
- Better availability with replication/failover.

## Basic Architecture

Typical read flow:
1. Application checks cache for key.
2. If found (**cache hit**), return immediately.
3. If missing (**cache miss**), read from DB/service.
4. Put result into cache with TTL.
5. Return response.

Typical write flow depends on chosen write strategy (write-through, write-back, etc.).

## Core Distributed Cache Concepts

### 1) Partitioning (Sharding)

Data is split across nodes so each node stores only a subset of keys.

Common approaches:
- **Modulo hashing**: `node = hash(key) % N` (simple but poor rebalance on node count change).
- **Consistent hashing**: minimizes key movement when nodes are added/removed.

**Example**
- Keys: `user:101`, `user:102`, `user:103`
- Node ring maps each key to nearest clockwise node.
- If one node is removed, only a fraction of keys remap.

### 2) Replication

Same key can be copied to more than one node.

- Improves read availability.
- Helps during node failure.
- Trade-off: more memory usage and write complexity.

### 3) Consistency Model

Distributed caches are often **eventually consistent**.

- Reads can be stale for a short time.
- Strong consistency is possible but slower and more complex.

### 4) TTL (Time-to-Live)

Each key can expire automatically after a configured duration.

- Prevents very old data from living forever.
- Helps control memory usage.

## Major Cache Strategies

## 1) Cache-Aside (Lazy Loading)

Application manages cache explicitly.

**Read**
- Try cache.
- On miss, read DB and set cache.

**Write**
- Write DB first.
- Invalidate/delete cache key.

**Advantages**
- Most common and simple.
- Good control per key.
- Cache only what is requested.

**Disadvantages**
- First request after miss is slow.
- Risk of stale data if invalidation is missed.

**Use Cases**
- Product details, user profile, article pages.

**Example**
- `GET /products/42`
- Cache miss -> fetch from DB -> set `product:42` with TTL 10 minutes.

---

## 2) Read-Through

Application reads from cache only; cache layer fetches DB on miss.

**Advantages**
- Cleaner application code (cache handles miss logic).
- Standardized behavior across services.

**Disadvantages**
- Requires cache provider/system that supports read-through.
- Extra dependency on cache layer capabilities.

**Use Cases**
- Platforms with centralized data access layer.

**Example**
- Service asks cache for `order:9001`; cache internally loads from DB if missing.

---

## 3) Write-Through

Application writes to cache and DB synchronously (or cache forwards write).

**Advantages**
- Cache is always warm for recently written keys.
- Stronger read-after-write behavior than cache-aside.

**Disadvantages**
- Higher write latency (DB + cache path on each write).
- Writes data that may never be read.

**Use Cases**
- User preference updates, profile settings, metadata.

**Example**
- Update `user:101:theme=dark` -> write DB and cache in same request path.

---

## 4) Write-Around

Writes go directly to DB, bypassing cache; cache populated only on reads.

**Advantages**
- Avoids polluting cache with write-only or rarely-read data.
- Lower cache write pressure.

**Disadvantages**
- Read after write may miss cache and hit DB.
- Higher miss rate for newly written data.

**Use Cases**
- Logging/event streams with many writes and few reads.

**Example**
- New audit record written to DB only; if queried later, cache-aside fills it.

---

## 5) Write-Back (Write-Behind)

Write goes to cache first, then asynchronously flushed to DB.

**Advantages**
- Very low write latency.
- Good for bursty traffic.

**Disadvantages**
- Risk of data loss if cache crashes before flush.
- Complex durability and retry handling.
- Eventual consistency by design.

**Use Cases**
- Counters, analytics, high-frequency telemetry (when small delay is acceptable).

**Example**
- Increment page-view counter in cache every request; flush aggregates every few seconds.

## Eviction Policies (When Cache is Full)

### 1) LRU (Least Recently Used)
- Evicts key not used for the longest time.
- Good default for temporal locality workloads.

### 2) LFU (Least Frequently Used)
- Evicts key with lowest access frequency.
- Better when frequently hot keys must be retained.

### 3) FIFO (First In, First Out)
- Evicts oldest inserted key.
- Very simple but often less optimal.

### 4) TTL-Based Expiration
- Keys removed after configured lifetime.
- Works well with naturally aging data.

### 5) Random Eviction
- Chooses random key.
- Cheap and sometimes useful at very large scale.

## Cache Invalidation Strategies

Cache invalidation is often the hardest part.

### A) Time-Based Invalidation
- Use TTL (for example 5 minutes).
- Simple, but can serve stale data before expiry.

### B) Event-Based Invalidation
- Invalidate when source data changes.
- More accurate, but requires reliable event pipeline.

### C) Versioned Keys
- Encode version in key (for example `product:42:v3`).
- Avoids hard deletes and supports safe rollouts.

### D) Tag/Group Invalidation
- Invalidate related keys together (e.g., all category pages).
- Useful for CMS/e-commerce catalogs.

## Real-World Examples

## Example 1: E-Commerce Product Page

Pattern:
- Cache-aside + TTL + event invalidation

Flow:
1. `GET product:42` from cache.
2. Miss -> DB read -> cache set (TTL 10 min).
3. Product price update event arrives -> invalidate `product:42`.

Why this works:
- high read, low write
- acceptable short staleness

## Example 2: User Session Store

Pattern:
- Distributed in-memory cache with replication

Flow:
1. Session token maps to user session key.
2. Any API server can fetch session.
3. TTL auto-expires inactive sessions.

Why this works:
- very frequent reads/writes
- low latency requirement

## Example 3: News Feed Ranking

Pattern:
- Write-back for counters + periodic DB flush

Flow:
1. Like/view counters increment in cache.
2. Background job flushes batched updates every few seconds.

Why this works:
- huge write volume
- eventual persistence acceptable

## Example 4: Rate Limiter

Pattern:
- In-memory distributed counters with short TTL

Flow:
1. Key: `rate:user123:minute-2026-07-24T10:32`.
2. Increment atomically per request.
3. Reject when threshold crossed.

Why this works:
- atomic operations
- small TTL windows
- low latency decisions

## Common Problems and Mitigations

### 1) Cache Stampede (Thundering Herd)
Many requests miss same key simultaneously.

Mitigations:
- request coalescing/single-flight lock
- probabilistic early refresh
- staggered TTLs (jitter)

### 2) Hot Keys
One key receives massive traffic.

Mitigations:
- key replication
- local in-process cache for hottest keys
- isolate heavy keys to dedicated shard

### 3) Cache Avalanche
Many keys expire at same time.

Mitigations:
- random TTL jitter
- pre-warm critical keys
- fallback/degraded mode

### 4) Cold Start
After restart, hit rate drops heavily.

Mitigations:
- warm-up scripts
- persistent snapshots (if supported)
- prioritize top-N hot keys

## Choosing the Right Strategy

Quick guide:
- **Read-heavy, moderate staleness** -> Cache-aside + TTL + event invalidation.
- **Need always-hot recently written values** -> Write-through.
- **Very high write burst, eventual durability OK** -> Write-back.
- **Write-heavy but low read reuse** -> Write-around.
- **Centralized access layer available** -> Read-through.

## What to Measure

Track these metrics continuously:
- cache hit ratio
- p95/p99 latency
- eviction rate
- keyspace size and memory usage
- stale read rate
- backend DB QPS reduction

## Interview Framing Tips

When explaining distributed caching in interviews:
1. Clarify data access pattern (read-heavy vs write-heavy).
2. Pick strategy and justify trade-offs.
3. Explain invalidation and staleness handling.
4. Discuss failure modes (stampede, hot keys, node loss).
5. Mention observability and tuning metrics.

