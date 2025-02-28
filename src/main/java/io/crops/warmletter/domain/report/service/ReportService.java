package io.crops.warmletter.domain.report.service;


import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.eventpost.entity.EventComment;
import io.crops.warmletter.domain.eventpost.exception.EventCommentNotFoundException;
import io.crops.warmletter.domain.eventpost.repository.EventCommentRepository;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.exception.LetterNotFoundException;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.exception.MemberNotFoundException;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import io.crops.warmletter.domain.report.dto.request.CreateReportRequest;
import io.crops.warmletter.domain.report.dto.request.UpdateReportRequest;
import io.crops.warmletter.domain.report.dto.response.ReportResponse;
import io.crops.warmletter.domain.report.dto.response.ReportsResponse;
import io.crops.warmletter.domain.report.dto.response.UpdateReportResponse;
import io.crops.warmletter.domain.report.entity.Report;
import io.crops.warmletter.domain.report.enums.ReportStatus;
import io.crops.warmletter.domain.report.enums.ReportType;
import io.crops.warmletter.domain.report.exception.DuplicateReportException;
import io.crops.warmletter.domain.report.exception.InvalidReportRequestException;
import io.crops.warmletter.domain.report.exception.ReportNotFoundException;
import io.crops.warmletter.domain.report.repository.ReportRepository;
import io.crops.warmletter.domain.share.entity.SharePost;
import io.crops.warmletter.domain.share.repository.SharePostRepository;
import io.crops.warmletter.domain.share.repository.ShareProposalRepository;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.facade.NotificationFacade;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final LetterRepository letterRepository;
    private final SharePostRepository sharePostRepository;
    private final EventCommentRepository eventCommentRepository;
    private final MemberRepository memberRepository;
    private final ShareProposalRepository shareProposalRepository;

    private final AuthFacade authFacde;
    private final NotificationFacade notificationFacade;

    @Transactional
    public UpdateReportResponse updateReport(Long reportId, UpdateReportRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        //신고 대상 찾기 (Letter, SharePost, EventComment 중 하나)
        Long targetMemberId = getTargetMemberId(report);
        Member reportedMember = memberRepository.findById(targetMemberId)
                .orElseThrow(MemberNotFoundException::new);

        //신고 처리: 관리자 메모 & 상태 업데이트
        report = report.toBuilder()
                .reportStatus(request.getStatus())
                .adminMemo(request.getAdminMemo())
                .build();
        reportRepository.save(report);
        if (request.getStatus() == ReportStatus.RESOLVED) {
            deactivateTarget(report);
            reportedMember.increaseWarningCount();
            memberRepository.save(reportedMember);
            resolvePendingReports(report);
            // targetMemberId로 알림 전송 TODO : 배포 후 테스트 예정
            notificationFacade.sendNotification(null, targetMemberId, AlarmType.REPORT, report.getAdminMemo());
        }
        return new UpdateReportResponse(report,reportedMember);
    }




    public Page<ReportsResponse> getAllReports(String reportType, String status, Pageable pageable) {
        return reportRepository.findAllWithFilters(reportType, status, pageable);
    }

    @Transactional
    public ReportResponse createReport(CreateReportRequest request) {
        Long memberId = authFacde.getCurrentUserId();
//        Long memberId = 10L;
        validateRequest(request, memberId);
        Report.ReportBuilder builder = Report.builder()
                .memberId(memberId)  // 고정 신고자 ID 사용
                .reasonType(request.getReasonType())
                .reason(Optional.ofNullable(request.getReason()).orElse(""))
                .reportStartedAt(LocalDateTime.now())
                .reportStatus(ReportStatus.PENDING);

        switch (request.getReportType()) {
            case LETTER -> builder
                    .reportType(ReportType.LETTER)
                    .letterId(request.getLetterId());
            case SHARE_POST -> builder
                    .reportType(ReportType.SHARE_POST)
                    .sharePostId(request.getSharePostId());
            case EVENT_COMMENT -> builder
                    .reportType(ReportType.EVENT_COMMENT)
                    .eventCommentId(request.getEventCommentId());
        }
        Report report = builder.build();
        Report savedReport = reportRepository.save(report);
        return new ReportResponse(savedReport);

    }


    void deactivateTarget(Report report) {
        if (report.getLetterId() != null) {
            Letter letter = letterRepository.findById(report.getLetterId())
                    .orElseThrow(LetterNotFoundException::new);
            letter.inactive();
        } else if (report.getSharePostId() != null) {
            SharePost sharePost = sharePostRepository.findById(report.getSharePostId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SHARE_POST_NOT_FOUND));
            sharePost.deactivate();
        } else if (report.getEventCommentId() != null) {
            EventComment eventComment = eventCommentRepository.findById(report.getEventCommentId())
                    .orElseThrow(EventCommentNotFoundException::new);
            eventComment.softDelete();
        }
    }


    private Long getTargetMemberId(Report report) {
        if (report.getLetterId() != null) {
            return letterRepository.findById(report.getLetterId())
                    .orElseThrow(LetterNotFoundException::new)
                    .getWriterId();
        }
        if (report.getSharePostId() != null) {
            return shareProposalRepository.findById(report.getSharePostId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SHARE_PROPOSAL_NOTFOUND))
                    .getRequesterId();
        }
        if (report.getEventCommentId() != null) {
            return eventCommentRepository.findById(report.getEventCommentId())
                    .orElseThrow(EventCommentNotFoundException::new)
                    .getWriterId();
        }
        throw new InvalidReportRequestException();
    }


    @Transactional
    public void resolvePendingReports(Report report) {
        List<Report> pendingReports = reportRepository.findBySameTargetAndStatus(
                report.getLetterId(),
                report.getSharePostId(),
                report.getEventCommentId(),
                ReportStatus.PENDING
        );

        for (Report pendingReport : pendingReports) {
            pendingReport.resolveAutomatically();
        }

        reportRepository.flush();
    }



    void validateRequest(CreateReportRequest request, Long memberId) {
        // 공통: 신고 대상 ID 중 하나만 있어야 함
        boolean isLetter = request.getLetterId() != null;
        boolean isSharePost = request.getSharePostId() != null;
        boolean isEventComment = request.getEventCommentId() != null;
        int count = (isLetter ? 1 : 0) + (isSharePost ? 1 : 0) + (isEventComment ? 1 : 0);
        if(count != 1) {
            throw new InvalidReportRequestException();
        }


        // 타입별 추가 검증
        switch (request.getReportType()) {
            case LETTER:
                validateLetterReport(request, memberId);
                break;
            case SHARE_POST:
                validateSharePostReport(request, memberId);
                break;
            case EVENT_COMMENT:
                validateEventCommentReport(request, memberId);
                break;
        }
    }

    void validateLetterReport(CreateReportRequest request, Long memberId) {
        if (!letterRepository.existsById(request.getLetterId())) {
            throw new LetterNotFoundException();
        }
        if(reportRepository.existsByLetterIdAndMemberId(request.getLetterId(), memberId)) {
            throw new DuplicateReportException();
        }
    }

    void validateSharePostReport(CreateReportRequest request, Long memberId) {
        if(!sharePostRepository.existsById(request.getSharePostId())) {
            throw new BusinessException(ErrorCode.SHARE_POST_NOT_FOUND);
        }
        // sharePostRepository.existsById() 등 추가 검증 가능
        if(reportRepository.existsBySharePostIdAndMemberId(request.getSharePostId(), memberId)) {
            throw new DuplicateReportException();
        }
    }


    void validateEventCommentReport(CreateReportRequest request, Long memberId) {
        if(!eventCommentRepository.existsById(request.getEventCommentId())) {
            throw new EventCommentNotFoundException();
        }
        if(reportRepository.existsByEventCommentIdAndMemberId(request.getEventCommentId(), memberId)) {
            throw new DuplicateReportException();
        }
    }

}
