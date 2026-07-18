package com.system.design.low_level_design.lld_case_studies.notification.service;

import com.system.design.low_level_design.lld_case_studies.notification.entities.Template;
import com.system.design.low_level_design.lld_case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.lld_case_studies.notification.exceptions.TemplateNotFoundException;
import com.system.design.low_level_design.lld_case_studies.notification.repositories.TemplateRepository;
import java.util.Optional;

/**
 * Service for managing notification templates.
 */
public class TemplateService {

    private final TemplateRepository templateRepository;

    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    /**
     * Gets a template by ID.
     *
     * @param templateId the template ID
     * @return the template
     * @throws TemplateNotFoundException if template not found
     */
    public Template getTemplate(String templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found: " + templateId));
    }

    /**
     * Gets a template by name, channel, and locale.
     * Falls back to default locale if specific locale not found.
     *
     * @param name the template name
     * @param channel the notification channel
     * @param locale the preferred locale
     * @return the template
     * @throws TemplateNotFoundException if template not found
     */
    public Template getTemplate(String name, NotificationChannel channel, String locale) {
        // Try to find with specific locale first
        Optional<Template> template = templateRepository.findByNameAndChannelAndLocale(name, channel, locale);
        if (template.isPresent()) {
            return template.get();
        }

        // Fall back to default locale (en_US)
        template = templateRepository.findByNameAndChannelAndLocale(name, channel, "en_US");
        if (template.isPresent()) {
            return template.get();
        }

        throw new TemplateNotFoundException("Template not found: " + name + " for channel: " + channel);
    }

    /**
     * Saves a new template.
     *
     * @param template the template to save
     * @return the saved template
     */
    public Template saveTemplate(Template template) {
        return templateRepository.save(template);
    }

    /**
     * Updates an existing template.
     *
     * @param template the template to update
     * @return the updated template
     */
    public Template updateTemplate(Template template) {
        return templateRepository.update(template);
    }

    /**
     * Deletes a template.
     *
     * @param templateId the template ID
     * @return true if deleted, false if not found
     */
    public boolean deleteTemplate(String templateId) {
        return templateRepository.delete(templateId);
    }
}

