# Low Level Design of Notification Service

This document provides a topic-wise Low Level Design (LLD) guide for a Notification Service. It is written for interview preparation and practical design discussion.

It covers:
- problem understanding
- functional and non-functional requirements
- core entities and object model
- class design and interfaces
- design patterns used
- delivery flow for Email, SMS, Push, and In-App notifications
- retries, scheduling, deduplication, and rate limiting
- user preferences and template management
- extensibility and testing
- common interview questions and answers

---

## 1) What is a Notification Service?

A Notification Service is a module or subsystem responsible for sending messages to users through one or more channels such as:
- Email
- SMS
- Push Notification
- In-App Notification
- Webhook

Its job is not just to "send messages". A production-grade notification service also handles:
- user preferences
- templates
- channel selection
- retries
- provider failure
- scheduling
- audit/history
- idempotency
- rate limiting

In interviews, this problem is popular because it tests:
- object-oriented design
- use of abstractions
- extensibility for new channels/providers
- modeling of workflows and states
- practical tradeoffs

---

## 2) Interview Problem Statement

### Typical interview prompt
"Design a Notification Service that can send notifications through multiple channels like Email, SMS, and Push. The system should support templates, user preferences, retries, and future extensibility."

### What interviewers usually want to see
- how you model a notification request
- how you support multiple channels cleanly
- how you plug in different providers
- how you store delivery history and statuses
- how you prevent duplication and spamming
- how you support future channels without rewriting everything

---

## 3) Functional Requirements

Typical functional requirements:

1. Send notification to a user.
2. Support multiple channels:
   - Email
   - SMS
   - Push
   - In-App
3. Support different notification types:
   - transactional
   - promotional
   - security
   - system alerts
4. Use templates with placeholders.
5. Respect user preferences and opt-in/opt-out rules.
6. Support retries on temporary failures.
7. Support scheduled notifications.
8. Maintain delivery history and status.
9. Prevent duplicate notifications for the same event.
10. Allow adding new channels/providers easily.

Optional advanced requirements:
- fallback from one channel to another
- priority-based delivery
- bulk notifications
- throttling/rate limiting
- localization
- A/B template selection
- callback/webhook handling from providers

---

## 4) Non-Functional Requirements

### Q1. What non-functional requirements matter most here?
**Answer:**
- high reliability for transactional messages
- extensibility for new channels/providers
- low coupling between business logic and providers
- observability and auditability
- controlled retries and backoff
- good throughput for bulk messaging
- idempotency to avoid duplicates

### Q2. Is low latency always the top priority?
**Answer:**
Not always. Transactional notifications like OTP or password reset are latency-sensitive, but promotional or digest notifications can be asynchronous and throughput-oriented.

### Q3. Why is audit history important?
**Answer:**
Because teams often need to know:
- whether a notification was created
- whether it was attempted
- whether it was delivered or failed
- which provider/channel was used
- why it failed

---

## 5) Clarifying Questions to Ask in an Interview

Before designing, ask:

1. Which channels are required initially?
2. Is the system synchronous, asynchronous, or both?
3. Do we need templates?
4. Do users have per-channel preferences?
5. Do we need retries and fallback?
6. Do we need scheduling?
7. Do we need provider pluggability?
8. Do we need bulk sends?
9. Do we need deduplication or idempotency?
10. Do we need delivery tracking callbacks?

These questions help you avoid under-design and over-design.

---

## 6) Core Domain Concepts

The most common domain concepts are:

- `User`
- `NotificationRequest`
- `Notification`
- `NotificationChannel`
- `NotificationType`
- `Template`
- `TemplateEngine`
- `Preference`
- `Provider`
- `DeliveryAttempt`
- `RetryPolicy`
- `Scheduler`
- `NotificationStatus`

---

## 7) Core Entities and Their Responsibilities

### 7.1 User
Represents the receiver.

Typical fields:
- `userId`
- `email`
- `phoneNumber`
- `deviceTokens`
- `locale`

### 7.2 NotificationRequest
Represents the incoming send request from a caller.

Typical fields:
- `requestId`
- `userId`
- `notificationType`
- `templateId`
- `channels`
- `payload` or `variables`
- `priority`
- `scheduledAt`
- `idempotencyKey`

