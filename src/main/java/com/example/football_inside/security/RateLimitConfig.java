package com.example.football_inside.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {
    @Bean
    public Bucket loginBucket() {
        long capacity = 5;
        Refill refill = Refill.intervally(5, Duration.ofMinutes(1));
        return Bucket.builder()
                .addLimit(Bandwidth.classic(capacity, refill))
                .build();
    }
}
