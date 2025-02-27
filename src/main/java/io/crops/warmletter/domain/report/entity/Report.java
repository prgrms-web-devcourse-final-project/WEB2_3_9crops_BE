package io.crops.warmletter.domain.report.entity;


import io.crops.warmletter.domain.report.enums.ReasonType;
import io.crops.warmletter.domain.report.enums.ReportStatus;
import io.crops.warmletter.domain.report.enums.ReportType;
import io.crops.warmletter.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reports")
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    private ReasonType reasonType;

    private LocalDateTime reportStartedAt;

    @Enumerated(EnumType.STRING)
    private ReportStatus reportStatus;

    private String adminMemo;

    private Long memberId;

    private Long letterId;
    private Long sharePostId;
    private Long eventCommentId;

    public void resolveAutomatically() {
        this.reportStatus = ReportStatus.RESOLVED;
        this.adminMemo = "같은 대상의 신고가 처리되어 자동 승인됨.";
    }
}