### 7.3 Notification
Represents the internal notification object created by the service.

Typical fields:
- `notificationId`
- `requestId`
- `recipient`
- `channel`
- `content`
- `status`
- `createdAt`
- `updatedAt`

### 7.4 Template
Defines reusable message content with placeholders.

Typical fields:
- `templateId`
- `name`
- `channel`
- `subject`
- `body`
- `locale`
- `active`

### 7.5 UserPreference
Stores opt-in / opt-out and channel-level permissions.

Typical fields:
- `userId`
- `notificationType`
- `channel`
- `enabled`
- `quietHours`

### 7.6 Provider
Represents external delivery integration.

Examples:
- SMTP provider
- Twilio for SMS
- Firebase for push

### 7.7 DeliveryAttempt
Tracks one send attempt.

Typical fields:
- `attemptId`
- `notificationId`
- `providerName`
- `attemptNumber`
- `status`
- `errorCode`
- `errorMessage`
- `attemptedAt`

---

## 8) Enums Commonly Used

### NotificationChannel
- EMAIL
- SMS
- PUSH
- IN_APP
- WEBHOOK

### NotificationType
- TRANSACTIONAL
- PROMOTIONAL
- SECURITY
- SYSTEM_ALERT

### NotificationStatus
- CREATED
- QUEUED
- PROCESSING
- SENT
- DELIVERED
- FAILED
- RETRY_SCHEDULED
- CANCELLED
- SUPPRESSED

### Priority
- LOW
- MEDIUM
- HIGH
- CRITICAL

---

## 9) High-Level Low-Level Flow

The usual object-level flow is:

1. Caller sends `NotificationRequest`
2. `NotificationService` validates request
3. Service checks idempotency / deduplication
4. Service fetches user and preferences
5. Service resolves channels to use
6. Service loads template
7. `TemplateEngine` renders content
8. Service creates internal `Notification`
9. Notification is queued or sent immediately
10. Channel-specific sender invokes provider
11. Result is stored in history / attempt table
12. Retry or fallback happens if needed

---

## 10) Recommended Class Design

Below is a practical LLD breakdown.

### 10.1 Main orchestrator

#### `NotificationService`
Responsibilities:
- validate request
- check preferences
- choose channels
- render content
- delegate sending
- persist notification state

Possible methods:
- `send(NotificationRequest request)`
- `schedule(NotificationRequest request)`
- `retry(String notificationId)`
- `cancel(String notificationId)`

### 10.2 Channel abstraction

#### `NotificationSender`
An interface for sending via one channel.

Example methods:
- `SendResult send(Notification notification)`
- `NotificationChannel getChannel()`

Concrete implementations:
- `EmailNotificationSender`
- `SmsNotificationSender`
- `PushNotificationSender`
- `InAppNotificationSender`

### 10.3 Provider abstraction

#### `ChannelProvider`
An abstraction over external vendors.

Example methods:
- `ProviderResponse deliver(Message message)`
- `boolean supports(NotificationChannel channel)`

Concrete implementations:
- `SmtpEmailProvider`
- `SesEmailProvider`
- `TwilioSmsProvider`
- `FirebasePushProvider`

### 10.4 Template handling

#### `TemplateService`
Responsibilities:
- fetch template by id/type/channel/locale
- manage active templates

#### `TemplateEngine`
Responsibilities:
- replace placeholders
- build final subject/body

### 10.5 Preference handling

#### `PreferenceService`
Responsibilities:
- fetch user preferences
- decide whether a notification is allowed
- check quiet hours / opt-out

### 10.6 Repository layer

Repositories typically include:
- `NotificationRepository`
- `TemplateRepository`
- `PreferenceRepository`
- `DeliveryAttemptRepository`
- `IdempotencyRepository`

### 10.7 Retry support

#### `RetryPolicy`
Responsibilities:
- decide if retry is allowed
- compute next retry time
- track max retry count

### 10.8 Scheduling support

#### `NotificationScheduler`
Responsibilities:
- store jobs for future delivery
- release due notifications for processing

---

## 11) Suggested Interface Definitions Conceptually

You do not need to write full code in the interview, but you should be able to describe interfaces like these:

### `NotificationSender`
- `SendResult send(Notification notification)`
- `NotificationChannel channel()`

