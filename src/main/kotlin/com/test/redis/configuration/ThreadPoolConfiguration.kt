package com.test.redis.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class ThreadPoolConfiguration {

    @Bean
    fun cloudEventExecutor(): ThreadPoolTaskExecutor {
        return ThreadPoolTaskExecutor().apply {
            this.corePoolSize = 500
            this.maxPoolSize = 1000
            setThreadNamePrefix("threadPoolTaskExecutor-")
            initialize()
        }
    }
}