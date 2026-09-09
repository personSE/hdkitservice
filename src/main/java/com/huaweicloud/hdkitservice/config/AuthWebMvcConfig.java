package com.huaweicloud.hdkitservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuthWebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final ApiKeyInterceptor apiKeyInterceptor;

    public AuthWebMvcConfig(AuthInterceptor authInterceptor, ApiKeyInterceptor apiKeyInterceptor) {
        this.authInterceptor = authInterceptor;
        this.apiKeyInterceptor = apiKeyInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/rest/developer/server/hdkitservice/dashboard/**")
                .excludePathPatterns(
                        "/rest/developer/server/auth/**",
                        "/rest/developer/server/telemetry/**",
                        "/rest/developer/server/user/**"
                );

        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns(
                        "/rest/developer/server/hdkitservice/telemetry/**",
                        "/rest/developer/server/hdkitservice/user/**"
                );
    }
}
