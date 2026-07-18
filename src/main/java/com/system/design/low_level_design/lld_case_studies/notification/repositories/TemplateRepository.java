package com.system.design.low_level_design.lld_case_studies.notification.repositories;

import com.system.design.low_level_design.lld_case_studies.notification.entities.Template;
import com.system.design.low_level_design.lld_case_studies.notification.enums.NotificationChannel;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Template persistence.
 */
public interface TemplateRepository {

    /**
     * Saves a template.
     *
     * @param template the template to save
     * @return saved template
     */
    Template save(Template template);

    /**
     * Finds a template by ID.
     *
     * @param templateId the template ID
     * @return optional containing the template if found
     */
    Optional<Template> findById(String templateId);

    /**
     * Finds templates by name, channel, and locale.
     *
     * @param name template name
     * @param channel notification channel
     * @param locale locale code
     * @return optional containing the template if found
     */
    Optional<Template> findByNameAndChannelAndLocale(String name, NotificationChannel channel, String locale);

    /**
     * Finds all templates for a channel.
     *
     * @param channel the notification channel
     * @return list of templates
     */
    List<Template> findByChannel(NotificationChannel channel);

    /**
     * Updates a template.
     *
     * @param template the template to update
     * @return updated template
     */
    Template update(Template template);

    /**
     * Deletes a template.
     *
     * @param templateId the template ID
     * @return true if deleted, false if not found
     */
    boolean delete(String templateId);
}

