# Low Level Design of Parking Slot System

This document is a complete, topic-wise Low Level Design (LLD) guide for a Parking Slot system.
It is structured for interview preparation and practical implementation discussion.

It covers:
- problem understanding
- functional and non-functional requirements
- entities and object model
- class and interface design
- design patterns
- slot allocation and release flow
- entry/exit, ticketing, pricing, and payment
- reservations, cancellation, and overbooking prevention
- concurrency, scaling, observability, and security
- interview questions and sample answers

---

## 1) What is a Parking Slot System?

A Parking Slot System manages parking operations for one or more parking lots by handling:
- slot discovery
- slot assignment
- ticket generation
- entry/exit processing
- billing and payment
- occupancy tracking

In production, a robust system also handles:
- dynamic pricing
- reservations
- no-show and cancellation
- lost ticket handling
- device integration (barrier, camera, RFID)
- analytics and audit history

---

## 2) Typical Interview Prompt

"Design a Parking Slot System that supports multiple floors, vehicle types, ticketing, payment, and slot allocation strategies."

Interviewers usually expect:
- clean domain modeling
- extensible allocation strategy
- robust state transitions
- concurrency safety for slot assignment
- practical handling of edge cases (lost ticket, double exit, retries)

---

## 3) Functional Requirements

Core requirements:
1. Add parking lots, floors, and slots.
2. Support vehicle categories (BIKE, CAR, SUV, EV, TRUCK).
3. Park vehicle and allocate suitable slot.
4. Generate parking ticket at entry.
5. Compute charges at exit.
6. Process payment and release slot.
7. Track slot occupancy and availability.
8. Query available slots by type/floor.
9. Maintain entry/exit and payment history.
10. Prevent duplicate allocation of same slot.

Optional advanced requirements:
- pre-book/reserve slot
- valet mode
- dynamic pricing by time or demand
- subscription/season pass users
- EV charging slot constraints
- license plate recognition integration

---

## 4) Non-Functional Requirements

Key NFRs:
- correctness of slot allocation and billing
- low latency for entry/exit gate operations
- high availability for active lots
- scalability across multiple parking lots/cities
- strong auditability for disputes
- idempotent API behavior for retries

Reliability expectation:
- no double assignment of same slot
- no double payment for same ticket
- resilient behavior on partial failures (e.g., payment success but gate signal failure)

---

## 5) Clarifying Questions to Ask in Interview

1. Single lot or multi-lot system?
2. Walk-in only or reservations too?
3. Pricing fixed, slab-based, or dynamic?
4. Is payment mandatory before barrier opens?
5. Do we support monthly subscriptions?
6. Is there an offline mode for gate devices?
7. Should we store images/plate scans?
8. Do we need real-time dashboard updates?
9. Is overbooking allowed for reservations?
10. Which payment methods are required?

---

## 6) Core Domain Concepts

- `ParkingLot`
- `Floor`
- `ParkingSlot`
- `Vehicle`
- `ParkingTicket`
- `EntryRecord`
- `ExitRecord`
- `PricingPolicy`
- `Payment`
- `Reservation`
- `AllocationStrategy`
- `SlotStatus`

---

## 7) Core Entities and Responsibilities

### 7.1 ParkingLot
Represents a physical lot.

Fields:
- `lotId`
- `name`
- `address`
- `floors`
- `active`

### 7.2 Floor
Represents one floor/zone in lot.

Fields:
- `floorId`
- `lotId`
- `label`
- `slots`

### 7.3 ParkingSlot
Represents a single slot.

Fields:
- `slotId`
- `floorId`
- `slotNumber`
- `supportedVehicleTypes`
- `status`
- `hasCharger`
- `reserved`

### 7.4 Vehicle
Represents vehicle entering lot.

Fields:
- `vehicleId`
- `licensePlate`
- `vehicleType`
- `ownerId`

### 7.5 ParkingTicket
Represents active parking session.

Fields:
- `ticketId`
- `lotId`
- `slotId`
- `vehicleId`
- `entryTime`
- `exitTime`
- `status`
- `totalAmount`

### 7.6 Reservation
Represents pre-booked slot.

Fields:
- `reservationId`
- `userId`
- `lotId`
- `vehicleType`
- `reservedFrom`
- `reservedTo`
- `status`
- `assignedSlotId`

### 7.7 Payment
Represents payment transaction.