### `TemplateEngine`
- `RenderedTemplate render(Template template, Map<String, Object> variables)`

### `PreferenceService`
- `boolean isAllowed(User user, NotificationType type, NotificationChannel channel)`

### `RetryPolicy`
- `boolean canRetry(Notification notification, int attemptCount)`
- `Duration nextBackoff(int attemptCount)`

### `IdempotencyService`
- `boolean tryCreate(String key)`
- `Optional<NotificationResult> getExistingResult(String key)`

These show strong abstraction and extensibility.

---

## 12) Design Patterns Used

### Q1. Which patterns fit this problem well?
**Answer:**
Best-fit patterns include:
- Strategy
- Factory
- Template Method (optional)
- Observer/Event-Driven (optional)
- Builder
- Decorator (optional)

### Q2. Why Strategy Pattern is useful here?
**Answer:**
Because channel-specific send behavior differs.

Example:
- Email sender builds subject/body and uses email provider
- SMS sender validates short content and uses SMS provider
- Push sender uses title/body/device token

All can still implement the same `NotificationSender` interface.

### Q3. Why Factory Pattern is useful here?
**Answer:**
Factory can return the right sender/provider based on channel or provider name.

Example:
- `NotificationSenderFactory.getSender(EMAIL)`

### Q4. Where can Builder Pattern help?
**Answer:**
Builder is useful when creating complex notification objects with optional fields like:
- subject
- body
- metadata
- attachments
- priority
- schedule time

### Q5. Where can Decorator Pattern help?
**Answer:**
Decorator can wrap a sender with cross-cutting behavior such as:
- logging
- metrics
- rate limiting
- retry handling

### Q6. Should we use Observer Pattern?
**Answer:**
Observer is useful when notification creation is triggered by domain events such as:
- order placed
- payment failed
- password changed

But the core notification sending module itself usually centers more on Strategy and Factory.

---

## 13) Class Responsibility Breakdown

### `NotificationService`
Should coordinate the workflow, but should not contain all logic inline.

It should delegate to:
- validator
- preference service
- template service
- sender factory
- repository
- retry policy

### `NotificationValidator`
Checks:
- missing user/channel/template
- unsupported channel
- missing required payload placeholders
- invalid schedule time

### `NotificationSenderFactory`
Returns the right sender implementation based on channel.

### `ProviderResolver`
Chooses the external provider for a channel.

### `DeliveryTracker`
Records attempts and final status.

This separation prevents `NotificationService` from becoming a god object.

---

## 14) Sample Object Model Discussion

One good interview explanation is:

"I will keep `NotificationRequest` as an input DTO, convert it into one or more internal `Notification` objects, and route each one to a channel-specific `NotificationSender`. Senders depend on provider abstractions, not concrete vendors. Templates and preferences are separate services so the orchestration logic remains clean and extensible."

That answer is simple and strong.

---

## 15) Channel-Specific Considerations

### 15.1 Email Notifications

Typical fields:
- to
- cc / bcc
- subject
- body
- html/text
- attachments

Special considerations:
- long content allowed
- HTML rendering
- provider bounce callbacks
- attachment handling
- email validation

### 15.2 SMS Notifications

Typical fields:
- phone number
- message body

Special considerations:
- short message length
- per-country format/validation
- cost sensitivity
- provider throttling
- delivery receipt callback

### 15.3 Push Notifications

Typical fields:
- device token
- title
- body
- data payload

Special considerations:
- device token expiry
- mobile platform differences
- silent vs visible notification
- collapse keys / dedupe behavior

### 15.4 In-App Notifications

Typical fields:
- user id
- title
- message
- action URL
- read/unread state

Special considerations:
- persistence in DB
- pagination and unread count
- mark-as-read support
- real-time delivery via websocket if needed

---

## 16) Multi-Channel Notification Design

Sometimes one event needs more than one channel.

Example:
- send email + in-app
- try push first, fallback to SMS for critical alerts

Design options:

### Option A: One request creates multiple notification objects
Pros:
- each channel can be tracked independently
- status and retry become easier

Cons:
- more records to store

### Option B: One notification contains many channels internally
Pros:
- fewer top-level objects

Cons:
- status management becomes more complex

Interview recommendation:
- prefer separate channel-specific notification records for clean tracking and retries

---

## 17) Template Design

