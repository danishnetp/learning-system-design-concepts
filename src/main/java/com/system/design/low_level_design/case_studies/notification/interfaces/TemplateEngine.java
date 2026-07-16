package com.system.design.low_level_design.case_studies.notification.interfaces;

import com.system.design.low_level_design.case_studies.notification.entities.Template;
import com.system.design.low_level_design.case_studies.notification.models.RenderedTemplate;
import java.util.Map;

/**
 * Interface for rendering templates with variables.
 */
public interface TemplateEngine {

    /**
     * Renders a template by replacing placeholders with variables.
     *
     * @param template the template to render
     * @param variables map of placeholder variables
     * @return rendered template with subject and body
     */
    RenderedTemplate render(Template template, Map<String, Object> variables);

    /**
     * Validates if all required placeholders are present in variables.
     *
     * @param template the template to validate
     * @param variables map of provided variables
     * @return true if all required placeholders are provided, false otherwise
     */
    boolean validateVariables(Template template, Map<String, Object> variables);
}

