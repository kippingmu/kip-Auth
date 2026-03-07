package xyz.kip.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.kip.auth.builder.ApiRequestArgumentResolver;

import java.util.List;

/**
 * @author xiaoshichuan
 * @version 2026-03-05 19:31
 */
@Configuration
public class ApiWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ApiRequestArgumentResolver());
    }
}
