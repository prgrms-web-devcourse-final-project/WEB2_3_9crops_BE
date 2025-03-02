package io.crops.warmletter.global.config;

import io.crops.warmletter.domain.member.service.MemberSuspensionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduleConfig implements SchedulingConfigurer {

    private final MemberSuspensionService memberSuspensionService;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        // 코어 수 만큼 스레드풀 설정
        scheduler.setPoolSize(corePoolSize);
        scheduler.setThreadNamePrefix("scheduled-task-");
        // 애플리케이션 종료 시 스레드 풀이 모든 작업을 완료할 때까지 기다릴 최대 시간(초)을 설정
        scheduler.setAwaitTerminationSeconds(60);
        // 작업 실행 중 오류가 발생해도 스케줄러는 계속 실행되며, 오류는 로그에 기록
        scheduler.setErrorHandler(throwable -> log.error("Scheduled task error", throwable));
        // 스레드 풀이 작업을 수용할 수 없을 때(모든 스레드가 사용 중이고 큐가 가득 찼을 때) 취할 정책을 설정
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return scheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler());
    }

    // 매일 00시에 실행
    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleMemberSuspension() {
        int suspendedCount = memberSuspensionService.suspendMembersWithExcessiveWarnings();
        log.info("일일 회원 정지 처리 완료: {}명의 회원이 정지되었습니다.", suspendedCount);
    }
}