### Q1. Why use templates?
**Answer:**
Templates separate message content from sending logic. This improves reuse, maintainability, and localization support.

### Q2. What fields should a template have?
**Answer:**
Typical fields:
- `templateId`
- `name`
- `channel`
- `locale`
- `subject`
- `body`
- `version`
- `active`

### Q3. How does rendering work?
**Answer:**
The template contains placeholders such as:
- `{{userName}}`
- `{{otp}}`
- `{{orderId}}`

`TemplateEngine` replaces them with runtime variables.

### Q4. What if required placeholders are missing?
**Answer:**
Validation should fail before sending, or the render result should explicitly return an error. Sending broken content is worse than failing fast.

### Q5. Should templates be per channel?
**Answer:**
Usually yes, because each channel has different content style and limits.

---

## 18) User Preferences and Suppression Rules

### Q1. Why do we need preferences?
**Answer:**
Users may not want all kinds of notifications through all channels.

Examples:
- allow security emails, block promotional SMS
- allow push in daytime only
- disable marketing entirely

### Q2. What should preference checks support?
**Answer:**
- per notification type
- per channel
- global opt-out
- quiet hours
- locale preference

### Q3. Should transactional notifications respect opt-out?
**Answer:**
Usually promotional messages should respect strict opt-out. Critical security or transactional notifications may have different policy rules depending on business/legal requirements.

### Q4. What is suppression?
**Answer:**
Suppression means deliberately not sending a notification due to a rule such as:
- user opted out
- duplicate detected
- channel unavailable
- outside quiet hours

Suppressed notifications should often still be recorded for audit.

---

## 19) Idempotency and Deduplication

### Q1. Why is idempotency important?
**Answer:**
Because notification requests are often retried by upstream systems. Without idempotency, users may receive duplicate OTPs, duplicate order emails, or duplicate alerts.

### Q2. How do we prevent duplicate notifications?
**Answer:**
Common approaches:
- require an `idempotencyKey`
- store processed event ID
- maintain dedupe window for same user + type + payload hash

### Q3. What is the difference between idempotency and deduplication?
**Answer:**
- **Idempotency:** same request retried should return same result
- **Deduplication:** repeated equivalent notifications should be suppressed within a time window

### Q4. Where should idempotency be checked?
**Answer:**
At the entry point before creating new notification records.

### Q5. What should be stored for idempotency?
**Answer:**
- key
- request hash
- created notification IDs
- status/result snapshot
- timestamps / TTL

---

## 20) Retry and Failure Handling

### Q1. Why do we need retries?
**Answer:**
Because many provider failures are temporary:
- timeout
- transient network issue
- provider 5xx
- temporary throttling

### Q2. When should we retry?
**Answer:**
Retry only retryable failures, not permanent ones.

Retryable examples:
- timeout
- temporary unavailable
- rate limit exceeded

Non-retryable examples:
- invalid email address
- invalid phone number
- missing device token
- malformed template

### Q3. What retry strategy is good?
**Answer:**
Exponential backoff with jitter is usually a strong answer.

Example:
- retry after 1 min
- then 2 min
- then 5 min
- then 15 min

### Q4. Should retries be synchronous?
**Answer:**
Usually no. Retries are cleaner as scheduled/background jobs.

### Q5. What should happen after max retries?
**Answer:**
Mark the notification as failed permanently and record failure details. Optionally raise an alert or send to dead-letter processing.

---

## 21) Fallback Strategy

### Q1. What is fallback in a notification system?
**Answer:**
Fallback means trying an alternative path when the preferred path fails.

Examples:
- if push fails, try SMS for critical alerts
- if primary email provider fails, use backup email provider

### Q2. Channel fallback vs provider fallback?
**Answer:**
- **Provider fallback:** same channel, different vendor
- **Channel fallback:** completely different channel

### Q3. Should every notification have fallback?
**Answer:**
No. Fallback is useful mainly for high-priority or critical notifications because it increases cost and complexity.

---

## 22) Scheduling Notifications

### Q1. Why support scheduling?
**Answer:**
Because some notifications should be sent later, such as:
- reminders
- daily digests
- promotional campaigns
- renewal alerts

### Q2. What does the design need for scheduling?
**Answer:**
- `scheduledAt` field
- persistent storage of pending jobs
- scheduler/worker to release due notifications

