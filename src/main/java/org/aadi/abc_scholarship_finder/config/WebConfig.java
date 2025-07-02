package org.aadi.abc_scholarship_finder.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Direct mapping for login and register, if not handled by AuthController directly.
        // This ensures Spring Security's default login page works if you uncomment it in SecurityConfig,
        // but we're handling it via AuthController here.
        // registry.addViewController("/login").setViewName("login");
        // registry.addViewController("/register").setViewName("register");
    }
}