package com.system.design.low_level_design.case_studies.notification.service;

import com.system.design.low_level_design.case_studies.notification.entities.Template;
import com.system.design.low_level_design.case_studies.notification.interfaces.TemplateEngine;
import com.system.design.low_level_design.case_studies.notification.models.RenderedTemplate;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple template engine that replaces {{placeholder}} with variable values.
 */
public class SimpleTemplateEngine implements TemplateEngine {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    @Override
    public RenderedTemplate render(Template template, Map<String, Object> variables) {
        String renderedSubject = template.getSubject() != null ? renderContent(template.getSubject(), variables) : null;
        String renderedBody = renderContent(template.getBody(), variables);
        return new RenderedTemplate(renderedSubject, renderedBody);
    }

    @Override
    public boolean validateVariables(Template template, Map<String, Object> variables) {
        String content = (template.getSubject() != null ? template.getSubject() : "") + template.getBody();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            if (!variables.containsKey(placeholder)) {
                return false;
            }
        }
        return true;
    }

    private String renderContent(String content, Map<String, Object> variables) {
        if (content == null) {
            return null;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            Object value = variables.getOrDefault(placeholder, "");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(value)));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}

