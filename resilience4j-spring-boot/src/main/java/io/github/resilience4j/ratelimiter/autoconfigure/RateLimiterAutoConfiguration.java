/*
 * Copyright 2017 Bohdan Storozhuk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.resilience4j.ratelimiter.autoconfigure;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.monitoring.endpoint.RateLimiterEndpoint;
import io.github.resilience4j.ratelimiter.monitoring.health.RateLimiterHealthIndicator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration} for resilience4j ratelimiter.
 */
@Configuration
@ConditionalOnClass(RateLimiter.class)
@EnableConfigurationProperties(RateLimiterProperties.class)
@Import(RateLimiterConfigurationOnMissingBean.class)
public class RateLimiterAutoConfiguration {
    private final RateLimiterProperties rateLimiterProperties;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final ConfigurableBeanFactory beanFactory;

    public RateLimiterAutoConfiguration(RateLimiterProperties rateLimiterProperties, RateLimiterRegistry rateLimiterRegistry, ConfigurableBeanFactory beanFactory) {
        this.rateLimiterProperties = rateLimiterProperties;
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.beanFactory = beanFactory;
    }

    @Bean
    public RateLimiterEndpoint rateLimiterEndpoint(RateLimiterRegistry rateLimiterRegistry) {
        return new RateLimiterEndpoint(rateLimiterRegistry);
    }

    @PostConstruct
    public void configureHealthIndicators() {
        rateLimiterProperties.getLimiters().forEach(
                (name, properties) -> {
                    RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(name);
                    if (properties.getRegisterHealthIndicator()) {
                        createHealthIndicatorForLimiter(beanFactory, name, rateLimiter);
                    }
                }
        );
    }

    private void createHealthIndicatorForLimiter(ConfigurableBeanFactory beanFactory, String name, RateLimiter rateLimiter) {
        beanFactory.registerSingleton(
                name + "RateLimiterHealthIndicator",
                new RateLimiterHealthIndicator(rateLimiter)
        );
    }


}
