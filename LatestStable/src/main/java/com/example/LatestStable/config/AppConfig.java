package com.carbonscope.config;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;


@Configuration
public class AppConfig<OkHttpClient> {

    // Read custom properties from application.properties
    // ${carbon.kwh-per-gb} reads the value "0.06"
    // :0.06 = default value if property is not found
    @Value("${carbon.kwh-per-gb:0.06}")
    private double kwhPerGb;

    @Value("${carbon.grams-co2-per-kwh:490}")
    private double gramsCo2PerKwh;


    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                // Follow redirects (e.g., http → https)
                .followRedirects(true)
                .followSslRedirects(true)
                // Allow HTTP/2 for faster requests
                .build();
    }


    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("carbonscope-async-");
        executor.initialize();
        return executor;
    }

    // ── CORS CONFIGURATION ────────────────────────────────────────

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Which frontend origins are allowed to call our API?
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",  // React development server
                "http://localhost:5173",  // Vite development server
                "http://localhost:4200",  // Angular development server
                "https://carbonscope.app" // Production domain (update with yours)
        ));

        // Which HTTP methods are allowed?
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Which request headers are allowed?
        config.setAllowedHeaders(List.of("*"));

        // Allow cookies/credentials in cross-origin requests
        config.setAllowCredentials(true);

        // Cache preflight response for 1 hour (performance optimization)
        config.setMaxAge(3600L);

        // Apply this CORS config to ALL endpoints (/**)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }


    @Bean
    public CarbonConstants carbonConstants() {
        return new CarbonConstants(kwhPerGb, gramsCo2PerKwh);
    }

    public record CarbonConstants(double kwhPerGb, double gramsCo2PerKwh) {}
}