Fields:
- `paymentId`
- `ticketId`
- `amount`
- `method`
- `status`
- `transactionRef`
- `paidAt`

---

## 8) Enums Commonly Used

### VehicleType
- BIKE
- CAR
- SUV
- EV
- TRUCK

### SlotStatus
- AVAILABLE
- OCCUPIED
- RESERVED
- BLOCKED
- OUT_OF_SERVICE

### TicketStatus
- ACTIVE
- PAYMENT_PENDING
- PAID
- CLOSED
- LOST
- CANCELLED

### ReservationStatus
- CREATED
- CONFIRMED
- CHECKED_IN
- EXPIRED
- CANCELLED
- NO_SHOW

### PaymentStatus
- INITIATED
- SUCCESS
- FAILED
- REFUNDED

---

## 9) End-to-End Flow (Entry to Exit)

1. Vehicle reaches entry gate.
2. System validates lot and vehicle type.
3. Allocation engine finds suitable slot.
4. Slot is atomically marked `OCCUPIED` (or reserved claim confirmed).
5. Ticket is created with entry timestamp.
6. Barrier opens and vehicle enters.
7. At exit, system fetches active ticket.
8. Pricing engine calculates payable amount.
9. Payment is captured.
10. Ticket is marked `PAID` and then `CLOSED`.
11. Slot is released to `AVAILABLE`.
12. Exit barrier opens and exit event is recorded.

---

## 10) Recommended Class Design

### 10.1 Orchestrator

#### `ParkingService`
Responsibilities:
- accept entry request
- allocate slot
- create ticket
- process exit flow
- release slot

Methods:
- `ParkingTicket park(ParkRequest request)`
- `ExitSummary unpark(UnparkRequest request)`
- `SlotView getAvailability(String lotId)`

### 10.2 Allocation

#### `SlotAllocationStrategy`
Responsibilities:
- choose best slot based on policy

Methods:
- `Optional<ParkingSlot> allocate(AllocationContext context)`

Implementations:
- `NearestEntryStrategy`
- `LowestFloorStrategy`
- `LeastUsedStrategy`

### 10.3 Pricing

#### `PricingPolicy`
Responsibilities:
- compute fare from duration and rules

Methods:
- `Money calculate(TicketSnapshot ticket, PricingContext context)`

Implementations:
- `FlatRatePricingPolicy`
- `SlabPricingPolicy`
- `DynamicDemandPricingPolicy`

### 10.4 Payment

#### `PaymentService`
Responsibilities:
- create payment intent
- capture payment
- handle retries and callback

Methods:
- `PaymentResult pay(PaymentRequest request)`
- `void refund(String paymentId)`

### 10.5 Reservation

#### `ReservationService`
Responsibilities:
- create/update/cancel reservation
- enforce availability window
- mark no-show

Methods:
- `Reservation create(CreateReservationRequest request)`
- `Reservation checkIn(String reservationId)`
- `void cancel(String reservationId)`

---

## 11) Interface Definitions (Conceptual)

### `SlotRepository`
- `Optional<ParkingSlot> findAvailable(String lotId, VehicleType type)`
- `boolean occupyIfAvailable(String slotId, String ticketId)`
- `void release(String slotId)`

### `TicketRepository`
- `ParkingTicket save(ParkingTicket ticket)`
- `Optional<ParkingTicket> findActiveByPlate(String licensePlate)`
- `ParkingTicket update(ParkingTicket ticket)`

### `PricingPolicy`
- `Money calculate(TicketSnapshot ticket, PricingContext context)`

### `IdempotencyService`
- `boolean tryCreate(String key)`
- `Optional<ExitSummary> getExisting(String key)`

---

## 12) Design Patterns Used

Best-fit patterns:
- Strategy (allocation, pricing)
- Factory (payment provider, strategy resolution)
- State (ticket/reservation lifecycle)
- Template Method (entry/exit flow orchestration)
- Observer (events for analytics, alerts)

Why Strategy is important:
- allocation logic changes by lot/business policy
- pricing logic changes by region, peak hours, event days

---

## 13) Validation Rules

- license plate required and valid format
- lot must exist and be active
- slot must support vehicle type
- one active ticket per vehicle per lot (unless policy allows)
- exit only for active ticket
- payment amount must match computed amount (or approved variance)
- reservation window validity checks

---

## 14) Slot Allocation Design Options

### Option A: Query DB every entry
Pros:
- simple consistency model

