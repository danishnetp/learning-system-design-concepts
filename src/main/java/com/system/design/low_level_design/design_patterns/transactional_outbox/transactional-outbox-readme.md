# Transactional Outbox Pattern (Interview Notes)

This package contains a runnable Java example: `TransactionalOutboxPatternExample`.

> High-level architecture diagram (GitHub renderable): [`transactional-outbox-high-level-diagram.md`](./transactional-outbox-high-level-diagram.md)

## 1) Problem first (what interviewers expect)

### Problem statement
In a microservice, one business action often needs **two side effects**:
1. Save data in DB (for example, create order)
2. Publish event to broker (for example, `OrderCreated`)

These are two different systems. They do not share one transaction boundary.

### Why this is hard
If you do naive dual-write:
1. DB commit succeeds
2. App crashes before broker publish

Then order exists, but event is missing. Downstream services never receive it.

If you reverse order:
1. Publish succeeds
2. DB commit fails

Then consumers react to an order that does not exist.

This is the classic **dual-write inconsistency** problem.

---

## 2) Solution first (short answer)

Use **Transactional Outbox**:

Inside one DB transaction:
1. Save business row (`orders`)
2. Save outbox row (`outbox_events`, status = `PENDING`)

After commit, a relay worker:
1. Reads pending outbox rows
2. Publishes events to broker
3. Marks outbox row as `PUBLISHED` (or increments retry count)

So if DB commit succeeds, the event is guaranteed to be eventually published.

---

## 3) Interview-ready explanation (60 seconds)

"Dual-write is unsafe because DB and broker are separate systems. Transactional outbox writes business data and event record in the same DB transaction, so both commit atomically. A relay process later publishes pending outbox events to the broker and marks them published. Since relay retries on failure, events are not lost. Because delivery is at-least-once, consumers must be idempotent using event IDs."

---

## 4) Components in this sample

- `OrderApplicationService`
  - `placeOrderDualWriteUnsafe(...)`: anti-pattern demonstration.
  - `placeOrderWithOutbox(...)`: order + outbox row in one transaction.
- `InMemoryDatabase`
  - Simulates transactional commit using copy-on-write.
- `OutboxRelay`
  - Polls pending events, publishes, and updates status/retry count.
- `InMemoryMessageBroker`
  - Simulates transient broker failure.
- `OrderCreatedConsumer`
  - Demonstrates idempotent consumption (duplicate-safe).

---

## 5) What interviewers usually ask (and expected points)

### Q1. Why not 2PC / distributed transaction?
- 2PC adds latency and operational complexity.
- Many brokers do not support XA cleanly in real production usage.
- Outbox is simpler, more resilient, and widely adopted.

### Q2. Does outbox give exactly-once delivery?
- Usually **no** at system level.
- Relay and broker typically provide **at-least-once**.
- You achieve correctness via **idempotent consumer** logic.

### Q3. How do you avoid duplicate processing?
- Include stable `eventId`.
- Consumer keeps processed event IDs (DB/Redis/table).
- If seen before, skip side effects.

### Q4. How does relay scale safely?
- Batch polling.
- Row locking strategy like `FOR UPDATE SKIP LOCKED`.
- Multiple relay instances can run in parallel.

### Q5. What happens when publish fails repeatedly?
- Keep retry count.
- Apply exponential backoff.
- Move to dead-letter queue/table after max retries.
- Alert on stuck pending rows.

### Q6. Why is ordering tricky?
- Across multiple aggregates, global order is hard.
- Usually guarantee ordering per aggregate key (`orderId`, `userId`).

### Q7. How do you clean old outbox rows?
- Archive/delete `PUBLISHED` rows periodically.
- Keep retention window for debugging/auditing.

---

## 6) Pros and trade-offs

### Pros
- Prevents lost events when DB commit succeeds.
- No distributed transaction coordinator required.
- Practical and production-proven design.

### Trade-offs
- Adds outbox table and relay process.
- Event delivery is asynchronous (eventual consistency).
- Requires idempotency and monitoring discipline.

---

## 7) Operational checklist (real-world)

- Outbox table indexed by `status`, `created_at`.
- Relay metrics: publish success/failure, lag, retries.
- Alert if pending backlog grows beyond threshold.
- DLQ strategy for poison events.
- Cleanup job for old published rows.
- Trace correlation IDs across request -> outbox -> broker -> consumer.

---

## 8) Common anti-patterns to mention in interview

- Publishing directly to broker in same request after DB commit.
- Non-idempotent consumers (duplicate emails/charges).
- No retry cap and no DLQ.
- No observability (cannot detect stuck outbox).

---

## 9) Run the demo

From repository root:

```powershell
.\gradlew.bat test
.\gradlew.bat run -PmainClass=com.system.design.low_level_design.design_patterns.transactional_outbox.TransactionalOutboxPatternExample
```

If the `run` task is not configured in your Gradle setup, run the class from the IDE directly.
