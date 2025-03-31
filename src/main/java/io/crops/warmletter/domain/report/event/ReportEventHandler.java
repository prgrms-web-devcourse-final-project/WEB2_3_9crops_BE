package io.crops.warmletter.domain.report.event;

import io.crops.warmletter.domain.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReportEventHandler {

    private final ReportService reportService;

    @Async
    @EventListener
  //  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAIResultReady(AIResultReadyEvent event) {
        reportService.updateReportWithAIResult(event.reportId(), event.result());
    }

}
