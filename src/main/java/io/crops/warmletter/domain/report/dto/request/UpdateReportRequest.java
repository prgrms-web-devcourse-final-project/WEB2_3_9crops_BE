package io.crops.warmletter.domain.report.dto.request;

import io.crops.warmletter.domain.report.enums.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReportRequest {
    @NotNull
    @Schema(description = "신고 상태 (PENDING: 대기 중, RESOLVED: 처리 완료, REJECTED: 거부됨)", example = "PENDING")
    private ReportStatus status;

    @Schema(description = "관리자 메모 (신고 처리 관련 메모, 선택 입력 가능)", example = "사용자 신고 검토 완료")
    private String adminMemo;
}
