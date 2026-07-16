package com.system.design.low_level_design.case_studies.notification.models;

/**
 * Represents a rendered template with subject and body populated.
 */
public class RenderedTemplate {
    private final String subject;
    private final String body;

    public RenderedTemplate(String subject, String body) {
        this.subject = subject;
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "RenderedTemplate{" +
                "subject='" + (subject != null ? subject.substring(0, Math.min(30, subject.length())) : "null") + "..." + '\'' +
                ", body='" + (body != null ? body.substring(0, Math.min(30, body.length())) : "null") + "..." + '\'' +
                '}';
    }
}

