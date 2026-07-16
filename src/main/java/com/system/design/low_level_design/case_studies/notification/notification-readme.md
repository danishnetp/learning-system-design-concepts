# Notification Service - Low Level Design Implementation

This is a comprehensive, production-ready implementation of a Notification Service Low Level Design (LLD) as described in the accompanying markdown documentation.

## Overview

The Notification Service is a flexible, extensible system for sending notifications through multiple channels (Email, SMS, Push, In-App) while respecting user preferences, handling retries, and maintaining delivery history.

## Architecture

### Core Components

#### 1. **Entities** (`entities/` package)
- `User` - Represents a notification recipient
- `NotificationRequest` - Incoming request from callers
- `Notification` - Internal notification entity with lifecycle tracking
- `Template` - Reusable message templates with placeholders
- `UserPreference` - User's notification preferences per channel/type
- `DeliveryAttempt` - Tracks individual delivery attempts

#### 2. **Enumerations** (`enums/` package)
- `NotificationChannel` - EMAIL, SMS, PUSH, IN_APP, WEBHOOK
- `NotificationType` - TRANSACTIONAL, PROMOTIONAL, SECURITY, SYSTEM_ALERT
- `NotificationStatus` - CREATED, QUEUED, PROCESSING, SENT, DELIVERED, FAILED, RETRY_SCHEDULED, CANCELLED, SUPPRESSED
- `Priority` - LOW, MEDIUM, HIGH, CRITICAL

#### 3. **Interfaces** (`interfaces/` package)
- `NotificationSender` - Abstraction for sending notifications through a channel
- `ChannelProvider` - Abstraction for external notification providers (SMTP, Twilio, Firebase, etc.)
- `TemplateEngine` - Renders templates by replacing placeholders with variables
- `RetryPolicy` - Defines retry strategy and backoff calculations

#### 4. **Providers** (`provider/` package)
Mock implementations of external services:
- `SmtpEmailProvider` - Email delivery via SMTP
- `TwilioSmsProvider` - SMS delivery via Twilio
- `FirebasePushProvider` - Push notifications via Firebase

#### 5. **Senders** (`sender/` package)
Channel-specific implementations of `NotificationSender`:
- `EmailNotificationSender` - Sends via Email channel
- `SmsNotificationSender` - Sends via SMS channel
- `PushNotificationSender` - Sends via Push channel
- `InAppNotificationSender` - Stores In-App notifications
- `NotificationSenderFactory` - Factory for creating/managing senders

#### 6. **Services** (`service/` package)
- `NotificationService` - Main orchestrator coordinating the entire workflow
- `TemplateService` - Template management and lookup with locale fallback
- `PreferenceService` - User preference checking and management
- `NotificationValidator` - Request validation and data format checking
- `SimpleTemplateEngine` - Template rendering with {{placeholder}} syntax
- `ExponentialBackoffRetryPolicy` - Retry logic with exponential backoff and jitter

#### 7. **Repositories** (`repositories/` package)
Data access layer with in-memory implementations:
- `NotificationRepository` - Persist notifications
- `TemplateRepository` - Persist templates
- `PreferenceRepository` - Persist user preferences
- `DeliveryAttemptRepository` - Track delivery attempts
- `IdempotencyRepository` - Prevent duplicate sends

#### 8. **Models** (`models/` package)
Data transfer objects:
- `RenderedTemplate` - Template with resolved placeholders
- `SendResult` - Result of a send operation

## Key Features

### 1. Multi-Channel Support
- Email (SMTP provider)
- SMS (Twilio provider)
- Push Notifications (Firebase)
- In-App Notifications
- Extensible for new channels

### 2. Template System
- {{placeholder}} based rendering
- Per-channel templates
- Locale support with fallback to en_US
- Variable validation before sending

### 3. User Preferences
- Per-channel opt-in/opt-out
- Per-notification-type preferences
- Quiet hours support
- Automatic suppression based on preferences

### 4. Reliability
- Exponential backoff retry strategy
- Retryable vs non-retryable failure classification
- Delivery attempt history tracking
- Status transitions with audit trail

### 5. Idempotency
- Idempotency key support for duplicate prevention
- 24-hour TTL for idempotency records
- Cached results for retry safety

### 6. Extensibility
- Strategy pattern for different senders
- Factory pattern for sender/provider instantiation
- Interface-based design for easy mocking and extension
- New channels can be added without modifying orchestration logic

## Workflow

```
1. NotificationRequest arrives
   ↓
2. Validate request (user, template, channels)
   ↓
3. Check idempotency (prevent duplicates)
   ↓
4. Check user preferences (determine allowed channels)
   ↓
5. Load and render template with variables
   ↓
6. Create Notification entity per allowed channel
   ↓
7. For each notification:
   - Get appropriate NotificationSender
   - Send via ChannelProvider
   - Track DeliveryAttempt
   - Update notification status
   ↓
8. Handle failures (retry, fallback, mark as failed)
   ↓
9. Return list of notification IDs
```

## UML Diagrams

