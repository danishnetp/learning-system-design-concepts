# Handling Transactions in Distributed Systems

In a monolith, one database transaction is usually enough.
In a distributed system, a single business action can involve multiple services and databases, so transaction handling becomes much harder.

## Why Transactions Are Hard in Distributed Environments

When service boundaries increase, these challenges appear:
- Network failures and timeouts are common.
- Partial success can happen (service A commits, service B fails).
- Different services may use different databases.
- Global locking reduces availability and throughput.

So, distributed systems choose between:
- **Strong consistency** (harder, slower), or
- **Eventual consistency** (faster, more resilient, but requires compensation).

## Transaction Types in Distributed Systems

## 1) Local Transaction (Single Service, Single DB)

A classic ACID transaction inside one database.

**When to use**
- Logic is inside one service.
- No cross-service side effects.

**Example**
- Update `account_balance` and insert `account_statement` in same DB transaction.

---

## 2) One-Phase Commit (1PC)

Each participant commits independently without a distributed coordinator.

**How it works**
- Service performs steps in sequence.
- If a later step fails, earlier commits are already permanent.

**Advantages**
- Very simple.
- Low coordination overhead.

**Disadvantages**
- Not atomic across services.
- High risk of inconsistent state on partial failure.

**Example**
- Order service saves order, then Payment service charge fails.
- Result: order exists but unpaid.

---

## 3) Two-Phase Commit (2PC)

A coordinator ensures all participants either commit or abort together.

### Phase 1: Prepare (Voting)
- Coordinator asks all participants: "Can you commit?"
- Participants lock resources and reply `YES` or `NO`.

### Phase 2: Commit/Rollback
- If all `YES` -> coordinator sends `COMMIT`.
- If any `NO` or timeout -> coordinator sends `ROLLBACK`.

**Advantages**
- Atomic behavior across multiple services/databases.
- Strong consistency model.

**Disadvantages**
- Blocking protocol: participants can hold locks for long time.
- Coordinator failure can stall progress.
- Poor fit for high-latency microservices.

**Use Cases**
- Banking-like systems needing strict correctness.
- Controlled enterprise systems with limited scale.

**Example**
- Transfer money between Bank-A DB and Bank-B DB.
- Both `prepare` succeeds -> both commit.
- If one fails prepare -> both rollback.

---

## 4) Three-Phase Commit (3PC)

3PC tries to reduce blocking in 2PC by adding an extra phase.

### Phase 1: CanCommit
- Coordinator asks participants if they can commit.

### Phase 2: PreCommit
- Coordinator tells participants to pre-commit (ready state).

### Phase 3: DoCommit
- Final commit signal is sent.

**Advantages**
- Less blocking than 2PC in some failure scenarios.
- Better timeout handling than plain 2PC.

**Disadvantages**
- More complex and more network round trips.
- Rarely used in modern cloud microservices.
- Still cannot solve all partition edge cases.

**Use Cases**
- Specialized distributed DB protocols.
- Systems that can tolerate added complexity for stronger coordination.

**Example**
- Multi-node transaction coordinator in a private datacenter where strict orchestration is required.

---

## 5) Saga Pattern (Most Common in Microservices)

A **Saga** breaks one global transaction into a sequence of local transactions.
If one step fails, previously completed steps are undone via **compensating transactions**.

Two styles:
- **Choreography**: services emit/consume events (no central orchestrator).
- **Orchestration**: a central Saga orchestrator calls each step.

**Advantages**
- No global lock across services.
- High availability and scalability.
- Fits event-driven microservices.

**Disadvantages**
- Eventual consistency (temporary inconsistency is possible).
- Compensation logic is complex.
- Requires idempotency and good observability.

**Use Cases**
- E-commerce checkout.
- Travel booking (flight + hotel + cab).
- Subscription lifecycle workflows.

**Example: E-commerce Order Saga**
1. Create order (`OrderService`).
2. Reserve inventory (`InventoryService`).
3. Charge payment (`PaymentService`).
4. Arrange shipment (`ShippingService`).

If payment fails:
- Compensate inventory reservation (release stock).
- Mark order as `PAYMENT_FAILED`.

---

## 6) TCC (Try-Confirm-Cancel)

TCC is a stricter form of Saga for reservation-based operations.

