package io.crops.warmletter.domain.letter.service;

import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.*;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.timeline.dto.request.NotificationRequest;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LetterProcessingServiceTest {

    @Mock
    private LetterRepository letterRepository;

    @Mock
    private ApplicationEventPublisher notificationPublisher;

    @InjectMocks
    private LetterProcessingService letterProcessingService;

    @Captor
    private ArgumentCaptor<NotificationRequest> notificationCaptor;

    private final String ZIP_CODE = "12345";
    private final Long TEST_RECEIVER_ID = 100L;
    private final Long TEST_WRITER_ID = 200L;
    private final Long TEST_LETTER_ID = 300L;

    private Letter createTestLetter() {
        // Builder 패턴을 사용하여 Letter 객체 생성
        Letter letter = Letter.builder()
                .writerId(TEST_WRITER_ID)
                .receiverId(TEST_RECEIVER_ID)
                .parentLetterId(null)
                .letterType(LetterType.DIRECT)
                .category(Category.ETC)
                .title("테스트 편지")
                .content("테스트 내용입니다.")
                .status(Status.IN_DELIVERY)
                .fontType(FontType.DEFAULT)
                .paperType(PaperType.PAPER)
                .matchingId(null)
                .build();

        ReflectionTestUtils.setField(letter, "id", TEST_LETTER_ID);

        return letter;
    }

    @Test
    @DisplayName("편지 배송 완료 처리 - 상태 업데이트 및 저장 검증")
    void processDeliveryCompletion_ShouldUpdateStatusAndSave() {
        // Given
        Letter testLetter = createTestLetter();

        // When
        letterProcessingService.processDeliveryCompletion(testLetter, ZIP_CODE);

        // Then
        assertThat(testLetter.getStatus()).isEqualTo(Status.DELIVERED);
        verify(letterRepository, times(1)).save(testLetter);
    }

    @Test
    @DisplayName("편지 배송 완료 처리 - 알림 전송 검증")
    void processDeliveryCompletion_ShouldSendNotification() {
        // Given
        Letter testLetter = createTestLetter();

        // When
        letterProcessingService.processDeliveryCompletion(testLetter, ZIP_CODE);

        // Then
        verify(notificationPublisher, times(1)).publishEvent(notificationCaptor.capture());

        NotificationRequest capturedRequest = notificationCaptor.getValue();
        assertThat(capturedRequest.getSenderZipCode()).isEqualTo(ZIP_CODE);
        assertThat(capturedRequest.getReceiverId()).isEqualTo(TEST_RECEIVER_ID);
        assertThat(capturedRequest.getAlarmType()).isEqualTo(AlarmType.LETTER);
        assertThat(capturedRequest.getData()).isEqualTo(TEST_LETTER_ID.toString());
    }

    @Test
    @DisplayName("편지 배송 완료 처리 - 수신자 ID가 null인 경우 알림 미전송")
    void processDeliveryCompletion_WhenReceiverIdIsNull_ShouldNotSendNotification() {
        // Given
        Letter testLetter = Letter.builder()
                .writerId(TEST_WRITER_ID)
                .receiverId(null) // 수신자 ID를 null로 설정
                .title("테스트 편지")
                .content("테스트 내용입니다.")
                .status(Status.IN_DELIVERY)
                .build();
        ReflectionTestUtils.setField(testLetter, "id", TEST_LETTER_ID);

        // When
        letterProcessingService.processDeliveryCompletion(testLetter, ZIP_CODE);

        // Then
        verify(letterRepository, times(1)).save(testLetter);
        verify(notificationPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("편지 배송 완료 처리 - 발신자 우편번호가 null인 경우 알림 미전송")
    void processDeliveryCompletion_WhenSenderZipCodeIsNull_ShouldNotSendNotification() {
        // Given
        Letter testLetter = createTestLetter();

        // When
        letterProcessingService.processDeliveryCompletion(testLetter, null);

        // Then
        verify(letterRepository, times(1)).save(testLetter);
        verify(notificationPublisher, never()).publishEvent(any());
    }
}