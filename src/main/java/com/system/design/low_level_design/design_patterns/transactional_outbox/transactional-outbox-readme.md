# Transactional Outbox Pattern (Interview Notes)

This package contains a runnable Java example: `TransactionalOutboxPatternExample`.

## Problem: Why dual-write is risky

A common flow is:
1. Save order in database
2. Publish `OrderCreated` event to message broker

If the app crashes between step 1 and step 2, the order exists in DB but no event is published.
That causes data mismatch across services.

## Pattern idea

Inside one DB transaction:
1. Save business data (order)
2. Save event into `outbox` table with `PENDING` status

Then a separate relay process:
1. Polls pending outbox rows
2. Publishes to broker
3. Marks row as `PUBLISHED` (or increments retry count on failure)

This ensures the event is never lost if order commit succeeds.

## Components in this sample

- `OrderApplicationService`
  - `placeOrderDualWriteUnsafe(...)` shows anti-pattern.
  - `placeOrderWithOutbox(...)` writes order + outbox event atomically.
- `InMemoryDatabase`
  - Simulates a transactional DB with copy-on-write commit.
- `OutboxRelay`
  - Polls pending events and publishes to broker with retry on failure.
- `InMemoryMessageBroker`
  - Simulates broker and transient publish failure.
- `OrderCreatedConsumer`
  - Demonstrates idempotent consumption using processed event IDs.

## How to explain in interview (short script)

1. "Dual write is unsafe because DB and broker are separate systems."
2. "With transactional outbox, order row and outbox row commit together."
3. "A relay asynchronously publishes outbox rows to broker."
4. "Failures are retried because outbox row stays pending."
5. "Consumers are idempotent for at-least-once delivery semantics."

## Run the demo

From repository root:

```powershell
.\gradlew.bat test
.\gradlew.bat run -PmainClass=com.system.design.low_level_design.design_patterns.transactional_outbox.TransactionalOutboxPatternExample
```

If the `run` task is not configured in your Gradle setup, run the class from the IDE directly.