### Q3. What edge cases matter for scheduling?
**Answer:**
- timezone handling
- cancellation before execution
- duplicate scheduled jobs
- missed execution after service restart

### Q4. Is in-memory scheduling enough?
**Answer:**
Only for toy examples. For production-style discussion, scheduled jobs should be persisted.

---

## 23) Rate Limiting and Throttling

### Q1. Why rate limit a notification service?
**Answer:**
To avoid:
- spamming users
- exceeding provider quotas
- burst overload
- cost blow-up

### Q2. What can be rate limited?
**Answer:**
- per user
- per channel
- per notification type
- per provider
- global tenant/account quota

### Q3. Give a practical example.
**Answer:**
- max 3 OTP SMS per 10 minutes per user
- max 1 promotional email per day per user
- provider quota 1000 SMS/minute

### Q4. Where should throttling logic live?
**Answer:**
Usually as a separate policy/service so senders stay focused on delivery.

---

## 24) Delivery Status Tracking

### Q1. Why is one "sent" status not enough?
**Answer:**
Because a notification may move through multiple stages:
- created
- queued
- processing
- sent to provider
- provider acknowledged
- delivered
- failed

### Q2. Why separate `SENT` and `DELIVERED`?
**Answer:**
`SENT` often means the provider accepted the request. `DELIVERED` means the end recipient likely received it based on callback/receipt.

### Q3. How do external callbacks fit in?
**Answer:**
Providers may later send delivery or failure callbacks. A callback handler should update notification status asynchronously.

---

## 25) Synchronous vs Asynchronous Design

### Q1. Should notification sending be synchronous?
**Answer:**
Usually the API accepts the request quickly and actual sending happens asynchronously, especially for email, push, or bulk operations.

### Q2. When might synchronous send still be acceptable?
**Answer:**
For simple or urgent use cases such as OTP in a small-scale system, but even then, robust systems usually offload sending to background processing.

### Q3. What is the LLD impact of async processing?
**Answer:**
You need:
- queue-friendly request model
- persisted status
- retry workers
- idempotent processing

---

## 26) Persistence Design

A practical schema discussion may include:

### `notifications`
Fields:
- notification_id
- request_id
- user_id
- channel
- type
- status
- template_id
- rendered_subject
- rendered_body
- priority
- scheduled_at
- created_at
- updated_at

### `delivery_attempts`
Fields:
- attempt_id
- notification_id
- provider
- attempt_number
- status
- error_code
- error_message
- created_at

### `user_preferences`
Fields:
- user_id
- type
- channel
- enabled
- quiet_hours_start
- quiet_hours_end

### `templates`
Fields:
- template_id
- name
- channel
- locale
- subject
- body
- version
- active

### `idempotency_records`
Fields:
- idempotency_key
- request_hash
- result_snapshot
- created_at
- expires_at

For LLD interviews, you do not need full SQL, but this level of modeling is strong.

---

## 27) API/Method-Level Design Discussion

Common service methods:

- `send(NotificationRequest request)`
- `sendBulk(List<NotificationRequest> requests)`
- `schedule(NotificationRequest request)`
- `getStatus(String notificationId)`
- `markAsRead(String notificationId)` for in-app
- `cancel(String notificationId)`
- `retry(String notificationId)`

Possible result object:

### `NotificationResult`
Fields:
- `notificationId`
- `status`
- `message`
- `channel`
- `provider`

---

## 28) Validation Rules

Examples of validations:

- user must exist
- at least one channel required
- template must exist and be active
- payload must satisfy required placeholders
- SMS must have a valid phone number
- push must have a valid device token
- scheduled time cannot be in invalid past if business rules disallow it
- priority must be within supported values

Validation should happen before expensive provider calls.

---

## 29) Concurrency and Thread Safety

### Q1. Where can concurrency bugs happen?
**Answer:**
- duplicate processing of same request
- retry worker and callback handler updating same notification
- concurrent status updates
- double scheduling

### Q2. How do you make the design safer?
**Answer:**
- idempotency key handling
- atomic status transitions
- optimistic locking/versioning
- careful retry ownership
- avoid shared mutable in-memory state when possible

