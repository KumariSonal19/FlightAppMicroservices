package com.flightapp.apigateway.config; //

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration; // Note: This import might conflict with class name
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class GlobalCorsConfig { // <--- CHANGED NAME TO MATCH FILE

    @Bean
    public CorsWebFilter corsWebFilter() {
        // Use full package name here to avoid conflict with your class name
        org.springframework.web.cors.CorsConfiguration corsConfig = new org.springframework.web.cors.CorsConfiguration(); //

        // 1. Allow your Angular Frontend specifically
        corsConfig.setAllowedOrigins(Collections.singletonList("http://localhost:4200")); //

        // 2. Allow all standard HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")); //

        // 3. Allow all headers
        corsConfig.addAllowedHeader("*"); //

        // 4. Allow credentials
        corsConfig.setAllowCredentials(true); //
        corsConfig.setMaxAge(3600L); //

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); //
        source.registerCorsConfiguration("/**", corsConfig); //

        return new CorsWebFilter(source); //
    }
}