- **Try**: reserve resources tentatively.
- **Confirm**: finalize reservation.
- **Cancel**: release reservation if flow fails.

**Advantages**
- Clear semantics for business operations.
- Better control for high-value operations.

**Disadvantages**
- Every service must implement Try/Confirm/Cancel APIs.
- Higher implementation cost.

**Use Cases**
- Wallet, coupon, seat booking, inventory reservation.

**Example**
- Try: reserve 2 seats in theater.
- Confirm: ticket payment success -> confirm seats.
- Cancel: payment timeout -> release seats.

---

## 7) Transactional Outbox + Inbox (Reliable Messaging)

Used when DB update and event publish must be reliable without 2PC.

**Outbox pattern**
- Write business data + outbox event in one local DB transaction.
- Background worker publishes outbox events to message broker.

**Inbox/idempotent consumer**
- Consumer stores processed message IDs.
- Duplicate events are ignored safely.

**Advantages**
- Avoids distributed XA transaction.
- Reliable event delivery with eventual consistency.

**Disadvantages**
- Adds async complexity and retry handling.
- Requires monitoring for outbox lag.

**Use Cases**
- Order created -> publish `OrderCreated` reliably.
- Payment captured -> publish `PaymentCompleted` reliably.

## Comparison Summary

| Pattern | Consistency | Latency | Complexity | Typical Fit |
|---|---|---|---|---|
| 1PC | Weak | Low | Low | Simple flows, tolerant to inconsistency |
| 2PC | Strong | High | Medium-High | Strict atomic multi-resource commit |
| 3PC | Strong-ish | Higher | High | Specialized coordination systems |
| Saga | Eventual | Medium | Medium-High | Microservices business workflows |
| TCC | Eventual (controlled) | Medium | High | Reservation-based business operations |
| Outbox/Inbox | Eventual | Medium | Medium | Reliable event-driven architecture |

## Detailed End-to-End Examples

## Example 1: Bank Transfer (Strict Consistency)

Goal: transfer `100` from Account A to Account B.

**With 2PC**
1. Coordinator asks A-DB and B-DB to `prepare`.
2. Both lock rows and vote `YES`.
3. Coordinator sends `commit`.
4. A debited and B credited atomically.

If B votes `NO`, coordinator sends rollback to both.

## Example 2: Food Delivery Checkout (Saga Orchestration)

Steps:
1. Create order.
2. Reserve restaurant slot.
3. Authorize payment.
4. Assign delivery partner.

Failure scenario:
- Delivery partner assignment fails.

Compensation:
- Cancel payment authorization.
- Release restaurant slot.
- Mark order failed.

## Example 3: Hotel + Flight Booking (TCC)

Try phase:
- Reserve flight seat.
- Reserve hotel room.

Confirm phase:
- Customer payment success -> confirm both.

Cancel phase:
- Payment fails or timeout -> cancel both reservations.

## Example 4: Inventory Update with Outbox

1. `InventoryService` decreases stock and inserts `StockReduced` into outbox table in one local DB transaction.
2. Outbox worker publishes event to broker.
3. `OrderService` consumes event and records message ID in inbox table.
4. If duplicate event arrives, consumer skips it.

Result:
- No distributed lock.
- Reliable event processing.

## Important Design Rules

- Keep operations **idempotent** (safe retries).
- Use **timeouts + retry with backoff** for remote calls.
- Add **dead-letter queues** for poison messages.
- Track **transaction state machine** explicitly (`PENDING`, `CONFIRMED`, `COMPENSATED`).
- Use **correlation IDs** for tracing end-to-end flows.

## Common Interview Explanation Structure

When asked "How do you handle distributed transactions?", answer in this order:
1. Clarify consistency requirement (strict vs eventual).
2. If strict atomicity is mandatory, discuss 2PC trade-offs.
3. For microservices, propose Saga/TCC with compensations.
4. Add outbox/inbox for reliable async communication.
5. Mention idempotency, retries, monitoring, and failure recovery.

## Practical Decision Guide

- Need strict cross-resource atomicity and can accept blocking -> **2PC**.
- Need high scalability/availability in microservices -> **Saga**.
- Need explicit reservation lifecycle -> **TCC**.
- Need reliable event publishing from local transactions -> **Outbox/Inbox**.
- Avoid 1PC for critical money/state flows unless inconsistency is acceptable.

