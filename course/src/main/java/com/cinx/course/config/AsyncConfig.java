package com.cinx.course.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "asyncTaskExecutor")
    public Executor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);       // số thread tối thiểu
        executor.setMaxPoolSize(20);       // số thread tối đa
        executor.setQueueCapacity(100);    // queue chờ
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
