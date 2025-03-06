package io.crops.warmletter.global.schedule;

import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.Status;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.facade.NotificationFacade;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DeliverySchedule {

    private final LetterRepository letterRepository;
    private final NotificationFacade notificationFacade;

    @Transactional
    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul")
    public void processDeliveryCompletion() {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("--------- 배송 완료 처리 시작: {} ---------", currentTime);

        LocalDateTime now = LocalDateTime.now();

        // 배송 완료 조건을 만족하는 편지 목록 조회 (배송 중이면서 배송 완료 시간이 현재보다 이전인 편지)
        List<Letter> lettersToComplete = letterRepository.findByStatusAndDeliveryCompletedAtLessThanEqual(
                Status.IN_DELIVERY, now);
        // lettersToComplete 조건을 만족하는 편지를 보낸 사람의 zipCode 조회
        List<Object[]> zipCodeData = letterRepository.findZipCodeByLettersToComplete(now);
        Map<Long, String> senderZipCodes = zipCodeData.stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (String) row[1]));

        if (!lettersToComplete.isEmpty()) {
            log.info("배송 완료 처리할 편지 수: {}", lettersToComplete.size());

            // 각 편지의 상태를 DELIVERED로 변경
            for (Letter letter : lettersToComplete) {
                letter.updateStatus(Status.DELIVERED);
                log.info("편지 ID: {} 배송 완료 처리됨", letter.getId());
                // 도착 알림 전송
                notificationFacade.sendNotification(
                        senderZipCodes.get(letter.getWriterId()),
                        letter.getReceiverId(),
                        AlarmType.LETTER,
                        letter.getId().toString());
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