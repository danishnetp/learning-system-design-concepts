# Idempotent POST: Final Issue and Solution Document

## Scope and Source
This final document consolidates idempotency issues and solutions from the `idempodent` concept artifacts.

> Note: No `.md` source files were present in this folder. This summary is derived from all `.puml` files in:
`src/main/java/com/system/design/hld/code_concept/idempodent`

Covered diagrams:
- `PostApiIssueSequence.puml`
- `PostApiIssueParallel.puml`
- `MultiServerIdempotentIssue.puml`
- `MultiServerIdempotentIssueFlow.puml`
- `PostApiSolutionSequence.puml`
- `PostApiSolutionSequenceFlow.puml`
- `PostApiSolutionParallel.puml`
- `PostApiSolutionParallelFlow.puml`
- `MultiServerIdempotentWithCache.puml`
- `MultiServerIdempotentWithCacheFlow.puml`

## Problem Statement
A `POST /orders` request may be processed multiple times for the same user intent when:
- the client retries after timeout,
- multiple parallel requests are sent with the same idempotency key,
- retries hit different servers before replication catches up.

Business impact:
- Duplicate orders
- Double-charge risk
- Support and finance reconciliation complexity

---

## Issue 1: Sequential Retry Creates Duplicate Orders
### Scenario
- Client sends `POST /orders`.
- Server successfully inserts order.
- Client does not receive response (network timeout).
- Client retries same intent.
- Server inserts a second order.

### Root Cause
- No idempotency key tracking and no response replay.

### Example Outcome
- First request creates `order_id=101`
- Retry creates `order_id=102`

---

## Issue 2: Parallel Requests Bypass Non-Atomic Checks
### Scenario
- Tab 1 and Tab 2 send same payload and same `Idempotency-Key`.
- Both perform `check key -> not found` concurrently.
- Both continue to insert business records.

### Root Cause
- Non-atomic check-then-insert path (race window).

### Example Outcome
- Tab 1 creates `order_id=301`
- Tab 2 creates `order_id=302`

---

## Issue 3: Multi-Server + Replication Lag Causes Duplicate Processing
### Scenario
- Request #1 is routed to Server 1 and written to DB1.
- Client retries before DB1 -> DB2 replication completes.
- Retry lands on Server 2.
- Server 2 checks stale DB2 and treats request as new.

### Root Cause
- Idempotency state stored only in per-server DBs.
- Slow asynchronous database replication.

### Example Outcome
- Server 1 creates `order_id=701`
- Server 2 creates `order_id=702`

---

## Solution 1: Standard Idempotency Key Lifecycle (Retry-Safe)
### Design
Use a dedicated idempotency store with key lifecycle:
1. Validate `Idempotency-Key` header (required)
2. Lookup key
3. If not found, create key with `status=CREATED`
4. Execute business logic once
5. Store response and mark `status=CONSUMED`
6. On retry, return cached response (no new insert)

### Response Behavior
- Missing header: `400 Bad Request`
- Existing key with `CREATED`: `409 Conflict` (in progress)
- Existing key with `CONSUMED`: `200 OK` with cached response
- First successful execution: `201 Created`

---

## Solution 2: Parallel-Safe Atomic Key Claim
### Design
Prevent race conditions with two layers:
- Application-level mutex/critical section
- DB `UNIQUE` constraint on `idempotency_key`

### Handling
- Winning request claims key and executes business logic.
- Losing concurrent request hits unique violation, then:
  - returns `409 Conflict` if first request still `CREATED`, or
  - returns cached response if first request already `CONSUMED`.

### Result
- Exactly one order is created for a key.

---

## Solution 3: Multi-Server Safe with Distributed Cache (Redis)
### Why DB-Only Fails in Distributed Setup
Per-server DB replication may lag seconds/minutes, so retries can reach a server with stale state.

### Design
Make shared cache the idempotency authority:
1. Always check Redis before any DB access.
2. Claim key atomically via `SETNX` + TTL (e.g., 24h).
3. Set `status=CREATED` while processing.
4. On success, set `status=CONSUMED` and store response.
5. Replay cached response from Redis for retries.

### Behavior Across Servers
- Retry on a different server sees the same key quickly via Redis replication.
- Server returns cached response and skips DB write.
- DB replication lag no longer drives duplicate processing.

### Response Behavior
- Cache miss + successful claim: process request and return `201 Created`
- Cache hit `CREATED`: `409 Conflict` (request in progress)
- Cache hit `CONSUMED`: `200 OK` with cached response

---

## Recommended Contract
For `POST` operations that create resources:
- Header required: `Idempotency-Key: <uuid-v4>`
- Key uniqueness scope: per endpoint + tenant/user context
- Key TTL: 24 hours (or business-defined replay window)
- Stored value: request hash, status, response body, status code, timestamps

Optional hardening:
- Reject same key with different payload hash (`422 Unprocessable Entity`)
- Add exponential backoff guidance for `409`
- Add observability: key-claim latency, conflicts, replay rate, duplicate-prevented count

---

## End-to-End Processing Blueprint
1. Validate header present and format.
2. Compute request hash.
3. Read idempotency record from shared store (Redis preferred for multi-server).
4. If `CONSUMED`, replay stored response.
5. If `CREATED`, return `409`.
6. If absent, atomic claim (`SETNX` / insert with unique key).
7. Execute business operation once.
8. Persist result and set record to `CONSUMED`.
9. Return `201` (first call) or replayed `200` (retry).

---

## Final Conclusion
The documented idempotency failures all come from one pattern: **state visibility gaps plus non-atomic key handling**.

The complete fix is:
- atomic key claim,
- clear key lifecycle (`CREATED` -> `CONSUMED`),
- response replay,
- and in distributed deployments, a **shared low-latency cache as idempotency source of truth**.

With this design, the same business intent is processed exactly once, even with retries, parallel submits, and cross-server routing.