- Class diagram: `diagram/NotificationClassDiagram.puml`
- Send flow sequence diagram: `diagram/NotificationSendSequence.puml`

These diagrams help visualize both static design (classes, interfaces, dependencies)
and runtime behavior (request validation, idempotency, template rendering, provider delivery,
retry scheduling, and status updates).

## Usage Example

```java
// Initialize
NotificationService notificationService = setupNotificationService();

// Create request
NotificationRequest request = new NotificationRequest(
    "user123",
    NotificationType.TRANSACTIONAL,
    "otp-template-id",
    Set.of(NotificationChannel.EMAIL, NotificationChannel.SMS),
    Map.of("userName", "John", "otp", "123456"),
    Priority.HIGH,
    null, // no scheduling
    "idempotency-key-123"
);

// Send
List<String> notificationIds = notificationService.send(request);

// Check status
Optional<Notification> notification = notificationService.getNotificationStatus(notificationIds.get(0));

// Retry if needed
notificationService.retry(notificationIds.get(0));

// Cancel if needed
notificationService.cancel(notificationIds.get(0));
```

## Design Patterns Used

1. **Strategy Pattern** - Different notification channels as strategies
2. **Factory Pattern** - NotificationSenderFactory for sender creation
3. **Builder Pattern** - Entities use constructors with fluent-like APIs
4. **Repository Pattern** - Data access abstraction
5. **Template Method Pattern** - Notification workflow orchestration
6. **Decorator Pattern** - Can wrap senders with additional behaviors (logging, metrics, etc.)

## Extensibility Examples

### Adding a New Channel (WhatsApp)

```java
// 1. Implement ChannelProvider
public class WhatsappProvider implements ChannelProvider {
    // Implementation
}

// 2. Implement NotificationSender
public class WhatsappNotificationSender implements NotificationSender {
    // Implementation
}

// 3. Register in factory
senderFactory.registerSender(
    NotificationChannel.WHATSAPP,
    new WhatsappNotificationSender(new WhatsappProvider())
);

// 4. Create template with WHATSAPP channel
Template template = new Template(..., NotificationChannel.WHATSAPP, ...);
```

### Adding a New Provider (AWS SES for Email)

```java
public class AwsSesEmailProvider implements ChannelProvider {
    // Replaces SmtpEmailProvider
    // Returns same channel, different implementation
}

// Update factory to use new provider
senderFactory.registerSender(
    NotificationChannel.EMAIL,
    new EmailNotificationSender(new AwsSesEmailProvider())
);
```

## Testing Considerations

### Unit Tests
- Template rendering with placeholders
- Preference suppression logic
- Retry policy calculations
- Validator rules

### Integration Tests
- Provider adapter contracts
- Repository operations
- Complete notification workflow
- Idempotency verification

### Example
See `NotificationServiceExample.java` for demonstration of all major features.

## Running the Example

```bash
cd D:\repositories\learning-system-design-concepts
gradle build
gradle run --main-class com.system.design.low_level_design.notification.example.NotificationServiceExample
```

## Package Structure

```
notification/
├── entities/              # Domain entities
├── enums/                 # Enumerations
├── exceptions/            # Custom exceptions
├── example/               # Example/demo code
├── interfaces/            # Abstractions
├── models/                # DTOs
├── provider/              # External provider implementations
├── repositories/          # Data access
│   └── impl/              # In-memory implementations
├── sender/                # Notification sender implementations
└── service/               # Business logic services
```

## Production Considerations

### Database Implementation
Replace in-memory repositories with actual database implementations:
- Use JPA/Hibernate for persistence
- Add proper indexing for queries
- Consider partition by user_id or notification_id

### Caching
- Cache templates in Redis
- Cache user preferences with TTL
- Cache provider availability status

### Async Processing
- Use message queues (Kafka, RabbitMQ) for async sending
- Implement worker processes for retry handling
- Add scheduled jobs for deferred notifications

### Monitoring & Observability
- Log all state transitions
- Emit metrics for success/failure rates
- Add correlation IDs for tracing
- Set up alerts for provider failures

### Rate Limiting
- Implement token bucket algorithm
- Per-user rate limits
- Per-channel provider quotas
- Global system limits

### Fallback & Resilience
- Fallback from one provider to another
- Circuit breaker for provider failures
- Graceful degradation

## Key Interview Points

1. **Separation of Concerns** - Each class has single responsibility
2. **Abstraction** - Depends on interfaces, not implementations
3. **Extensibility** - New channels/providers without core changes
4. **Reliability** - Retry logic, attempt tracking, status management
5. **Scalability** - Async processing, idempotency, repository pattern
6. **Testing** - Easy to mock, interfaces-based design

## Compliance & Security

- PII protection in logs (mask emails, phones)
- Secure provider credential storage
- Webhook signature verification
- User opt-in/opt-out enforcement
- Audit trail for all notifications
- GDPR compliance (user data deletion)

---

**Author:** LLD Implementation  
**Version:** 1.0  
**Last Updated:** 2026-07-16

