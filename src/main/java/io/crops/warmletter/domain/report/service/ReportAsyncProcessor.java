package io.crops.warmletter.domain.report.service;

import io.crops.warmletter.domain.report.enums.ReasonType;
import io.crops.warmletter.domain.report.event.AIResultReadyEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportAsyncProcessor {

    private final ReportModerationService reportModerationService;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    public void processReportInBackground(Long reportId, String content, ReasonType reasonType, String reason) {
        Map<String, String> result = reportModerationService.moderateText(content, reasonType, reason);
        eventPublisher.publishEvent(new AIResultReadyEvent(reportId, result));
    }
}
