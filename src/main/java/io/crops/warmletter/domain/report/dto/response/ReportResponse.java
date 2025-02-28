package io.crops.warmletter.domain.report.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.crops.warmletter.domain.report.entity.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportResponse {
    private final Long id;
    private final String reportType; // 예: "LETTER", "SHARE_BOARD", "EVENT_COMMENT"
    private final String reasonType;   // 예: "ABUSE", "ETC", 등
    private final String reason;       // 상세 신고 사유
    private final String status;       // 예: "PENDING"
    private final LocalDateTime reportedAt;
    private final Long letterId;
    private final Long sharePostId;
    private final Long eventCommentId;

    public ReportResponse(Report report) {
        this.id = report.getId();
        this.reportType = report.getReportType().name();
        this.reasonType = report.getReasonType().name();
        this.reason = report.getReason();
        this.status = report.getReportStatus().name();
        this.reportedAt = report.getReportStartedAt();
        this.letterId = report.getLetterId();
        this.sharePostId = report.getSharePostId();
        this.eventCommentId = report.getEventCommentId();
    }

}
