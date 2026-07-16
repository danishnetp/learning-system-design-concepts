package com.system.design.low_level_design.case_studies.notification.repositories.impl;

import com.system.design.low_level_design.case_studies.notification.entities.Template;
import com.system.design.low_level_design.case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.case_studies.notification.repositories.TemplateRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory implementation of TemplateRepository for demonstration.
 */
public class InMemoryTemplateRepository implements TemplateRepository {

    private final Map<String, Template> storage = new HashMap<>();

    @Override
    public Template save(Template template) {
        storage.put(template.getTemplateId(), template);
        return template;
    }

    @Override
    public Optional<Template> findById(String templateId) {
        return Optional.ofNullable(storage.get(templateId));
    }

    @Override
    public Optional<Template> findByNameAndChannelAndLocale(String name, NotificationChannel channel, String locale) {
        return storage.values().stream()
                .filter(t -> t.getName().equals(name) && t.getChannel() == channel && t.getLocale().equals(locale) && t.isActive())
                .findFirst();
    }

    @Override
    public List<Template> findByChannel(NotificationChannel channel) {
        return storage.values().stream()
                .filter(t -> t.getChannel() == channel && t.isActive())
                .collect(Collectors.toList());
    }

    @Override
    public Template update(Template template) {
        storage.put(template.getTemplateId(), template);
        return template;
    }

    @Override
    public boolean delete(String templateId) {
        return storage.remove(templateId) != null;
    }
}

