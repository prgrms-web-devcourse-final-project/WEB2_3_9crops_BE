package io.crops.warmletter.domain.report.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
@AllArgsConstructor
public class ReportsResponse {
    private final Long id;
    private final Long reporterId;
    private final Long targetId;
    private final String reporterEmail;
    private final String targetEmail;
    private final String reportType;
    private final String reasonType;
    private final String reason;
    private final String status;
    private final LocalDateTime reportedAt;
    private final LocalDateTime updatedAt;
    private final Long letterId;
    private final Long sharePostId;
    private final Long eventCommentId;
    private final ContentDetail contentDetail;

    public ReportsResponse(
            Long id, Long reporterId, String reporterEmail, Long targetId, String targetEmail,
            String reportType, String reasonType, String reason, String status,
            LocalDateTime reportedAt, LocalDateTime updatedAt,
            Long letterId, Long sharePostId, Long eventCommentId, ContentDetail contentDetail
    ) {
        this.id = id;
        this.reporterId = reporterId;
        this.reporterEmail = reporterEmail;
        this.targetId = targetId;
        this.targetEmail = targetEmail;
        this.reportType = reportType;
        this.reasonType = reasonType;
        this.reason = reason;
        this.status = status;
        this.reportedAt = reportedAt;
        this.updatedAt = updatedAt;
        this.letterId = letterId;
        this.sharePostId = sharePostId;
        this.eventCommentId = eventCommentId;
        this.contentDetail = contentDetail;
    }

    @Getter
    @AllArgsConstructor
    public static class ContentDetail {
        private final String title;
        private final String content;

        // 🔹 title이 필요 없는 경우
        public ContentDetail(String content) {
            this(null, content);
        }
    }
}
