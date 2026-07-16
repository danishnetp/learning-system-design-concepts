package com.system.design.low_level_design.case_studies.notification.main;

import com.system.design.low_level_design.case_studies.notification.entities.*;
import com.system.design.low_level_design.case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.case_studies.notification.enums.NotificationType;
import com.system.design.low_level_design.case_studies.notification.enums.Priority;
import com.system.design.low_level_design.case_studies.notification.repositories.DeliveryAttemptRepository;
import com.system.design.low_level_design.case_studies.notification.repositories.IdempotencyRepository;
import com.system.design.low_level_design.case_studies.notification.repositories.NotificationRepository;
import com.system.design.low_level_design.case_studies.notification.repositories.PreferenceRepository;
import com.system.design.low_level_design.case_studies.notification.repositories.TemplateRepository;
import com.system.design.low_level_design.case_studies.notification.repositories.impl.InMemoryDeliveryAttemptRepository;
import com.system.design.low_level_design.case_studies.notification.repositories.impl.InMemoryIdempotencyRepository;
import com.system.design.low_level_design.case_studies.notification.repositories.impl.InMemoryNotificationRepository;
import com.system.design.low_level_design.case_studies.notification.repositories.impl.InMemoryPreferenceRepository;
import com.system.design.low_level_design.case_studies.notification.repositories.impl.InMemoryTemplateRepository;
import com.system.design.low_level_design.case_studies.notification.sender.NotificationSenderFactory;
import com.system.design.low_level_design.case_studies.notification.service.ExponentialBackoffRetryPolicy;
import com.system.design.low_level_design.case_studies.notification.service.NotificationService;
import com.system.design.low_level_design.case_studies.notification.service.NotificationValidator;
import com.system.design.low_level_design.case_studies.notification.service.PreferenceService;
import com.system.design.low_level_design.case_studies.notification.service.SimpleTemplateEngine;
import com.system.design.low_level_design.case_studies.notification.service.TemplateService;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Example/Demo class showing how to use the Notification Service LLD.
 * This demonstrates the complete workflow for sending notifications with multiple channels,
 * templates, preferences, and retries.
 */
public class NotificationServiceExample {

    public static void main(String[] args) {
        System.out.println("=== Notification Service LLD Example ===\n");

        // 1. Initialize repositories (in production, these would be database implementations)
        NotificationRepository notificationRepository = new InMemoryNotificationRepository();
        TemplateRepository templateRepository = new InMemoryTemplateRepository();
        PreferenceRepository preferenceRepository = new InMemoryPreferenceRepository();
        DeliveryAttemptRepository deliveryAttemptRepository = new InMemoryDeliveryAttemptRepository();
        IdempotencyRepository idempotencyRepository = new InMemoryIdempotencyRepository();

        // 2. Initialize services
        TemplateService templateService = new TemplateService(templateRepository);
        PreferenceService preferenceService = new PreferenceService(preferenceRepository);
        SimpleTemplateEngine templateEngine = new SimpleTemplateEngine();
        NotificationValidator validator = new NotificationValidator();
        NotificationSenderFactory senderFactory = NotificationSenderFactory.createDefault();

        // 3. Create main notification service
        NotificationService notificationService = new NotificationService(
                notificationRepository,
                deliveryAttemptRepository,
                idempotencyRepository,
                templateService,
                preferenceService,
                templateEngine,
                validator,
                senderFactory,
                new ExponentialBackoffRetryPolicy(3, 1000)
        );

        // 4. Setup templates
        setupTemplates(templateService);

        // 5. Setup user preferences
        setupUserPreferences(preferenceService);

        // 6. Example 1: Send OTP via Email and SMS
        System.out.println("--- Example 1: Sending OTP Notification ---");
        sendOTPNotification(notificationService);

        // 7. Example 2: Send promotional email
        System.out.println("\n--- Example 2: Sending Promotional Email ---");
        sendPromotionalNotification(notificationService);

        // 8. Example 3: Send multi-channel notification
        System.out.println("\n--- Example 3: Sending Multi-Channel Notification ---");
        sendMultiChannelNotification(notificationService);

        // 9. Example 4: Check notification status
        System.out.println("\n--- Example 4: Checking Notification Status ---");
        checkNotificationStatus(notificationService);

        // 10. Example 5: Retry failed notification
        System.out.println("\n--- Example 5: Retrying Failed Notification ---");
        retryNotification(notificationService);

        System.out.println("\n=== Example Complete ===");
    }

    private static void setupTemplates(TemplateService templateService) {
        // OTP Email Template
        Template otpEmailTemplate = new Template(
                "otp-email-template",
                "OTP",
                NotificationChannel.EMAIL,
                "en_US",
                "Your OTP Code",
                "Hi {{userName}},\n\nYour OTP is: {{otp}}\n\nThis OTP is valid for 10 minutes.",
                1,
                true
        );
        templateService.saveTemplate(otpEmailTemplate);

        // OTP SMS Template
        Template otpSmsTemplate = new Template(
                "otp-sms-template",
                "OTP",
                NotificationChannel.SMS,
                "en_US",
                null,
                "Your OTP is {{otp}}. Valid for 10 minutes.",
                1,
                true
        );
        templateService.saveTemplate(otpSmsTemplate);

        // Welcome Email Template
        Template welcomeEmailTemplate = new Template(
                "welcome-email-template",
                "Welcome",
                NotificationChannel.EMAIL,
                "en_US",
                "Welcome to our platform!",
                "Hi {{userName}},\n\nWelcome to our awesome platform! We're excited to have you on board.\n\nBest regards,\nThe Team",
                1,
                true
        );
        templateService.saveTemplate(welcomeEmailTemplate);

        // Push Notification Template
        Template pushTemplate = new Template(
                "order-status-push",
                "OrderStatus",
                NotificationChannel.PUSH,
                "en_US",
                "Order {{orderId}} Status",
                "Your order {{orderId}} has been {{status}}",
                1,
                true
        );
        templateService.saveTemplate(pushTemplate);

        System.out.println("✓ Templates setup complete");
    }

