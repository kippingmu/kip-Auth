package xyz.kip.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import xyz.kip.auth.manager.BizInfoManager;
import xyz.kip.open.common.base.Result;

/**
 * Provides safe fallback beans when concrete implementations are absent.
 * Active for non-prod profiles, and only created if no other bean exists.
 * @author xiaoshichuan
 */
@Configuration
@Profile("!prod")
public class FallbackManagerConfig {

    /**
     * Fallback stub for BizInfoManager used when no real bean is defined.
     */
    @Bean
    @ConditionalOnMissingBean(BizInfoManager.class)
    public BizInfoManager bizInfoManagerFallback() {
        return bizCode -> Result.failure("Fallback BizInfoManager: no implementation available");
    }
}

