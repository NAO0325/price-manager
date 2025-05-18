package com.price.manager.driven.repositories.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConfigurationProperties("spring.datasource")
@EntityScan("com.price.manager.driven.repositories.models")
@EnableJpaRepositories(basePackages = {"com.price.manager.driven.repositories"})
public class RepositoryConfig {
}
