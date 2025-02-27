package io.crops.warmletter.domain.report.dto.response;

import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.report.entity.Report;
import io.crops.warmletter.domain.report.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UpdateReportResponse {

    private Long id;
    private ReportStatus status;
    private String adminMemo;
    private int warningCount;

    public UpdateReportResponse(Report report, Member reportedMember) {
        this.id = report.getId();
        this.status = report.getReportStatus();
        this.adminMemo = report.getAdminMemo();
        this.warningCount = reportedMember.getWarningCount();
    }
}

