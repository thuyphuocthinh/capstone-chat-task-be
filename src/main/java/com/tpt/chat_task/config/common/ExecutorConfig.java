package com.tpt.chat_task.config.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {
    @Bean
    public ExecutorService taskExecutor() {
        return Executors.newFixedThreadPool(10);
    }
}