### Q3. Should sender implementations be stateful?
**Answer:**
Prefer stateless sender classes where possible. Stateless components are easier to test and safer in concurrent processing.

---

## 30) Extensibility Discussion

### Q1. How would you add a new channel like WhatsApp?
**Answer:**
With a good design, you should only need to:
- add `WHATSAPP` enum value
- create `WhatsappNotificationSender`
- create provider integration
- update factory/resolver registration
- create templates and preference rules

The orchestration flow should not require major modification.

### Q2. How would you support multiple providers for Email?
**Answer:**
Add a provider abstraction and a provider selection strategy. Sender depends on the provider interface, not on a specific vendor.

### Q3. What if different business domains want custom notification rules?
**Answer:**
Introduce configurable policy abstractions such as:
- channel selection policy
- retry policy
- fallback policy
- rate-limit policy

---

## 31) Bulk Notification Design

### Q1. What changes when we need bulk notifications?
**Answer:**
Bulk notifications introduce scale-oriented concerns:
- one request may target many users
- template rendering may happen per recipient
- preference checks must happen per recipient
- failures must be tracked independently
- throughput and batching become important

### Q2. Should one bulk request create one notification record?
**Answer:**
Usually no. A bulk request is better treated as a parent job that creates child notification records per user and channel.

Suggested modeling:
- `BulkNotificationJob`
- `BulkNotificationItem`
- child `Notification` records

### Q3. Why split bulk jobs into child items?
**Answer:**
Because different recipients may:
- have different preferences
- have different template variables
- succeed/fail independently
- require different retry paths

### Q4. What batching ideas are worth mentioning in interviews?
**Answer:**
- batch DB writes where safe
- batch provider calls if provider supports it
- paginate recipient loading
- avoid loading all users into memory at once

---

## 32) Provider Callbacks and Webhook Handling

### Q1. Why do we need callback handling?
**Answer:**
Many providers accept a send request first and later send callbacks for:
- delivered
- bounced
- rejected
- clicked/opened
- unsubscribed

### Q2. What components are needed for callback handling?
**Answer:**
- `ProviderCallbackController` or handler
- `ProviderSignatureValidator`
- `ProviderEventMapper`
- `NotificationStatusUpdater`

### Q3. Why should callback processing be idempotent too?
**Answer:**
Providers may retry callbacks. Duplicate callback processing should not corrupt status history or create duplicate side effects.

### Q4. How do you map a provider callback back to a notification?
**Answer:**
Store provider message ID or correlation metadata when sending, then use it during callback processing.

---

## 33) Observability and Auditability

### Q1. What should be logged in a notification system?
**Answer:**
- request ID / notification ID
- user ID
- channel
- provider
- status transitions
- failure reason
- retry count

### Q2. What metrics are useful?
**Answer:**
- notifications created per channel
- success rate
- failure rate by provider
- retry count
- queue delay
- time to delivery
- suppression count
- callback lag

### Q3. Why is correlation ID important?
**Answer:**
Because one user action may create multiple notifications and provider calls. Correlation IDs help trace the entire workflow end to end.

### Q4. What is a good audit model?
**Answer:**
Keep final status on the notification record, but also keep attempt/history records for every important state change or provider interaction.

---

## 34) Security and Compliance Considerations

### Q1. What security concerns matter in a notification service?
**Answer:**
- protect PII such as email and phone number
- avoid logging sensitive message content blindly
- secure provider credentials
- validate webhook signatures
- prevent notification abuse and spam

### Q2. Why can notification content become a compliance issue?
**Answer:**
Notifications may contain OTPs, account data, personal information, or marketing content subject to opt-in/opt-out rules and retention policies.

### Q3. What should be masked or protected?
**Answer:**
- provider API keys
- phone numbers in logs where appropriate
- email addresses in public logs
- sensitive template variables like OTP or reset tokens

### Q4. Should promotional and security notifications follow the same policy?
**Answer:**
Usually no. Security and compliance-related notifications often have stricter delivery guarantees and different suppression rules than marketing notifications.

---

## 35) Testing Strategy for Notification Service LLD

### Q1. What unit tests would you mention in an interview?
**Answer:**
- preference suppression logic
- template rendering with placeholders
- sender selection by channel
- retryable vs non-retryable failure classification
- idempotency checks
- fallback logic

