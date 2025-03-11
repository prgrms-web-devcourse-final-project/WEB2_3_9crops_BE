package io.crops.warmletter.global.schedule;

import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.Status;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.timeline.dto.request.NotificationRequest;
import io.crops.warmletter.domain.timeline.dto.response.LetterAlarmResponse;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DeliverySchedule {

    private final LetterRepository letterRepository;
    private final ApplicationEventPublisher notificationPublisher;
    @Qualifier("deliveryTaskExecutor")
    private final AsyncTaskExecutor taskExecutor;

    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul")
    public void processDeliveryCompletion() {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("--------- 배송 완료 처리 시작: {} ---------", currentTime);

        LocalDateTime now = LocalDateTime.now();

        // 배송 완료 조건을 만족하는 편지 목록 조회
        List<Letter> lettersToComplete = letterRepository.findByStatusAndDeliveryCompletedAtLessThanEqual(
                Status.IN_DELIVERY, now);

        // zipCode 조회
        List<LetterAlarmResponse> zipCodeData = letterRepository.findZipCodeByLettersToComplete(now);
        Map<Long, String> senderZipCodes = zipCodeData.stream()
                .collect(Collectors.toMap(
                        LetterAlarmResponse::getWriterId,
                        LetterAlarmResponse::getZipCode,
                        (existingZipCode, newZipCode) -> existingZipCode
                ));

        if (!lettersToComplete.isEmpty()) {
            log.info("배송 완료 처리할 편지 수: {}", lettersToComplete.size());

            // 결과 추적을 위한 CompletableFuture 목록
            List<CompletableFuture<Boolean>> futures = new ArrayList<>();

            // 각 편지를 비동기적으로 처리
            for (Letter letter : lettersToComplete) {
                CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        processLetter(letter, senderZipCodes.get(letter.getWriterId()));
                        log.info("편지 ID: {} 배송 완료 처리됨", letter.getId());
                        return true;
                    } catch (Exception e) {
                        log.error("편지 ID: {} 배송 완료 처리 실패: {}", letter.getId(), e.getMessage(), e);
                        return false;
                    }
                }, taskExecutor);

                futures.add(future);
            }

            // 모든 비동기 작업 완료 대기 (옵션)
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 성공/실패 편지 수 계산
            long successCount = futures.stream().filter(f -> {
                try {
                    return f.get();
                } catch (Exception e) {
                    return false;
                }
            }).count();

            log.info("총 {}개 중 {}개의 편지 배송 완료 처리 성공", lettersToComplete.size(), successCount);
        } else {
            log.info("배송 완료 처리할 편지가 없습니다.");
        }
        log.info("--------- 배송 완료 처리 완료 ---------");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processLetter(Letter letter, String senderZipCode) {
        // 편지 상태 업데이트
        letter.updateStatus(Status.DELIVERED);
        letterRepository.save(letter);

        // 알림 전송
        if (letter.getReceiverId() != null && senderZipCode != null) {
            notificationPublisher.publishEvent(NotificationRequest.builder()
                    .senderZipCode(senderZipCode)
                    .receiverId(letter.getReceiverId())
                    .alarmType(AlarmType.LETTER)
                    .data(letter.getId().toString())
                    .build());
        }
    }
}