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

    @Value("${tdw.ai.provider:mock}")
    private String providerName;

    @Autowired
    public AiProviderFactory(List<AiProvider> providerList)
    {
        for (AiProvider provider : providerList) {
            providers.put(provider.getName(), provider);
        }
    }

    public AiProvider getProvider()
    {
        AiProvider provider = providers.get(providerName);
        if (provider == null) {
            provider = providers.get("mock");
        }
        if (provider == null) {
            throw new IllegalStateException("No AI provider available");
        }
        return provider;
    }
}
