package io.crops.warmletter.domain.report.dto.response;

import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.report.entity.Report;
import io.crops.warmletter.domain.report.enums.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UpdateReportResponse {

    @Schema(description = "신고 ID", example = "1")
    private Long id;

    @Schema(description = "신고 상태 (PENDING: 대기 중, RESOLVED: 처리 완료, REJECTED: 거부됨)", example = "RESOLVED")
    private ReportStatus status;

    @Schema(description = "관리자 메모 (신고 처리 관련 메모)", example = "신고된 게시글 삭제 완료")
    private String adminMemo;

    @Schema(description = "신고 대상자의 경고 횟수", example = "2")
    private int warningCount;

    public UpdateReportResponse(Report report, Member reportedMember) {
        this.id = report.getId();
        this.status = report.getReportStatus();
        this.adminMemo = report.getAdminMemo();
        this.warningCount = reportedMember.getWarningCount();
    }
}