### Q2. What integration tests are useful?
**Answer:**
- provider adapter contract tests
- repository persistence tests
- callback processing tests
- scheduled notification execution tests

### Q3. Why are mocks/fakes useful here?
**Answer:**
Because external providers should not be called in ordinary unit tests. A provider interface makes mocking or fake provider simulation easy.

### Q4. How do you test retry behavior without waiting in real time?
**Answer:**
Inject a clock/scheduler abstraction and verify computed retry schedule or enqueued retry jobs rather than sleeping in tests.

---

## 36) Common Mistakes in Notification Service LLD

1. Putting all logic in one `NotificationService` class
2. Hardcoding provider-specific logic directly in service methods
3. Mixing template rendering with provider delivery code
4. Ignoring user preferences
5. No retry strategy or no distinction between retryable/non-retryable failures
6. No idempotency handling
7. One status only with no history
8. Tight coupling to one notification channel
9. No plan for scheduling or bulk flow
10. Using inheritance where composition/strategy is better

---

## 37) Suggested Interview Answer Structure

When asked to design Notification Service, a strong flow is:

1. Clarify channels, sync/async behavior, and preferences
2. Identify entities: request, notification, template, preference, provider, attempt
3. Define `NotificationSender` abstraction per channel
4. Use factory/strategy for sender selection
5. Separate template rendering and preference evaluation
6. Track status and attempts independently
7. Add retry, fallback, scheduling, and idempotency
8. Explain how a new channel/provider can be added

This answer shows both clean design and practical thinking.

---

## 38) Topic-Wise Interview Questions and Answers

### A. Basics

#### Q1. What is the core responsibility of a Notification Service?
**Answer:**
To create, manage, and deliver user notifications through appropriate channels while respecting templates, preferences, retries, and audit requirements.

#### Q2. What are the main components in the design?
**Answer:**
- NotificationService
- NotificationSender interface and channel implementations
- TemplateService and TemplateEngine
- PreferenceService
- Repository layer
- RetryPolicy
- Scheduler
- Provider integrations

#### Q3. Why separate request DTO from internal notification entity?
**Answer:**
Because the incoming request represents client input, while the internal entity represents tracked system state, lifecycle, and delivery information.

### B. Abstractions

#### Q4. Why use an interface for senders?
**Answer:**
Because Email, SMS, Push, and In-App all have different delivery logic but share a common "send notification" behavior.

#### Q5. Why should provider integrations sit behind abstractions?
**Answer:**
To avoid coupling business logic to external vendors and to enable easy replacement or fallback.

### C. Templates

#### Q6. Why is template rendering a separate concern?
**Answer:**
Because content generation and content delivery are different responsibilities. Separating them improves reuse and testing.

#### Q7. How do you support localization?
**Answer:**
Store templates by locale and let `TemplateService` resolve the best template for the user's preferred language.

### D. Preferences

#### Q8. How do you avoid sending unwanted messages?
**Answer:**
Use `PreferenceService` to enforce opt-outs, quiet hours, and per-channel permissions before sending.

### E. Reliability

#### Q9. How do you handle provider failure?
**Answer:**
Track attempt failures, retry retryable cases with backoff, and optionally fallback to alternate provider/channel for critical notifications.

#### Q10. Why store delivery attempts separately?
**Answer:**
Because one notification may have many attempts, and each attempt needs provider-level audit and troubleshooting detail.

### F. Scalability of Design

#### Q11. How does this design support new channels?
**Answer:**
By adding a new sender implementation and registering it with the factory or resolver without rewriting the core orchestration logic.

#### Q12. How do you support bulk notifications cleanly?
**Answer:**
Treat each recipient/channel delivery as its own internal notification record while sharing upstream request context where useful.

### G. Correctness

#### Q13. How do you prevent duplicate sends?
**Answer:**
Use idempotency keys at request level and dedupe policies for repeated equivalent events.

#### Q14. Why distinguish retryable and permanent failures?
**Answer:**
Because retrying permanent failures wastes cost and delays clear failure reporting.

### H. State Modeling

#### Q15. What states should a notification have?
**Answer:**
At minimum: CREATED, QUEUED, PROCESSING, SENT, DELIVERED, FAILED, RETRY_SCHEDULED, SUPPRESSED.