Cons:
- slower under high throughput

### Option B: In-memory availability index + DB confirmation
Pros:
- faster lookups

Cons:
- index consistency complexity

Interview recommendation:
- keep DB atomic update as source of truth (`occupyIfAvailable` CAS style)
- optionally use cache/index for read optimization

---

## 15) Ticketing Design

Ticket should capture immutable entry details and mutable lifecycle state.

Suggested fields:
- `ticketId`
- `vehiclePlate`
- `vehicleType`
- `entryGateId`
- `entryTime`
- `slotId`
- `status`
- `pricingVersion`

Important:
- Keep pricing rule version in ticket for dispute-proof billing.

---

## 16) Pricing Design

Common policies:
- flat per hour
- slab pricing (0-2h one rate, 2-6h another)
- day/night different rates
- weekend/event multiplier

Edge cases:
- grace period (first 10 mins free)
- minimum billable duration
- lost ticket fixed fine
- max daily cap

---

## 17) Payment Design

Flow:
1. Compute payable amount.
2. Create payment intent.
3. Capture payment.
4. Confirm success callback/webhook.
5. Mark ticket `PAID`.
6. Release slot and close ticket.

Failure handling:
- keep ticket in `PAYMENT_PENDING`
- retry capture for transient failures
- idempotency key for payment retries

---

## 18) Reservation Design

Reservation checks:
- time window availability
- vehicle type compatibility
- per-user reservation limit

Policy examples:
- hold slot for 15 minutes after reservation start
- mark `NO_SHOW` if not checked in
- cancellation fee rules based on time before start

---

## 19) Concurrency and Thread Safety

Common race conditions:
- two vehicles allocated same slot
- duplicate exit payment retries
- reservation check-in and cancellation overlap

Mitigation:
- atomic DB update for slot occupancy
- optimistic locking/version on slot/ticket rows
- idempotency keys for entry/exit/payment APIs
- transactional updates for ticket + slot + payment state

---

## 20) State Modeling (Important in Interviews)

### Ticket state transitions
`ACTIVE -> PAYMENT_PENDING -> PAID -> CLOSED`

Failure paths:
- `ACTIVE -> LOST`
- `PAYMENT_PENDING -> ACTIVE` (payment failed/retry)

### Reservation state transitions
`CREATED -> CONFIRMED -> CHECKED_IN`
or
`CONFIRMED -> EXPIRED/NO_SHOW/CANCELLED`

---

## 21) Data Model (Practical Schema Discussion)

### `parking_lots`
- lot_id, name, address, active, created_at

### `floors`
- floor_id, lot_id, label

### `parking_slots`
- slot_id, floor_id, slot_number, vehicle_types, status, has_charger, version

### `tickets`
- ticket_id, lot_id, slot_id, vehicle_plate, vehicle_type, entry_time, exit_time, status, pricing_version, amount

### `payments`
- payment_id, ticket_id, amount, method, status, provider_ref, created_at

### `reservations`
- reservation_id, user_id, lot_id, vehicle_type, from_time, to_time, status, assigned_slot_id

### `audit_events`
- event_id, entity_type, entity_id, event_type, payload, created_at

---

## 22) APIs / Method-Level Design

Entry and slot APIs:
- `POST /lots/{lotId}/park`
- `GET /lots/{lotId}/availability?vehicleType=CAR`

Exit and billing APIs:
- `POST /tickets/{ticketId}/checkout`
- `GET /tickets/{ticketId}/estimate`

Reservation APIs:
- `POST /reservations`
- `POST /reservations/{id}/checkin`
- `DELETE /reservations/{id}`

Payment APIs:
- `POST /payments`
- `POST /payments/{id}/confirm`

---

## 23) Error Handling and Idempotency

Idempotency points:
- `park` request retries
- `checkout` retries
- payment capture callbacks

Error categories:
- validation errors (4xx)
- business errors (slot unavailable, ticket closed)
- transient infra/provider errors (retryable)
- permanent external failures (non-retryable)

---

## 24) Observability and Auditability

Must-log fields:
- requestId/correlationId
- lotId, slotId, ticketId
- licensePlate hash/masked value
- pricing policy/version
- payment provider reference
- state transitions

Useful metrics:
- allocation latency
- lot occupancy percent
- checkout success rate
- payment failure rate
- no-show reservations
- revenue per lot/hour

---

## 25) Security and Compliance

