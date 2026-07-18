package com.system.design.low_level_design.design_patterns.transactional_outbox;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Interview-friendly, runnable sample of the <strong>Transactional Outbox</strong> pattern.
 *
 * <h2>Pattern Summary</h2>
 * <p>When a service must <em>both</em> persist data in a database <em>and</em> publish a message
 * to a broker (e.g. Kafka, RabbitMQ), a naïve "dual-write" approach is unsafe: the application can
 * crash between the DB commit and the broker publish, leaving the two systems permanently out of
 * sync.</p>
 *
 * <p>The Transactional Outbox pattern solves this by writing the business record <strong>and</strong>
 * an outbox event record inside the <em>same</em> DB transaction.  A separate relay process
 * (or CDC connector) then reads the outbox table and forwards events to the broker.</p>
 *
 * <h2>What this demo shows</h2>
 * <ol>
 *   <li>Why direct dual-write is unsafe (DB update + broker publish in two separate calls).</li>
 *   <li>How writing order + outbox event atomically avoids message loss.</li>
 *   <li>How an outbox relay retries failed broker publishes without data loss.</li>
 *   <li>Why consumers must be <em>idempotent</em> under at-least-once delivery.</li>
 * </ol>
 *
 * <h2>Components</h2>
 * <ul>
 *   <li>{@link OrderApplicationService} – business logic layer (safe and unsafe variants).</li>
 *   <li>{@link InMemoryDatabase} – simulated relational DB with transactional semantics.</li>
 *   <li>{@link OutboxRelay} – background relay that polls {@code PENDING} outbox rows.</li>
 *   <li>{@link InMemoryMessageBroker} – simulated message broker with fault injection.</li>
 *   <li>{@link OrderCreatedConsumer} – idempotent downstream consumer.</li>
 * </ul>
 *
 * <h2>How to run</h2>
 * <p>Execute {@link #main(String[])} directly from an IDE.  The output shows each step of the
 * pattern and the final outcome of the {@link DemoReport}.</p>
 *
 * <h2>Quick interview script</h2>
 * <ol>
 *   <li>"Dual-write is unsafe because DB and broker are separate systems – a crash in-between
 *       silently drops the event."</li>
 *   <li>"The outbox pattern writes the business row and an outbox row inside one DB transaction,
 *       so both succeed or both roll back – atomicity guaranteed."</li>
 *   <li>"A relay process polls pending outbox rows and pushes them to the broker, retrying on
 *       transient failures."</li>
 *   <li>"Because the relay may publish the same event more than once (at-least-once delivery),
 *       consumers must be idempotent – typically via a processed-event-ID set."</li>
 * </ol>
 */
public class TransactionalOutboxPatternExample {

    /**
     * Entry point.  Runs the demo and prints a human-readable report to {@code stdout}.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        DemoReport report = runDemo();

        System.out.println("================ Transactional Outbox Demo ================");
        System.out.println("1) Dual-write failure observed: " + report.dualWriteMessageLossObserved());
        System.out.println("2) Outbox pending after first relay attempt: " + report.pendingAfterFirstRelay());
        System.out.println("3) Published events after second relay attempt: " + report.publishedAfterSecondRelay());
        System.out.println("4) Consumer processed unique messages: " + report.consumerProcessedUniqueMessages());
        System.out.println("5) Consumer ignored duplicates: " + report.consumerIgnoredDuplicates());
        System.out.println("============================================================");
    }

    /**
     * Executes the full transactional outbox demonstration and returns a
     * {@link DemoReport} with assertions-ready numeric results.
     *
     * <p>Steps executed:
     * <ol>
     *   <li>Place an order via the <em>unsafe dual-write</em> path and verify the broker receives
     *       no event (message loss).</li>
     *   <li>Place an order via the <em>safe outbox</em> path so the event lands in the outbox
     *       table atomically.</li>
     *   <li>Simulate a transient broker failure during the first relay attempt; verify the event
     *       remains {@code PENDING}.</li>
     *   <li>Run a second relay attempt that succeeds; verify the event is marked
     *       {@code PUBLISHED}.</li>
     *   <li>Deliver the event twice to the consumer to demonstrate idempotency.</li>
     * </ol>
     *
     * @return a {@link DemoReport} capturing observed counts for verification
     */
    public static DemoReport runDemo() {
        InMemoryDatabase database = new InMemoryDatabase();
        InMemoryMessageBroker broker = new InMemoryMessageBroker();
        OrderApplicationService orderService = new OrderApplicationService(database);
        OutboxRelay relay = new OutboxRelay(database, broker, 10);
        OrderCreatedConsumer consumer = new OrderCreatedConsumer();

        // ── Step 1: Anti-pattern ─────────────────────────────────────────────────
        // placeOrderDualWriteUnsafe commits the order to the DB but never publishes
        // to the broker (simulating a crash / missing publish call).
        String lostOrderId = orderService.placeOrderDualWriteUnsafe("req-1", "cust-a", 1200);
        boolean messageLossObserved = database.totalOrders() == 1 && broker.totalMessages("order-events") == 0;

        // ── Step 2: Safe pattern – outbox write inside the same transaction ───────
        String safeOrderId = orderService.placeOrderWithOutbox("req-2", "cust-b", 3500);
        if (lostOrderId.equals(safeOrderId)) {
            throw new IllegalStateException("Order ids should differ for demo clarity");
        }

        // ── Step 3: First relay attempt – transient broker failure ────────────────
        // The broker is configured to reject the next publish call.
        // The relay catches the exception, increments retryCount, and leaves status PENDING.
        broker.setFailNextPublish(true);
        relay.publishPendingOnce();
        int pendingAfterFirstRelay = database.totalPendingOutboxEvents();

        // ── Step 4: Second relay attempt – succeeds ───────────────────────────────
        // The broker now accepts the message; the relay marks the outbox row PUBLISHED.
        relay.publishPendingOnce();
        int publishedAfterSecondRelay = database.totalPublishedOutboxEvents();

        // ── Step 5: Idempotent consumer ───────────────────────────────────────────
        // Each message is delivered twice.  The consumer tracks processed IDs so only
        // the first delivery is handled; the duplicate is silently dropped.
        List<Message> messages = broker.getMessages("order-events");
        int uniqueProcessed = 0;
        int duplicatesIgnored = 0;
        for (Message message : messages) {
            if (consumer.consume(message)) {
                uniqueProcessed++;
            } else {
                duplicatesIgnored++;
            }
            // Simulate duplicate delivery (at-least-once semantics).
            if (consumer.consume(message)) {
                uniqueProcessed++;
            } else {
                duplicatesIgnored++;
            }
        }

        return new DemoReport(
                messageLossObserved,
                pendingAfterFirstRelay,
                publishedAfterSecondRelay,
                uniqueProcessed,
                duplicatesIgnored
        );
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Result Record
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Immutable summary of the demo run, suitable for direct use in unit-test assertions.
     *
     * @param dualWriteMessageLossObserved {@code true} if the unsafe dual-write path produced an
     *                                     order in the DB but no event in the broker (message loss
     *                                     confirmed).
     * @param pendingAfterFirstRelay       number of outbox events still in {@code PENDING} status
     *                                     after the first (failed) relay attempt.
     * @param publishedAfterSecondRelay    number of outbox events in {@code PUBLISHED} status after
     *                                     the second (successful) relay attempt.
     * @param consumerProcessedUniqueMessages number of distinct events the consumer accepted.
     * @param consumerIgnoredDuplicates    number of duplicate deliveries silently dropped by the
     *                                     idempotent consumer.
     */
    public record DemoReport(
            boolean dualWriteMessageLossObserved,
            int pendingAfterFirstRelay,
            int publishedAfterSecondRelay,
            int consumerProcessedUniqueMessages,
            int consumerIgnoredDuplicates
    ) {}

    // ═══════════════════════════════════════════════════════════════════════════════
    // Domain Enums
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Lifecycle state of an outbox event row.
     *
     * <ul>
     *   <li>{@link #PENDING} – written by the application service; not yet forwarded to the
     *       broker.</li>
     *   <li>{@link #PUBLISHED} – successfully forwarded by the relay; safe to archive or
     *       delete.</li>
     * </ul>
     */
    private enum OutboxStatus {
        /**
         * The event has been committed to the outbox table and is waiting for the relay
         * to forward it to the message broker.
         */
        PENDING,

        /**
         * The relay has successfully published this event to the message broker.
         * The row can now be archived or cleaned up by a housekeeping job.
         */
        PUBLISHED
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Domain Records / Entities
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Represents an order created by a customer.
     *
     * <p>In a real system this would be a JPA {@code @Entity} or a document in a NoSQL store.
     * Here it is an immutable record stored in-memory.
     *
     * @param orderId    unique identifier, e.g. {@code "ORD-1001"}.
     * @param customerId identifier of the customer who placed the order.
     * @param amount     total order amount in the smallest currency unit (e.g. paise / cents).
     * @param createdAt  wall-clock time when the order was persisted.
     */
    private record Order(String orderId, String customerId, int amount, Instant createdAt) {
    }

    /**
     * A single row in the <em>outbox table</em> – the heart of the pattern.
     *
     * <p>This class is intentionally immutable.  State transitions (e.g. marking an event as
     * published) produce a new instance, mirroring an SQL {@code UPDATE} statement that returns
     * the updated row.</p>
     *
     * <p><strong>Real-world DB schema equivalent:</strong>
     * <pre>
     * CREATE TABLE outbox_events (
     *   event_id      VARCHAR PRIMARY KEY,
     *   aggregate_id  VARCHAR NOT NULL,
     *   event_type    VARCHAR NOT NULL,
     *   payload       TEXT    NOT NULL,
     *   created_at    TIMESTAMP NOT NULL,
     *   status        VARCHAR NOT NULL DEFAULT 'PENDING',
     *   retry_count   INT     NOT NULL DEFAULT 0
     * );
     * </pre>
     */
    private static final class OutboxEvent {

        /** Globally unique identifier for this event instance (e.g. {@code "EVT-5001"}). */
        private final String eventId;

        /**
         * Identifier of the aggregate that produced this event (e.g. an {@code orderId}).
         * Used by consumers to correlate events to business entities.
         */
        private final String aggregateId;

        /**
         * Logical event type name (e.g. {@code "OrderCreated"}, {@code "PaymentProcessed"}).
         * Consumers use this to route the event to the correct handler.
         */
        private final String eventType;

        /**
         * Serialised event body.  In production this would be JSON or Avro/Protobuf bytes.
         * Here it is a simple key=value string for readability.
         */
        private final String payload;

        /** Wall-clock time when this outbox row was originally written. */
        private final Instant createdAt;

        /**
         * Current lifecycle status – {@link OutboxStatus#PENDING} until the relay successfully
         * delivers the event to the broker, at which point it becomes
         * {@link OutboxStatus#PUBLISHED}.
         */
        private final OutboxStatus status;

        /**
         * Number of times the relay has attempted (and failed) to publish this event.
         * Can be used to implement exponential back-off or a dead-letter threshold.
         */
        private final int retryCount;

        /**
         * Full constructor – all fields required.
         *
         * @param eventId      unique event identifier
         * @param aggregateId  owning aggregate identifier
         * @param eventType    logical event type name
         * @param payload      serialised event body
         * @param createdAt    creation timestamp
         * @param status       current outbox status
         * @param retryCount   number of failed delivery attempts so far
         */
        private OutboxEvent(
                String eventId,
                String aggregateId,
                String eventType,
                String payload,
                Instant createdAt,
                OutboxStatus status,
                int retryCount) {
            this.eventId = eventId;
            this.aggregateId = aggregateId;
            this.eventType = eventType;
            this.payload = payload;
            this.createdAt = createdAt;
            this.status = status;
            this.retryCount = retryCount;
        }

        /**
         * Factory method that creates a new outbox event in the {@link OutboxStatus#PENDING}
         * state with {@code retryCount = 0}.
         *
         * <p>Call this inside the same DB transaction that persists the business entity.
         *
         * @param eventId     unique event identifier
         * @param aggregateId owning aggregate identifier
         * @param eventType   logical event type name
         * @param payload     serialised event body
         * @return a fresh {@code OutboxEvent} ready to be inserted into the outbox table
         */
        private static OutboxEvent pending(String eventId, String aggregateId, String eventType, String payload) {
            return new OutboxEvent(eventId, aggregateId, eventType, payload, Instant.now(), OutboxStatus.PENDING, 0);
        }

        /**
         * Returns a copy of this event with status changed to {@link OutboxStatus#PUBLISHED}.
         *
         * <p>Called by the relay after a successful broker publish.  In SQL this maps to:
         * <pre>UPDATE outbox_events SET status = 'PUBLISHED' WHERE event_id = ?</pre>
         *
         * @return a new {@code OutboxEvent} with status {@code PUBLISHED}
         */
        private OutboxEvent markPublished() {
            return new OutboxEvent(eventId, aggregateId, eventType, payload, createdAt, OutboxStatus.PUBLISHED, retryCount);
        }

        /**
         * Returns a copy of this event with {@code retryCount} incremented by one.
         *
         * <p>Called by the relay when a broker publish throws an exception.  The event stays
         * {@link OutboxStatus#PENDING} so the next relay poll will pick it up again.  In SQL:
         * <pre>UPDATE outbox_events SET retry_count = retry_count + 1 WHERE event_id = ?</pre>
         *
         * @return a new {@code OutboxEvent} with incremented retry count
         */
        private OutboxEvent markRetry() {
            return new OutboxEvent(eventId, aggregateId, eventType, payload, createdAt, OutboxStatus.PENDING, retryCount + 1);
        }
    }

    /**
     * Represents a message as seen by a broker topic consumer.
     *
     * <p>The {@code eventId} doubles as the idempotency key: consumers store it to detect
     * duplicate deliveries that are normal under at-least-once delivery guarantees.
     *
     * @param eventId   globally unique event identifier, matches {@link OutboxEvent#eventId}.
     * @param eventType logical event type name, e.g. {@code "OrderCreated"}.
     * @param payload   serialised event body forwarded verbatim from the outbox row.
     */
    public record Message(String eventId, String eventType, String payload) {
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Infrastructure – In-Memory Database
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Simulates a relational database with transactional semantics using copy-on-write maps.
     *
     * <p>The {@link #executeInTransaction(Function)} method copies the current state, passes a
     * {@link Transaction} view to the caller's unit-of-work lambda, and – if no exception is
     * thrown – promotes the copies back to the live state.  This mimics a simple
     * <em>read-committed</em> transaction without rollback logic (sufficient for demo purposes).
     *
     * <p>All public methods are {@code synchronized} to make the simulation thread-safe when
     * the relay runs on a separate thread in production-like scenarios.
     */
    private static final class InMemoryDatabase {

        /** Stores all persisted {@link Order} instances, keyed by {@code orderId}. */
        private Map<String, Order> orders = new LinkedHashMap<>();

        /**
         * The outbox table: stores {@link OutboxEvent} rows keyed by {@code eventId}.
         * Both {@code PENDING} and {@code PUBLISHED} rows live here until archived.
         */
        private Map<String, OutboxEvent> outboxEvents = new LinkedHashMap<>();

        /**
         * Idempotency index: maps a client-supplied {@code requestId} to the order that was
         * created for it.  Prevents duplicate orders on retry.
         */
        private Map<String, String> requestToOrderId = new HashMap<>();

        /**
         * Executes a unit of work inside a simulated database transaction.
         *
         * <p>The caller receives a {@link Transaction} scoped to snapshot copies of all tables.
         * Changes made through the {@code Transaction} are committed atomically when the lambda
         * returns normally.  Any unchecked exception aborts the "transaction" (changes discarded).
         *
         * @param <T>        the return type of the unit of work
         * @param unitOfWork lambda that performs reads and writes against the transaction scope
         * @return the value returned by {@code unitOfWork}
         */
        public synchronized <T> T executeInTransaction(Function<Transaction, T> unitOfWork) {
            Map<String, Order> ordersCopy = new LinkedHashMap<>(orders);
            Map<String, OutboxEvent> outboxCopy = new LinkedHashMap<>(outboxEvents);
            Map<String, String> requestCopy = new HashMap<>(requestToOrderId);

            T result = unitOfWork.apply(new Transaction(ordersCopy, outboxCopy, requestCopy));

            // Commit: promote snapshot back to live state.
            orders = ordersCopy;
            outboxEvents = outboxCopy;
            requestToOrderId = requestCopy;
            return result;
        }

        /**
         * Returns up to {@code limit} outbox events whose status is {@link OutboxStatus#PENDING}.
         *
         * <p>This is the query executed by the relay on each polling iteration.  In a real system
         * this would be:
         * <pre>
         * SELECT * FROM outbox_events
         * WHERE status = 'PENDING'
         * ORDER BY created_at
         * LIMIT :limit
         * FOR UPDATE SKIP LOCKED;  -- prevents concurrent relay instances from double-processing
         * </pre>
         *
         * @param limit maximum number of rows to return per batch
         * @return snapshot list of pending outbox events (never {@code null})
         */
        public synchronized List<OutboxEvent> fetchPendingOutboxEvents(int limit) {
            List<OutboxEvent> pending = new ArrayList<>();
            for (OutboxEvent event : outboxEvents.values()) {
                if (event.status == OutboxStatus.PENDING) {
                    pending.add(event);
                }
                if (pending.size() == limit) {
                    break;
                }
            }
            return pending;
        }

        /**
         * Persists an updated {@link OutboxEvent} (replaces the existing row by {@code eventId}).
         *
         * <p>Used by the relay to transition an event from {@code PENDING} to {@code PUBLISHED}
         * (or to increment its {@code retryCount}).
         *
         * @param updated the updated outbox event instance; must share the same {@code eventId}
         *                as an existing row
         */
        public synchronized void updateOutboxEvent(OutboxEvent updated) {
            outboxEvents.put(updated.eventId, updated);
        }

        /**
         * Returns the total number of {@link Order} rows currently stored.
         *
         * @return count of all orders (including those placed via unsafe dual-write)
         */
        public synchronized int totalOrders() {
            return orders.size();
        }

        /**
         * Returns the count of outbox events in {@link OutboxStatus#PENDING} status.
         *
         * @return number of events waiting to be forwarded to the broker
         */
        public synchronized int totalPendingOutboxEvents() {
            int count = 0;
            for (OutboxEvent event : outboxEvents.values()) {
                if (event.status == OutboxStatus.PENDING) {
                    count++;
                }
            }
            return count;
        }

        /**
         * Returns the count of outbox events in {@link OutboxStatus#PUBLISHED} status.
         *
         * @return number of events successfully forwarded to the broker
         */
        public synchronized int totalPublishedOutboxEvents() {
            int count = 0;
            for (OutboxEvent event : outboxEvents.values()) {
                if (event.status == OutboxStatus.PUBLISHED) {
                    count++;
                }
            }
            return count;
        }
    }

    /**
     * A scoped, mutable view of the database state used within a single transaction.
     *
     * <p>Mutations applied to the maps held by this record are visible to the database only after
     * the enclosing {@link InMemoryDatabase#executeInTransaction(Function)} call completes
     * successfully.  This mimics a transactional buffer (write-set) in a real RDBMS.
     *
     * @param orders    mutable copy of the orders table for this transaction
     * @param outbox    mutable copy of the outbox table for this transaction
     * @param requests  mutable copy of the request-idempotency index for this transaction
     */
    private record Transaction(
            Map<String, Order> orders,
            Map<String, OutboxEvent> outbox,
            Map<String, String> requests
    ) {
        /**
         * Looks up an existing {@code orderId} by the idempotency {@code requestId}.
         *
         * <p>Returns {@link Optional#empty()} if no prior order was created for this request,
         * meaning the caller should proceed with order creation.
         *
         * @param requestId client-supplied idempotency key
         * @return {@code Optional} containing the previously-created order ID, or empty
         */
        private Optional<String> getOrderIdByRequest(String requestId) {
            return Optional.ofNullable(requests.get(requestId));
        }

        /**
         * Inserts or updates an {@link Order} in the transaction's order table copy.
         *
         * @param order the order to persist
         */
        private void saveOrder(Order order) {
            orders.put(order.orderId(), order);
        }

        /**
         * Inserts an {@link OutboxEvent} into the transaction's outbox table copy.
         *
         * <p>This call must always be paired with {@link #saveOrder(Order)} within the same
         * transaction – that is the core invariant of the Transactional Outbox pattern.
         *
         * @param event the outbox event to insert; should have status {@link OutboxStatus#PENDING}
         */
        private void saveOutboxEvent(OutboxEvent event) {
            outbox.put(event.eventId, event);
        }

        /**
         * Records the mapping from an idempotency request key to the created order ID.
         *
         * <p>Subsequent calls with the same {@code requestId} will return the existing order
         * instead of creating a duplicate, achieving idempotent order placement.
         *
         * @param requestId client-supplied idempotency key
         * @param orderId   the order ID created for this request
         */
        private void linkRequestToOrder(String requestId, String orderId) {
            requests.put(requestId, orderId);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Application Service
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Application service responsible for order placement.
     *
     * <p>Exposes two variants:
     * <ul>
     *   <li>{@link #placeOrderDualWriteUnsafe(String, String, int)} – the <em>anti-pattern</em>
     *       that only writes the order to the DB and skips the broker publish.</li>
     *   <li>{@link #placeOrderWithOutbox(String, String, int)} – the <em>correct</em>
     *       implementation that writes the order and outbox event atomically.</li>
     * </ul>
     */
    private static final class OrderApplicationService {

        /** Underlying database used for transactional persistence. */
        private final InMemoryDatabase database;

        /** Monotonically increasing counter used to generate unique order IDs. */
        private final AtomicInteger orderSequence = new AtomicInteger(1000);

        /** Monotonically increasing counter used to generate unique outbox event IDs. */
        private final AtomicInteger eventSequence = new AtomicInteger(5000);

        /**
         * Constructs the service with a required database dependency.
         *
         * @param database in-memory (or real) database to use for persistence
         */
        private OrderApplicationService(InMemoryDatabase database) {
            this.database = database;
        }

        /**
         * <strong>Anti-pattern</strong>: places an order using unsafe dual-write.
         *
         * <p>The method commits the {@link Order} to the database but never publishes an event
         * to the message broker.  In a real implementation the broker publish would follow the
         * transaction commit – and a crash between those two steps would cause <em>permanent
         * message loss</em>.
         *
         * <p>This method demonstrates the problem so that the correct approach (outbox) can be
         * contrasted against it.
         *
         * @param requestId  client idempotency key; prevents duplicate orders on retry
         * @param customerId identifier of the customer placing the order
         * @param amount     total order amount
         * @return the {@code orderId} of the newly created (or previously created) order
         */
        public String placeOrderDualWriteUnsafe(String requestId, String customerId, int amount) {
            return database.executeInTransaction(tx -> {
                Optional<String> alreadyCreated = tx.getOrderIdByRequest(requestId);
                if (alreadyCreated.isPresent()) {
                    return alreadyCreated.get();
                }

                String orderId = "ORD-" + orderSequence.incrementAndGet();
                tx.saveOrder(new Order(orderId, customerId, amount, Instant.now()));
                tx.linkRequestToOrder(requestId, orderId);
                return orderId;
            });
            // ⚠ A crash could happen here before broker publish → message is silently lost.
        }

        /**
         * <strong>Correct pattern</strong>: places an order using the Transactional Outbox.
         *
         * <p>Both the {@link Order} row and the {@link OutboxEvent} row are inserted inside
         * the <em>same</em> database transaction.  Either both are committed or neither is –
         * the two systems can never diverge due to a mid-flight crash.
         *
         * <p>The outbox row will be picked up by the {@link OutboxRelay} on its next polling
         * cycle and forwarded to the message broker.
         *
         * @param requestId  client idempotency key; prevents duplicate orders on retry
         * @param customerId identifier of the customer placing the order
         * @param amount     total order amount
         * @return the {@code orderId} of the newly created (or previously created) order
         */
        public String placeOrderWithOutbox(String requestId, String customerId, int amount) {
            return database.executeInTransaction(tx -> {
                Optional<String> alreadyCreated = tx.getOrderIdByRequest(requestId);
                if (alreadyCreated.isPresent()) {
                    return alreadyCreated.get();
                }

                String orderId = "ORD-" + orderSequence.incrementAndGet();
                String eventId = "EVT-" + eventSequence.incrementAndGet();

                // ✅ Order + outbox event committed atomically – no message loss possible.
                tx.saveOrder(new Order(orderId, customerId, amount, Instant.now()));

                String payload = "orderId=" + orderId + ",customerId=" + customerId + ",amount=" + amount;
                tx.saveOutboxEvent(OutboxEvent.pending(eventId, orderId, "OrderCreated", payload));

                tx.linkRequestToOrder(requestId, orderId);
                return orderId;
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Infrastructure – Outbox Relay
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Background relay (sometimes called the <em>outbox worker</em> or <em>message relay</em>)
     * that polls the outbox table and forwards pending events to the message broker.
     *
     * <h3>Responsibilities</h3>
     * <ol>
     *   <li>Fetch a bounded batch of {@link OutboxStatus#PENDING} events from the database.</li>
     *   <li>Publish each event to the broker topic.</li>
     *   <li>On success: mark the row {@link OutboxStatus#PUBLISHED}.</li>
     *   <li>On failure: increment {@code retryCount} and leave the row {@code PENDING} so the
     *       next polling cycle retries it.</li>
     * </ol>
     *
     * <h3>Production considerations</h3>
     * <ul>
     *   <li>Use {@code SELECT ... FOR UPDATE SKIP LOCKED} so multiple relay instances do not
     *       compete for the same rows (prevents duplicate publishes within the relay tier).</li>
     *   <li>Apply an exponential back-off strategy based on {@code retryCount}.</li>
     *   <li>Move rows that exceed a {@code maxRetries} threshold to a dead-letter table.</li>
     *   <li>Archive or delete {@code PUBLISHED} rows periodically to keep the table small.</li>
     *   <li>Alternatively, replace the polling relay with a CDC tool (e.g. Debezium) that
     *       streams DB write-ahead-log changes directly to the broker – zero polling overhead.</li>
     * </ul>
     */
    private static final class OutboxRelay {

        /** Database instance used to fetch and update outbox rows. */
        private final InMemoryDatabase database;

        /** Message broker to which events are forwarded. */
        private final InMemoryMessageBroker broker;

        /**
         * Maximum number of outbox rows to process in a single polling invocation.
         * Limits memory usage and keeps relay latency bounded.
         */
        private final int batchSize;

        /**
         * Constructs the relay with its dependencies.
         *
         * @param database  database from which pending outbox events are fetched
         * @param broker    message broker to which events are published
         * @param batchSize maximum rows to process per {@link #publishPendingOnce()} call
         */
        private OutboxRelay(InMemoryDatabase database, InMemoryMessageBroker broker, int batchSize) {
            this.database = database;
            this.broker = broker;
            this.batchSize = batchSize;
        }

        /**
         * Executes one polling cycle: fetches up to {@link #batchSize} pending outbox events
         * and attempts to publish each to the broker.
         *
         * <p>Successful publishes transition the outbox row to {@link OutboxStatus#PUBLISHED}.
         * Failed publishes (any {@link RuntimeException}) leave the row {@link OutboxStatus#PENDING}
         * with an incremented {@code retryCount} so the next cycle can retry.
         *
         * <p>In production this method would be invoked on a fixed-rate schedule (e.g. every
         * 500 ms) or triggered by a CDC change event.
         */
        public void publishPendingOnce() {
            List<OutboxEvent> batch = database.fetchPendingOutboxEvents(batchSize);
            for (OutboxEvent event : batch) {
                try {
                    broker.publish("order-events", new Message(event.eventId, event.eventType, event.payload));
                    database.updateOutboxEvent(event.markPublished());
                } catch (RuntimeException ex) {
                    // Transient failure – keep the row PENDING and let the next cycle retry it.
                    database.updateOutboxEvent(event.markRetry());
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Infrastructure – In-Memory Message Broker
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Simulates a message broker (Kafka / RabbitMQ / SNS) using in-memory topic maps.
     *
     * <p>Supports controlled fault injection via {@link #setFailNextPublish(boolean)} to
     * demonstrate relay retry behaviour without needing a real broker in tests.
     *
     * <p>All methods are {@code synchronized} for thread safety.
     */
    private static final class InMemoryMessageBroker {

        /**
         * Topic storage: maps topic name → ordered list of messages published to that topic.
         * Ordering mirrors a log-based broker like Kafka.
         */
        private final Map<String, List<Message>> topics = new HashMap<>();

        /**
         * When {@code true}, the next call to {@link #publish(String, Message)} will throw a
         * {@link RuntimeException} and reset this flag to {@code false}, simulating a single
         * transient network failure.
         */
        private boolean failNextPublish;

        /**
         * Controls whether the next publish call should simulate a transient failure.
         *
         * <p>Set to {@code true} before a relay attempt to test retry behaviour.
         * The flag is automatically cleared after the first failed publish.
         *
         * @param failNextPublish {@code true} to inject a failure on the next publish call
         */
        public synchronized void setFailNextPublish(boolean failNextPublish) {
            this.failNextPublish = failNextPublish;
        }

        /**
         * Publishes a message to the specified topic.
         *
         * <p>If {@link #failNextPublish} is {@code true} the method throws a
         * {@link RuntimeException} (simulating a transient broker error) and resets the flag.
         * Otherwise the message is appended to the topic's message list.
         *
         * @param topic   name of the destination topic (e.g. {@code "order-events"})
         * @param message the message to publish
         * @throws RuntimeException if fault injection is active for this call
         */
        public synchronized void publish(String topic, Message message) {
            if (failNextPublish) {
                failNextPublish = false;
                throw new RuntimeException("Transient broker error – simulated network failure");
            }

            topics.computeIfAbsent(topic, unused -> new ArrayList<>()).add(message);
        }

        /**
         * Returns a snapshot copy of all messages published to the given topic.
         *
         * <p>The returned list is independent of the internal store – mutations are not reflected.
         *
         * @param topic topic name
         * @return list of messages in insertion order; empty list if topic has no messages
         */
        public synchronized List<Message> getMessages(String topic) {
            return new ArrayList<>(topics.getOrDefault(topic, List.of()));
        }

        /**
         * Returns the total number of messages currently held in the given topic.
         *
         * @param topic topic name
         * @return message count; {@code 0} if the topic does not exist
         */
        public synchronized int totalMessages(String topic) {
            return topics.getOrDefault(topic, List.of()).size();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Downstream Consumer
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Idempotent downstream consumer for {@code OrderCreated} events.
     *
     * <h3>Why idempotency matters</h3>
     * <p>The outbox relay guarantees <em>at-least-once</em> delivery: if the relay crashes after
     * publishing an event but before marking it {@code PUBLISHED}, it will re-publish the same
     * event on the next cycle.  Consumers that are <em>not</em> idempotent will process the
     * same order twice – potentially sending duplicate emails, charging the customer twice, or
     * decrementing inventory twice.</p>
     *
     * <h3>Implementation strategy</h3>
     * <p>The simplest strategy is to maintain a set of already-processed event IDs (persisted in
     * Redis, a DB table, or a Bloom filter for high throughput).  On receipt of a message the
     * consumer checks the set first:
     * <ul>
     *   <li>If the ID is <em>absent</em> → process and record the ID.</li>
     *   <li>If the ID is <em>present</em> → silently skip (duplicate detected).</li>
     * </ul>
     *
     * <p>In this demo the set is in-memory, which suffices for a single JVM example.
     */
    private static final class OrderCreatedConsumer {

        /**
         * Set of event IDs that have already been successfully processed.
         *
         * <p>In production this would be a persistent store (e.g. a Redis set or a
         * {@code processed_events} DB table) so deduplication survives consumer restarts.
         */
        private final Set<String> processedEventIds = new HashSet<>();

        /**
         * Attempts to process the given message.
         *
         * <p>If the message's {@code eventId} has not been seen before, it is recorded and the
         * method returns {@code true} (message processed).  If the ID is already in the
         * processed set the message is a duplicate and the method returns {@code false} without
         * performing any side-effects (idempotent drop).
         *
         * @param message the message received from the broker
         * @return {@code true} if the message was processed for the first time;
         *         {@code false} if it was a duplicate and was safely ignored
         */
        public boolean consume(Message message) {
            if (!processedEventIds.add(message.eventId())) {
                // Duplicate detected – idempotent drop.
                return false;
            }
            // First delivery – perform business logic here (e.g. send confirmation email).
            return true;
        }
    }
}