    private static void setupUserPreferences(PreferenceService preferenceService) {
        // Allow OTP email
        UserPreference otpEmailPref = new UserPreference("user123", NotificationType.TRANSACTIONAL, NotificationChannel.EMAIL);
        otpEmailPref.setEnabled(true);
        preferenceService.savePreference(otpEmailPref);

        // Allow OTP SMS
        UserPreference otpSmsPref = new UserPreference("user123", NotificationType.TRANSACTIONAL, NotificationChannel.SMS);
        otpSmsPref.setEnabled(true);
        preferenceService.savePreference(otpSmsPref);

        // Promotional emails disabled
        UserPreference promotionalEmailPref = new UserPreference("user123", NotificationType.PROMOTIONAL, NotificationChannel.EMAIL);
        promotionalEmailPref.setEnabled(false);
        preferenceService.savePreference(promotionalEmailPref);

        // Push notifications with quiet hours
        UserPreference pushPref = new UserPreference("user123", NotificationType.PROMOTIONAL, NotificationChannel.PUSH);
        pushPref.setEnabled(true);
        pushPref.setQuietHoursStart(LocalTime.of(22, 0)); // 10 PM
        pushPref.setQuietHoursEnd(LocalTime.of(8, 0));   // 8 AM
        preferenceService.savePreference(pushPref);

        System.out.println("✓ User preferences setup complete");
    }

    private static void sendOTPNotification(NotificationService notificationService) {
        Set<NotificationChannel> channels = new HashSet<>();
        channels.add(NotificationChannel.EMAIL);
        channels.add(NotificationChannel.SMS);

        Map<String, Object> payload = new HashMap<>();
        payload.put("userName", "John Doe");
        payload.put("otp", "123456");

        NotificationRequest request = new NotificationRequest(
                "user123",
                NotificationType.TRANSACTIONAL,
                "otp-email-template", // Will handle channel resolution
                channels,
                payload,
                Priority.HIGH,
                null,
                null
        );

        List<String> notificationIds = notificationService.send(request);
        System.out.println("✓ OTP notifications sent: " + notificationIds.size() + " notification(s)");
        for (String id : notificationIds) {
            System.out.println("  - Notification ID: " + id);
        }
    }

    private static void sendPromotionalNotification(NotificationService notificationService) {
        Set<NotificationChannel> channels = new HashSet<>();
        channels.add(NotificationChannel.EMAIL);

        Map<String, Object> payload = new HashMap<>();
        payload.put("userName", "John Doe");

        NotificationRequest request = new NotificationRequest(
                "user123",
                NotificationType.PROMOTIONAL,
                "welcome-email-template",
                channels,
                payload,
                Priority.MEDIUM,
                null,
                null
        );

        List<String> notificationIds = notificationService.send(request);
        if (notificationIds.isEmpty()) {
            System.out.println("✗ Promotional email not sent (user has opted out)");
        } else {
            System.out.println("✓ Promotional email sent");
        }
    }

    private static void sendMultiChannelNotification(NotificationService notificationService) {
        Set<NotificationChannel> channels = new HashSet<>();
        channels.add(NotificationChannel.PUSH);
        channels.add(NotificationChannel.IN_APP);

        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", "ORD-12345");
        payload.put("status", "Shipped");

        NotificationRequest request = new NotificationRequest(
                "user123",
                NotificationType.PROMOTIONAL,
                "order-status-push",
                channels,
                payload,
                Priority.MEDIUM,
                null,
                "idempotency-key-" + System.currentTimeMillis()
        );

        List<String> notificationIds = notificationService.send(request);
        System.out.println("✓ Multi-channel notifications sent: " + notificationIds.size() + " notification(s)");
    }

    private static void checkNotificationStatus(NotificationService notificationService) {
        // Send a notification first
        Set<NotificationChannel> channels = new HashSet<>();
        channels.add(NotificationChannel.EMAIL);

        Map<String, Object> payload = new HashMap<>();
        payload.put("userName", "Jane Doe");

        NotificationRequest request = new NotificationRequest(
                "user456",
                NotificationType.TRANSACTIONAL,
                "welcome-email-template",
                channels,
                payload
        );

        List<String> notificationIds = notificationService.send(request);

        if (!notificationIds.isEmpty()) {
            String notificationId = notificationIds.get(0);

            // Check status
            Optional<Notification> notification = notificationService.getNotificationStatus(notificationId);
            if (notification.isPresent()) {
                Notification n = notification.get();
                System.out.println("Notification Status:");
                System.out.println("  - ID: " + n.getNotificationId());
                System.out.println("  - Channel: " + n.getChannel());
                System.out.println("  - Status: " + n.getStatus());
                System.out.println("  - Attempts: " + n.getAttemptCount());

                // Check delivery attempts
                List<DeliveryAttempt> attempts =
                        notificationService.getDeliveryAttempts(notificationId);
                System.out.println("  - Delivery Attempts: " + attempts.size());
                for (DeliveryAttempt attempt : attempts) {
                    System.out.println("    * Attempt " + attempt.getAttemptNumber() + ": " + attempt.getStatus());
                }
            }
        }
    }

    private static void retryNotification(NotificationService notificationService) {
        System.out.println("Retry functionality is available through:");
        System.out.println("  - notificationService.retry(notificationId)");
        System.out.println("  - Returns true if retry was scheduled, false otherwise");
        System.out.println("✓ Retry mechanism configured with exponential backoff (1s, 2s, 4s)");
    }
}

