package io.crops.warmletter.global.schedule;

import io.crops.warmletter.domain.letter.enums.Status;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableScheduling
public class DeliverySchedule {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    private final LetterRepository letterRepository;

    // 10분마다 실행으로 변경 (부하 감소)
    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul")
    public void runDeliveryJob() throws Exception {
        log.info("[runDeliveryJob] 실행");


        // 실행 전 배송 대상 편지 확인 (불필요한 배치 실행 방지)
        long count = letterRepository.countByStatusAndDeliveryCompletedAtLessThanEqual(
                Status.IN_DELIVERY, LocalDateTime.now());

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"));
        log.info("---------배송 완료 처리 시작 : {}, 대상 편지: {}개---------", date, count);

        if (count == 0) {
            log.info("배송 완료 처리할 편지가 없습니다. 배치 작업을 실행하지 않습니다.");
            return;
        }

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", date)
                .addLong("time", System.currentTimeMillis())
                .addString("uniqueId", UUID.randomUUID().toString())  // 추가
                .toJobParameters();

        try {
            jobLauncher.run(jobRegistry.getJob("deliveryBatchJob"), jobParameters);
        } catch (Exception e) {
            log.error("배송 완료 배치 작업 실행 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}