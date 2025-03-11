package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.Status;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.timeline.dto.request.NotificationRequest;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LetterProcessingService {

    private final LetterRepository letterRepository;
    private final ApplicationEventPublisher notificationPublisher;

    /**
     * 편지 배송 완료 처리 및 알림 전송
     * 개별 트랜잭션으로 처리하여 다른 편지 처리에 영향을 주지 않음
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processDeliveryCompletion(Letter letter, String senderZipCode) {
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