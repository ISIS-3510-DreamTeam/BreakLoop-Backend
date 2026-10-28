package com.dreamteam.breakloop.config;

import com.dreamteam.breakloop.auth.FirebaseAuthInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ObjectProvider<FirebaseAuthInterceptor> authInterceptor;

    public WebConfig(ObjectProvider<FirebaseAuthInterceptor> authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // Absent when breakloop.auth.enabled=false
        authInterceptor.ifAvailable(interceptor -> registry
                .addInterceptor(interceptor)
                .addPathPatterns(
                        "/api/users/{uid}/activity-logs/**",
                        "/api/users/{uid}/suggestion-interactions/**"));
    }
}
