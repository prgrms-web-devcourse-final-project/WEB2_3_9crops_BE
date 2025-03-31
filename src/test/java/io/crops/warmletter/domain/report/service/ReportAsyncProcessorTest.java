package io.crops.warmletter.domain.report.service;
import io.crops.warmletter.domain.report.enums.ReasonType;
import io.crops.warmletter.domain.report.event.AIResultReadyEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportAsyncProcessorTest {

    @Mock
    ReportModerationService reportModerationService;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    ReportAsyncProcessor reportAsyncProcessor;

    @Test
    void processReportInBackground_shouldModerateTextAndPublishEvent() {
        // Given
        Long reportId = 1L;
        String content = "신고 대상 콘텐츠";
        ReasonType reasonType = ReasonType.ETC;
        String reason = "기타 사유";

        Map<String, String> mockResult = Map.of("status", "RESOLVED");
        when(reportModerationService.moderateText(content, reasonType, reason)).thenReturn(mockResult);

        // When
        reportAsyncProcessor.processReportInBackground(reportId, content, reasonType, reason);

        // Then
        verify(reportModerationService).moderateText(content, reasonType, reason);

        // 이벤트 검증 (캡처해서 값까지 확인)
        ArgumentCaptor<AIResultReadyEvent> captor = ArgumentCaptor.forClass(AIResultReadyEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        AIResultReadyEvent event = captor.getValue();
        assertThat(event.reportId()).isEqualTo(reportId);
        assertThat(event.result()).isEqualTo(mockResult);
    }
}
