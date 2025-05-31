package com.price.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.price.manager.domain",
        "com.price.manager.application",
        "com.price.manager.boot",
        "com.price.manager.driven.repositories",
        "com.price.manager.driving.controllers"
})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}