#### Q16. Why is state modeling important?
**Answer:**
Because notifications are workflows, not single function calls. Good states improve tracking, retries, and observability.

### I. Design Principles

#### Q17. Which SOLID principles are most relevant here?
**Answer:**
- SRP: keep template, sending, preference, retry, and repository responsibilities separate
- OCP: add channels/providers through extension
- DIP: services depend on sender/provider abstractions

#### Q18. Where is composition preferred over inheritance here?
**Answer:**
Sender classes should compose provider, validator, and policy collaborators instead of building deep inheritance hierarchies.

### J. Advanced Follow-Ups

#### Q19. How would you support notification priority?
**Answer:**
Add priority to the request/entity and use policy-based scheduling or queue ordering for HIGH/CRITICAL messages.

#### Q20. How would you support attachments in email only?
**Answer:**
Keep common notification abstraction minimal and let email-specific message models hold attachment metadata. Do not pollute SMS or push models with irrelevant fields.

#### Q21. How would you support read/unread for in-app notifications?
**Answer:**
Persist in-app notifications and expose methods such as `markAsRead()` and `getUnreadCount()`.

#### Q22. How would you model callback updates from providers?
**Answer:**
Create a callback handler/service that identifies the notification from provider metadata and updates status and attempt details asynchronously.

#### Q23. How would you handle quiet hours?
**Answer:**
Add policy logic in preference evaluation to suppress or defer eligible notifications during configured windows.

#### Q24. How would you design for testability?
**Answer:**
Use dependency injection, interfaces for senders/providers, isolated template rendering, and repository abstractions so each component can be tested independently.

#### Q25. How would you support analytics like open rate or click rate?
**Answer:**
Store provider callback events such as opened, clicked, bounced, and unsubscribed as separate event/history records. Keep analytics separate from core send orchestration.

#### Q26. How would you prevent one noisy tenant from spamming everyone?
**Answer:**
Add tenant-level quotas and throttling policies, and keep them separate from per-user or per-channel rate limits.

#### Q27. How would you support both immediate and scheduled notifications in one design?
**Answer:**
Use the same request model with an optional `scheduledAt` field. Immediate requests are dispatched now; scheduled ones are persisted and released later by a scheduler.

#### Q28. How would you support provider failover without changing business code?
**Answer:**
Keep provider resolution behind a provider selector or resolver abstraction so sender logic depends only on the provider contract.

---

## 39) Example of a Clean Verbal Design Answer

"I would design the Notification Service around a `NotificationService` orchestrator that accepts a `NotificationRequest`. It validates the input, checks idempotency, loads the user's preferences, selects allowed channels, renders channel-specific templates, and creates one internal `Notification` record per channel. Each record is delegated to a `NotificationSender` implementation chosen using a factory or strategy map. Senders use provider abstractions such as SMTP, Twilio, or Firebase. Delivery attempts and statuses are stored separately so retries and audits are clear. I would also keep retry policy, fallback policy, scheduling, and rate limiting as separate collaborators so the design remains extensible and testable." 

That is a very strong interview-ready summary.

---

## 40) Quick Cheat Sheet

- Model **request**, **notification**, **template**, **preference**, **provider**, **attempt** separately
- Use **Strategy** for per-channel sending
- Use **Factory/Resolver** for sender or provider selection
- Use **TemplateService + TemplateEngine** for content generation
- Use **PreferenceService** for opt-in/opt-out and quiet hours
- Track **status** and **delivery attempts** separately
- Add **idempotency** to prevent duplicates
- Add **retry policy** for transient failures
- Add **fallback** only for high-priority use cases
- Keep sender classes **stateless** where possible
- Add new channels by **extension**, not by rewriting orchestration logic

---

## 41) Final Interview One-Liner

"A good Notification Service LLD separates orchestration, content generation, user preference rules, provider delivery, retries, and tracking so new channels and providers can be added without changing core business flow."

---

## 42) Closing Guidance

In a Notification Service LLD interview, the strongest answers do not stop at "EmailSender, SmsSender, PushSender". Strong answers explain:
- how the workflow is orchestrated,
- how preferences and templates are enforced,
- how failures and retries are handled,
- how statuses are tracked,
- and how the design grows safely when new channels or providers are added.

That is what makes the design interview-ready and production-oriented.


