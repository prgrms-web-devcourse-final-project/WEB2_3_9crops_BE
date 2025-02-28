package io.crops.warmletter.domain.report.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
@AllArgsConstructor
public class ReportsResponse {
    @Schema(description = "신고 ID", example = "1")
    private final Long id;

    @Schema(description = "신고자 ID", example = "100")
    private final Long reporterId;

    @Schema(description = "신고 대상 ID", example = "200")
    private final Long targetId;

    @Schema(description = "신고자 이메일", example = "reporter@example.com")
    private final String reporterEmail;

    @Schema(description = "신고 대상 이메일", example = "target@example.com")
    private final String targetEmail;

    @Schema(description = "신고 유형 (LETTER: 편지, SHARE_POST: 공유 게시글, EVENT_COMMENT: 이벤트 댓글)", example = "LETTER")
    private final String reportType;

    @Schema(description = "신고 사유 유형 (ABUSE: 욕설, DEFAMATION: 비방, HARASSMENT: 성희롱, THREATS: 폭언, ETC: 기타)", example = "ABUSE")
    private final String reasonType;

    @Schema(description = "신고 상세 사유", example = "부적절한 언어 사용")
    private final String reason;

    @Schema(description = "신고 상태 (PENDING: 대기 중, RESOLVED: 처리 완료, REJECTED: 거부됨)", example = "PENDING")
    private final String status;

    @Schema(description = "신고가 접수된 날짜 및 시간", example = "2024-02-27T12:34:56")
    private final LocalDateTime reportedAt;

    @Schema(description = "신고가 마지막으로 업데이트된 시간", example = "2024-02-28T14:22:30")
    private final LocalDateTime updatedAt;

    @Schema(description = "신고 대상이 편지인 경우 해당 편지 ID", example = "123", nullable = true)
    private final Long letterId;

    @Schema(description = "신고 대상이 공유 게시글인 경우 해당 게시글 ID", example = "456", nullable = true)
    private final Long sharePostId;

    @Schema(description = "신고 대상이 이벤트 댓글인 경우 해당 댓글 ID", example = "789", nullable = true)
    private final Long eventCommentId;

    @Schema(description = "신고 대상의 콘텐츠 상세 정보")
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
        @Schema(description = "신고된 콘텐츠 제목 (선택 사항)", example = "부적절한 게시글 제목", nullable = true)
        private final String title;

        @Schema(description = "신고된 콘텐츠 내용", example = "이 게시글 내용은 부적절합니다.")
        private final String content;

        // 🔹 title이 필요 없는 경우
        public ContentDetail(String content) {
            this(null, content);
        }
    }
}
