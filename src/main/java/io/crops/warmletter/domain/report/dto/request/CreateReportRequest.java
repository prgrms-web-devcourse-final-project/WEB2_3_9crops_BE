package io.crops.warmletter.domain.report.dto.request;
import io.crops.warmletter.domain.report.enums.ReasonType;
import io.crops.warmletter.domain.report.enums.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportRequest {
    @Schema(description = "신고 유형 (LETTER, SHARE_POST, EVENT_COMMENT 중 하나)", example = "LETTER")
    private ReportType reportType; // 신고 유형 (LETTER, SHARE_POST, EVENT_COMMENT)

    @Schema(description = "신고 사유 유형 (ABUSE, DEFAMATION, HARASSMENT, THREATS, ETC)", example = "ABUSE")
    private ReasonType reasonType;

    @Schema(description = "신고 상세 사유 (선택 입력 가능)", example = "욕설이 포함되어 있습니다.")
    private String reason;

    @Schema(description = "신고할 편지 ID (reportType이 LETTER일 때 입력)", example = "123")
    private Long letterId;

    @Schema(description = "신고할 공유 게시글 ID (reportType이 SHARE_POST일 때 입력)", example = "456")
    private Long sharePostId;


    @Schema(description = "신고할 이벤트 댓글 ID (reportType이 EVENT_COMMENT일 때 입력)", example = "789")
    private Long eventCommentId;

}
