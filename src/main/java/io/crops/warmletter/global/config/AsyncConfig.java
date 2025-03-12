package io.crops.warmletter.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public AsyncTaskExecutor deliveryTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;

        executor.setCorePoolSize(corePoolSize);              // CPU 코어 수 * 2
        executor.setMaxPoolSize(corePoolSize * 2);           // 부하가 높을 때 확장 가능한 여유
        executor.setQueueCapacity(corePoolSize * 4);         // 처리 대기열 크기
        executor.setKeepAliveSeconds(60);                    // 유휴 스레드 유지 시간
        executor.setThreadNamePrefix("delivery-async-");

        // 대기열이 가득 찼을 때 CallerRunsPolicy를 사용하여
        // 호출 스레드에서 작업 실행 (시스템 과부하 방지)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}