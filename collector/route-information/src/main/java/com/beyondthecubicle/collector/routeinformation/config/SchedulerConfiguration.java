package com.beyondthecubicle.collector.routeinformation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfiguration {

    private static final int MAX_POOL_SIZE = 10;
    private static final String SCHEDULER_NAME_PREFIX = "Scheduler-";

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(MAX_POOL_SIZE);
        threadPoolTaskScheduler.setThreadNamePrefix(SCHEDULER_NAME_PREFIX);
        return threadPoolTaskScheduler;
    }
}
