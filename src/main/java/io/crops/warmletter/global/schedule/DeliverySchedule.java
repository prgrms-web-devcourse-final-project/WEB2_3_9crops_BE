package io.crops.warmletter.global.schedule;

import io.crops.warmletter.domain.letter.repository.LetterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DeliverySchedule {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    private final LetterRepository letterRepository;



    @Scheduled(cron = "30 */1 * * * *", zone = "Asia/Seoul")
    public void runDeliveryJob()throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        String date = dateFormat.format(new Date());

        log.info("---------배송 완료 처리 시작 : {}---------", date);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", date)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("deliveryBatchJob"), jobParameters);
    }

//    @Transactional
//    @Scheduled(cron = "30 */1 * * * *", zone = "Asia/Seoul")
//    public void processDeliveryCompletion() {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
//        String date = dateFormat.format(new Date());
//
//        log.info("---------배송 완료 처리 시작 : {}---------", date);
//
//        LocalDateTime now = LocalDateTime.now();
//
//        // 배송 완료 조건을 만족하는 편지 목록 조회
//        List<Letter> lettersToComplete = letterRepository.findByStatusAndDeliveryCompletedAtLessThanEqual(Status.IN_DELIVERY, now);
//
//        if (!lettersToComplete.isEmpty()) {
//            log.info("배송 완료 처리할 편지 수: {}", lettersToComplete.size());
//
//            // 각 편지의 상태를 DELIVERED로 변경
//            for (Letter letter : lettersToComplete) {
//                letter.updateStatus(Status.DELIVERED);
//                log.info("편지 ID: {} 배송 완료 처리됨", letter.getId());
//            }
//
//            // 변경사항 저장
//            letterRepository.saveAll(lettersToComplete);
//            log.info("총 {} 개의 편지 배송 완료 처리됨", lettersToComplete.size());
//        } else {
//            log.info("배송 완료 처리할 편지가 없습니다.");
//        }
//    }
}
