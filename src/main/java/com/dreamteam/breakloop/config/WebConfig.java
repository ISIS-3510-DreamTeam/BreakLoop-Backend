package com.dreamteam.breakloop.config;

import com.dreamteam.breakloop.auth.FirebaseAuthInterceptor;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final boolean authEnabled;
    private final ObjectProvider<FirebaseAuth> firebaseAuth;

    public WebConfig(
            @Value("${breakloop.auth.enabled:true}") boolean authEnabled,
            ObjectProvider<FirebaseAuth> firebaseAuth
    ) {
        this.authEnabled = authEnabled;
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // Registered even with auth disabled: the access rule of these routes
        // needs to know who is asking.
        registry
                .addInterceptor(FirebaseAuthInterceptor
                        .authenticationOnly(firebaseAuth::getObject))
                .addPathPatterns(
                        "/api/users/{uid}/public-profile",
                        "/api/users/{uid}/badges");

        if (!authEnabled) {
            return;
        }

        registry
                .addInterceptor(
                        new FirebaseAuthInterceptor(firebaseAuth::getObject))
                .addPathPatterns(
                        "/api/users/{uid}/activity-logs/**",
                        "/api/users/{uid}/suggestion-interactions/**",
                        "/api/users/{uid}/friend-profile",
                        "/api/users/{uid}/friends/**");
    }
}
