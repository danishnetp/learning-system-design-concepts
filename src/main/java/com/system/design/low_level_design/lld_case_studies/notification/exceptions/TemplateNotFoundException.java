package com.system.design.low_level_design.lld_case_studies.notification.exceptions;

/**
 * Thrown when a template is not found.
 */
public class TemplateNotFoundException extends NotificationException {
    public TemplateNotFoundException(String message) {
        super(message);
    }

    public TemplateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

