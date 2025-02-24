package io.crops.warmletter.domain.report.dto.request;
import io.crops.warmletter.domain.report.enums.ReasonType;
import io.crops.warmletter.domain.report.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportRequest {

    private ReportType reportType; // 신고 유형 (LETTER, SHARE_POST, EVENT_COMMENT)
    private ReasonType reasonType;
    private String reason;
    private Long letterId;
    private Long sharePostId;
    private Long eventCommentId;

}
