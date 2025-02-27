package io.crops.warmletter.domain.report.dto.request;

import io.crops.warmletter.domain.report.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReportRequest {
    @NotNull
    private ReportStatus status;

    private String adminMemo;
}
