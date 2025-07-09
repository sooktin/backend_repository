package com.sooktin.backend.global;


import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

//@Async=>스프링 AOP에 의한 프록시 패턴 기반 동작.
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    @Value("${spring.application.async.email.core-pool-size:2}")
    private int emailCorePoolSize;

    @Value("${spring.application.async.email.max-pool-size:4}")
    private int emailMaxPoolSize;

    @Value("${spring.application.async.email.queue-capacity:50}")
    private int emailQueueCapacity;

    @Bean("emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(emailCorePoolSize);
        executor.setMaxPoolSize(emailMaxPoolSize);
        executor.setQueueCapacity(emailQueueCapacity);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("email-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(20);
        executor.initialize();
        log.info("이메일 TaskExecutor 초기화 완료 - CorePool: {}, MaxPool: {}, Queue: {}",
                emailCorePoolSize, emailMaxPoolSize, emailQueueCapacity);
        return executor;

    }

    @Override
    public Executor getAsyncExecutor() {
        return emailTaskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("비동기 작업 예외 발생 - Method: {}. Exception: {}",
                    method.getName(),ex.getMessage(), ex);
        };
    }
}
