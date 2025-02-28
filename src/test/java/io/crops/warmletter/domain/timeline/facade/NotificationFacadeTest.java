package io.crops.warmletter.domain.timeline.facade;

import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationFacadeTest {
    @Mock
    private NotificationService notificationService;  // mock NotificationService

    @InjectMocks
    private NotificationFacade notificationFacade;  // 테스트할 대상 객체

    @Test
    @DisplayName("알림 전송 요청 정상 전달")
    void send_createNotification_success() {
        // given
        String senderZipCode = "12345";
        Long receiverId = 1L;
        AlarmType alarmType = AlarmType.POSTED;  // 예시로 POSTED 사용
        String data = "Test";

        // when
        notificationFacade.sendNotification(senderZipCode, receiverId, alarmType, data);

        // then
        verify(notificationService, times(1))
                .createNotification(senderZipCode, receiverId, alarmType, data);
    }


}