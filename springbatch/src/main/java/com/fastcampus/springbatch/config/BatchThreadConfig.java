package com.fastcampus.springbatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class BatchThreadConfig {
    @Bean("CustomerJobTaskExecutor")    // Qualifier 사용을 위해 TaskExecutor의 이름을 "CustomerJobTaskExecutor"로 설정
    public TaskExecutor taskExecutor() {    // 해당 Job이 실행될 수 있는 공간을 제공하는 역할
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);      // 기본 스레드 수
        executor.setMaxPoolSize(10);      // 최대 스레드 수
        executor.setQueueCapacity(25);    // 대기 큐 크기
        executor.setThreadNamePrefix("customer-batch-"); // 스레드 이름 접두사
        return executor;
    }
}
