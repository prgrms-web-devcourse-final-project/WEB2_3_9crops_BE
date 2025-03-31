package io.crops.warmletter.domain.report.event;

import io.crops.warmletter.domain.report.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class ReportEventHandlerTest {
    @Mock
    ReportService reportService;

    @InjectMocks
    ReportEventHandler reportEventHandler;

    @Test
    @DisplayName("hanlder 테스트")
    void handleAIResultReady_ShouldInvokeReportService() {
        // Given
        Long reportId = 1L;
        Map<String, String> aiResult = Map.of("status", "RESOLVED");

        AIResultReadyEvent event = new AIResultReadyEvent(reportId, aiResult);

        // When
        reportEventHandler.handleAIResultReady(event);

        // Then
        verify(reportService).updateReportWithAIResult(reportId, aiResult);
    }
}
