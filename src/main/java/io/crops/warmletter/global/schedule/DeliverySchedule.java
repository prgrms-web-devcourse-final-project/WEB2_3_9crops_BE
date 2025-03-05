package io.crops.warmletter.global.schedule;

import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.Status;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DeliverySchedule {

    private final LetterRepository letterRepository;

    @Transactional
    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul")
    public void processDeliveryCompletion() {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("--------- 배송 완료 처리 시작: {} ---------", currentTime);

        LocalDateTime now = LocalDateTime.now();

        // 배송 완료 조건을 만족하는 편지 목록 조회 (배송 중이면서 배송 완료 시간이 현재보다 이전인 편지)
        List<Letter> lettersToComplete = letterRepository.findByStatusAndDeliveryCompletedAtLessThanEqual(
                Status.IN_DELIVERY, now);

        if (!lettersToComplete.isEmpty()) {
            log.info("배송 완료 처리할 편지 수: {}", lettersToComplete.size());

            // 각 편지의 상태를 DELIVERED로 변경
            for (Letter letter : lettersToComplete) {
                letter.updateStatus(Status.DELIVERED);
                log.info("편지 ID: {} 배송 완료 처리됨", letter.getId());
            }

            // 변경사항 저장
            letterRepository.saveAll(lettersToComplete);
            log.info("총 {}개의 편지 배송 완료 처리됨", lettersToComplete.size());
        } else {
            log.info("배송 완료 처리할 편지가 없습니다.");
        }
        log.info("--------- 배송 완료 처리 완료 ---------");
    }
}