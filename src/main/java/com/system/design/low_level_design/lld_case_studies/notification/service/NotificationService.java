package com.system.design.low_level_design.lld_case_studies.notification.service;

import com.system.design.low_level_design.lld_case_studies.notification.entities.DeliveryAttempt;
import com.system.design.low_level_design.lld_case_studies.notification.entities.Notification;
import com.system.design.low_level_design.lld_case_studies.notification.entities.NotificationRequest;
import com.system.design.low_level_design.lld_case_studies.notification.entities.Template;
import com.system.design.low_level_design.lld_case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.lld_case_studies.notification.enums.NotificationStatus;
import com.system.design.low_level_design.lld_case_studies.notification.exceptions.InvalidNotificationRequestException;
import com.system.design.low_level_design.lld_case_studies.notification.interfaces.NotificationSender;
import com.system.design.low_level_design.lld_case_studies.notification.interfaces.RetryPolicy;
import com.system.design.low_level_design.lld_case_studies.notification.interfaces.TemplateEngine;
import com.system.design.low_level_design.lld_case_studies.notification.models.RenderedTemplate;
import com.system.design.low_level_design.lld_case_studies.notification.models.SendResult;
import com.system.design.low_level_design.lld_case_studies.notification.repositories.DeliveryAttemptRepository;
import com.system.design.low_level_design.lld_case_studies.notification.repositories.IdempotencyRepository;
import com.system.design.low_level_design.lld_case_studies.notification.repositories.NotificationRepository;
import com.system.design.low_level_design.lld_case_studies.notification.sender.NotificationSenderFactory;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Main notification service orchestrator.
 * Coordinates the notification workflow: validation, preference checking, rendering, sending, and tracking.
 */
