package com.system.design.low_level_design.case_studies.notification.entities;

import com.system.design.low_level_design.case_studies.notification.enums.NotificationChannel;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a notification template with placeholders.
 */
public class Template {
    private final String templateId;
    private final String name;
    private final NotificationChannel channel;
    private final String locale;
    private final String subject; // For email, can be null for SMS/Push
    private final String body;
    private final int version;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Template(String templateId, String name, NotificationChannel channel, String locale,
                    String subject, String body, int version, boolean active) {
        this.templateId = templateId;
        this.name = name;
        this.channel = channel;
        this.locale = locale;
        this.subject = subject;
        this.body = body;
        this.version = version;
        this.active = active;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters
    public String getTemplateId() {
        return templateId;
    }

    public String getName() {
        return name;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public String getLocale() {
        return locale;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public int getVersion() {
        return version;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Template template = (Template) o;
        return Objects.equals(templateId, template.templateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(templateId);
    }

    @Override
    public String toString() {
        return "Template{" +
                "templateId='" + templateId + '\'' +
                ", name='" + name + '\'' +
                ", channel=" + channel +
                ", version=" + version +
                '}';
    }
}