- mask plate numbers in logs where needed
- protect payment tokens/credentials
- secure gate device communication
- rate limit APIs to prevent abuse
- maintain tamper-evident audit logs

---

## 26) Extensibility Discussion

How to add EV fast-charging specific logic:
- add slot capability metadata
- extend allocation strategy filter
- add charging fee component in pricing policy

How to add new payment provider:
- implement `PaymentGateway` interface
- plug into `PaymentGatewayFactory`

How to add valet flow:
- add valet operator and handoff states
- keep core ticketing and payment unchanged

---

## 27) Testing Strategy

Unit tests:
- slot allocation by vehicle type
- concurrency-safe occupy/release logic
- pricing slabs and edge boundaries
- reservation expiry and no-show transition
- idempotent checkout behavior

Integration tests:
- payment callback updates
- transactional exit flow
- repository optimistic lock behavior
- multi-floor availability read/write consistency

Load tests:
- burst entry scenarios
- simultaneous exits at peak hour
- dashboard availability refresh load

---

## 28) Common Mistakes in Parking Slot LLD

1. Directly assigning first available slot without atomic lock/update.
2. Mixing pricing logic inside controller/service orchestration.
3. Not storing pricing version used for final bill.
4. Ignoring idempotency for payment and checkout.
5. Releasing slot before confirmed payment (if policy disallows).
6. Single status field without transition discipline/audit trail.
7. No clear handling for lost tickets and no-shows.

---

## 29) Interview Answer Structure (Recommended)

1. Clarify assumptions (single vs multi-lot, pricing model).
2. Define entities and states.
3. Explain allocation strategy abstraction.
4. Explain entry and exit flows.
5. Explain pricing and payment separation.
6. Address concurrency and idempotency.
7. Cover observability and failure handling.
8. Explain extensibility for EV/reservation/valet.

---

## 30) Topic-Wise Interview Q&A

### Q1. How do you avoid double slot assignment?
Use atomic `occupyIfAvailable(slotId)` with optimistic/pessimistic lock and verify affected rows = 1.

### Q2. Why separate `PricingPolicy` from `ParkingService`?
Pricing changes frequently and should not force orchestration code changes.

### Q3. How do you support multiple lots and strategies?
Keep lot-specific configuration to choose strategy implementation at runtime.

### Q4. How do you handle payment success but exit barrier failure?
Persist payment success, mark ticket paid, emit retry command for barrier open, and alert operator.

### Q5. What if a vehicle loses ticket?
Support `LOST` flow with alternate identity checks and lost-ticket pricing policy.

### Q6. How do reservations affect live availability?
Reserved slots remain unavailable to walk-ins for hold window; released on expiry/no-show.

### Q7. What is the role of audit events?
To reconstruct disputed billing/allocation timelines and support compliance reporting.

### Q8. Which SOLID principles matter most?
SRP (separate allocation/pricing/payment), OCP (add strategies/policies), DIP (depend on interfaces).

---

## 31) Example Verbal Design Answer

"I model the system around `ParkingService` orchestrating entry and exit workflows. Slot selection is delegated to `SlotAllocationStrategy`, pricing to `PricingPolicy`, and payment to `PaymentService` through gateway abstractions. Every entry creates a `ParkingTicket`, and every exit updates ticket, payment, and slot states transactionally. I enforce atomic slot occupancy and idempotent checkout to avoid double allocation and double charging. Reservation and dynamic pricing are added via policy abstractions so core orchestration remains stable and extensible."

---

## 32) Quick Cheat Sheet

- Separate `Ticket`, `Slot`, `Payment`, `Reservation` models.
- Use Strategy for allocation and pricing.
- Keep DB atomic update as source of truth for occupancy.
- Track explicit state transitions and audit trail.
- Add idempotency for entry, checkout, and payment callback.
- Keep payment provider integration behind abstractions.
- Design for retries, partial failures, and operator recovery.

---

## 33) Final One-Liner

"A strong Parking Slot LLD separates allocation, ticketing, pricing, and payment concerns while enforcing atomic occupancy, idempotent checkout, and auditable state transitions."

---

## 34) Closing Guidance

In interviews, do not stop at "assign slot and generate ticket".
Strong answers cover:
- exact state transitions,
- race condition handling,
- pricing/payment separation,
- idempotency and retries,
- and extensibility for reservations, EV charging, and multi-lot scale.

That is what makes the design production-oriented and interview-ready.