public class NotificationService {

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());

    private final NotificationRepository notificationRepository;
    private final DeliveryAttemptRepository deliveryAttemptRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final TemplateService templateService;
    private final PreferenceService preferenceService;
    private final TemplateEngine templateEngine;
    private final NotificationValidator validator;
    private final NotificationSenderFactory senderFactory;
    private final RetryPolicy retryPolicy;

    public NotificationService(
            NotificationRepository notificationRepository,
            DeliveryAttemptRepository deliveryAttemptRepository,
            IdempotencyRepository idempotencyRepository,
            TemplateService templateService,
            PreferenceService preferenceService,
            TemplateEngine templateEngine,
            NotificationValidator validator,
            NotificationSenderFactory senderFactory,
            RetryPolicy retryPolicy) {
        this.notificationRepository = notificationRepository;
        this.deliveryAttemptRepository = deliveryAttemptRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.templateService = templateService;
        this.preferenceService = preferenceService;
        this.templateEngine = templateEngine;
        this.validator = validator;
        this.senderFactory = senderFactory;
        this.retryPolicy = retryPolicy;
    }

    /**
     * Main method to send a notification.
     *
     * @param request the notification request
     * @return list of notification IDs created
     */
    public List<String> send(NotificationRequest request) {
        // Validate request
        validator.validate(request);

        // Check idempotency
        if (!idempotencyRepository.tryCreate(request.getIdempotencyKey())) {
            Optional<String> existingResult = idempotencyRepository.getExistingResult(request.getIdempotencyKey());
            if (existingResult.isPresent()) {
                LOGGER.info("Duplicate request detected, returning cached result");
                return List.of(existingResult.get());
            }
        }

        try {
            // Check preferences and determine which channels to use
            Set<NotificationChannel> allowedChannels = getEnabledChannels(request);

            if (allowedChannels.isEmpty()) {
                LOGGER.warning("No channels allowed for notification due to user preferences");
                return List.of();
            }

            // Load and render template
            Template template = templateService.getTemplate(request.getTemplateId());

            // Validate template variables
            if (!templateEngine.validateVariables(template, request.getPayload())) {
                throw new InvalidNotificationRequestException("Missing required template variables");
            }

            RenderedTemplate rendered = templateEngine.render(template, request.getPayload());

            // Create and send notifications for each channel
            List<String> notificationIds = createAndSendNotifications(request, allowedChannels, rendered);

            // Store result for idempotency
            if (!notificationIds.isEmpty()) {
                idempotencyRepository.storeResult(request.getIdempotencyKey(), notificationIds.get(0));
            }

            return notificationIds;
        } catch (Exception e) {
            LOGGER.severe("Error processing notification: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Gets channels that are enabled for the user based on preferences.
     *
     * @param request the notification request
     * @return set of enabled channels
     */
    private Set<NotificationChannel> getEnabledChannels(NotificationRequest request) {
        Set<NotificationChannel> enabledChannels = new HashSet<>();

        for (NotificationChannel channel : request.getChannels()) {
            if (preferenceService.isNotificationAllowed(request.getUserId(), request.getType(), channel)) {
                enabledChannels.add(channel);
            }
        }

        return enabledChannels;
    }

    /**
     * Creates and sends notifications for each channel.
     *
     * @param request the notification request
     * @param channels the channels to send to
     * @param rendered the rendered template
     * @return list of notification IDs
     */
    private List<String> createAndSendNotifications(NotificationRequest request, Set<NotificationChannel> channels, RenderedTemplate rendered) {
        List<String> notificationIds = new java.util.ArrayList<>();

        for (NotificationChannel channel : channels) {
            try {
                // Create notification entity
                Notification notification = new Notification(
                        request.getRequestId(),
                        request.getUserId(),
                        channel,
                        request.getType(),
                        request.getTemplateId(),
                        rendered.getSubject(),
                        rendered.getBody()
                );

                notification.setPriority(request.getPriority());

                // Check if scheduled or should be sent immediately
                if (request.isScheduled()) {
                    notification.setScheduledAt(request.getScheduledAt());
                    notification.setStatus(NotificationStatus.QUEUED);
                    LOGGER.info("Scheduling notification: " + notification.getNotificationId());
                } else {
                    // Send immediately
                    sendNotificationNow(notification);
                }

                // Save notification
                notificationRepository.save(notification);
                notificationIds.add(notification.getNotificationId());
            } catch (Exception e) {
                LOGGER.severe("Error creating notification for channel " + channel + ": " + e.getMessage());
            }
        }

        return notificationIds;
    }

    /**
     * Sends a notification immediately.
     *
     * @param notification the notification to send
     */
    private void sendNotificationNow(Notification notification) {
        try {
            notification.setStatus(NotificationStatus.PROCESSING);

            NotificationSender sender = senderFactory.getSender(notification.getChannel());
            if (!sender.canSend(notification)) {
                notification.setStatus(NotificationStatus.FAILED);
                notification.setErrorCode("INVALID_NOTIFICATION");
                notification.setErrorMessage("Notification failed validation");
                return;
            }

            // Record attempt
            DeliveryAttempt attempt = new DeliveryAttempt(notification.getNotificationId(), "UNKNOWN", 1);
            notification.incrementAttemptCount();

            // Send
            SendResult result = sender.send(notification);

            // Update attempt
            if (result.isSuccess()) {
                attempt.setStatus(result.getStatus());
                notification.setStatus(result.getStatus());
                notification.setProviderMessageId(result.getProviderMessageId());
                notification.setSentAt(Instant.now());
            } else {
                attempt.setStatus(NotificationStatus.FAILED);
                attempt.setErrorCode(result.getErrorCode());
                attempt.setErrorMessage(result.getErrorMessage());

                if (result.isRetryable() && retryPolicy.canRetry(notification, notification.getAttemptCount())) {
                    notification.setStatus(NotificationStatus.RETRY_SCHEDULED);
                } else {
                    notification.setStatus(NotificationStatus.FAILED);
                    notification.setErrorCode(result.getErrorCode());
                    notification.setErrorMessage(result.getErrorMessage());
                }
            }

            deliveryAttemptRepository.save(attempt);
        } catch (Exception e) {
            LOGGER.severe("Error sending notification: " + e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorCode("SEND_EXCEPTION");
            notification.setErrorMessage(e.getMessage());
        }
    }

    /**
     * Gets notification status.
     *
     * @param notificationId the notification ID
     * @return the notification if found
     */
    public Optional<Notification> getNotificationStatus(String notificationId) {
        return notificationRepository.findById(notificationId);
    }

    /**
     * Gets delivery attempts for a notification.
     *
     * @param notificationId the notification ID
     * @return list of delivery attempts
     */
    public List<DeliveryAttempt> getDeliveryAttempts(String notificationId) {
        return deliveryAttemptRepository.findByNotificationId(notificationId);
    }

    /**
     * Retries a failed notification.
     *
     * @param notificationId the notification ID
     * @return true if retry was scheduled, false otherwise
     */
    public boolean retry(String notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            LOGGER.warning("Notification not found: " + notificationId);
            return false;
        }

        Notification notification = notificationOpt.get();

        // Check if can retry
        if (!notification.getStatus().isRetryable() || !retryPolicy.canRetry(notification, notification.getAttemptCount())) {
            LOGGER.warning("Notification cannot be retried: " + notificationId);
            return false;
        }

        // Send again
        sendNotificationNow(notification);
        notificationRepository.update(notification);

        LOGGER.info("Notification retried: " + notificationId);
        return true;
    }

    /**
     * Cancels a notification.
     *
     * @param notificationId the notification ID
     * @return true if cancelled, false otherwise
     */
    public boolean cancel(String notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            return false;
        }

        Notification notification = notificationOpt.get();
        if (!notification.getStatus().isTerminal()) {
            notification.setStatus(NotificationStatus.CANCELLED);
            notificationRepository.update(notification);
            LOGGER.info("Notification cancelled: " + notificationId);
            return true;
        }

        return false;
    }
}

