package com.ruoyi.tdw.ai.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Chooses the configured AI provider.
 */
@Component
public class AiProviderFactory
{
    private final Map<String, AiProvider> providers = new HashMap<String, AiProvider>();

    @Value("${tdw.ai.provider:glm}")
    private String providerName;

    @Value("${tdw.ai.allow-mock:false}")
    private boolean allowMock;

    @Autowired
    public AiProviderFactory(List<AiProvider> providerList)
    {
        for (AiProvider provider : providerList) {
            providers.put(provider.getName(), provider);
        }
    }

    public AiProvider getProvider()
    {
        if ("mock".equalsIgnoreCase(providerName) && !allowMock) {
            throw new IllegalStateException("Mock AI provider is disabled. Configure tdw.ai.provider=glm and TDW_GLM_API_KEY for real model calls.");
        }
        AiProvider provider = providers.get(providerName);
        if (provider == null) {
            throw new IllegalStateException("AI provider unavailable: " + providerName);
        }
        return provider;
    }
}
