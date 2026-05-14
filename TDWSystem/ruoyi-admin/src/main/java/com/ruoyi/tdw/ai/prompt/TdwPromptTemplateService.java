package com.ruoyi.tdw.ai.prompt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

/**
 * Loads prompt templates from classpath resources so prompt wording can change
 * without touching Java business code.
 */
@Service
public class TdwPromptTemplateService
{
    public String render(String templatePath, Map<String, String> variables)
    {
        String template = load(templatePath);
        if (variables == null || variables.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue() == null ? "" : entry.getValue());
        }
        return result;
    }

    private String load(String templatePath)
    {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Prompt template not found: " + templatePath, e);
        }
    }
}
