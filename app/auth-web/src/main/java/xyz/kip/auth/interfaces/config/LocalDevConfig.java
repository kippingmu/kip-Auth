package xyz.kip.auth.interfaces.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import xyz.kip.auth.manager.BizInfoManager;
import xyz.kip.auth.manager.domain.BizInfoDomain;
import xyz.kip.open.common.base.Result;

/**
 * Local/dev only stub beans to allow the app to start
 * without external dependencies.
 * @author xiaoshichuan
 */
@Configuration
@Profile("local")
public class LocalDevConfig {

    /**
     * Provide a minimal stub for BizInfoManager in local profile
     * to satisfy service wiring during local runs.
     */
    @Bean
    public BizInfoManager bizInfoManagerStub() {
        return new BizInfoManager() {
            @Override
            public Result<BizInfoDomain> queryByBizCode(String bizCode) {
                return Result.failure("Local stub: BizInfoManager not implemented");
            }
        };
    }
}

