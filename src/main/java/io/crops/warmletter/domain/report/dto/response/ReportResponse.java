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
    @Schema(description = "신고 ID", example = "1")
    private final Long id;

    @Schema(description = "신고 유형 (LETTER: 편지 신고, SHARE_POST: 공유 게시글 신고, EVENT_COMMENT: 이벤트 댓글 신고)", example = "LETTER")
    private final String reportType; // 예: "LETTER", "SHARE_BOARD", "EVENT_COMMENT"

    @Schema(description = "신고 사유 유형 (ABUSE: 욕설, DEFAMATION: 비방, HARASSMENT: 성희롱, THREATS: 폭언, ETC: 기타)", example = "ABUSE")
    private final String reasonType;   // 예: "ABUSE", "ETC", 등

    @Schema(description = "신고 상세 사유 (선택 입력 가능)", example = "부적절한 언어 사용")
    private final String reason;       // 상세 신고 사유

    @Schema(description = "신고 상태 (PENDING: 대기 중, RESOLVED: 처리 완료, REJECTED: 거부됨)", example = "PENDIG")
    private final String status;       // 예: "PENDING"
    @Schema(description = "신고가 접수된 날짜 및 시간", example = "2024-02-27T12:34:56")
    private final LocalDateTime reportedAt;

    // 신고 대상 ID들 (해당되지 않는 경우 null)
    @Schema(description = "신고 대상이 편지인 경우 해당 편지 ID", example = "123", nullable = true)
    private final Long letterId;

    @Schema(description = "신고 대상이 공유게시물인 경우 해당 공유게시물 ID", example = "123", nullable = true)
    private final Long sharePostId;

    @Schema(description = "신고 대상이 이벤트댓글인 경우 해당 편지 이벤트댓글", example = "123", nullable = true)
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
