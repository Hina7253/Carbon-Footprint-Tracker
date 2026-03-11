package com.example.LatestStable.config;

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
public class AppConfig {

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
                .followRedirects(true)
                .followSslRedirects(true)
                .build();
    }

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("carbonscope-");
        executor.initialize();
        return executor;
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:4200"
        ));
        config.setAllowedMethods(
                Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public CarbonConstants carbonConstants() {
        return new CarbonConstants(kwhPerGb, gramsCo2PerKwh);
    }

    public static class CarbonConstants {
        private final double kwhPerGb;
        private final double gramsCo2PerKwh;

        public CarbonConstants(double kwhPerGb, double gramsCo2PerKwh) {
            this.kwhPerGb        = kwhPerGb;
            this.gramsCo2PerKwh  = gramsCo2PerKwh;
        }

        public double kwhPerGb()       { return kwhPerGb; }
        public double gramsCo2PerKwh() { return gramsCo2PerKwh; }
    }
}