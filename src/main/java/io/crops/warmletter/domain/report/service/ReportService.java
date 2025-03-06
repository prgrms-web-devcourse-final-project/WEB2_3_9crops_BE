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
import io.crops.warmletter.domain.share.exception.SharePostNotFoundException;
import io.crops.warmletter.domain.share.exception.ShareProposalNotFoundException;
import io.crops.warmletter.domain.share.repository.SharePostRepository;
import io.crops.warmletter.domain.share.repository.ShareProposalRepository;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.facade.NotificationFacade;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final LetterRepository letterRepository;
    private final SharePostRepository sharePostRepository;
    private final EventCommentRepository eventCommentRepository;
    private final MemberRepository memberRepository;
    private final ShareProposalRepository shareProposalRepository;
    private final ReportModerationService reportModerationService;

    private final AuthFacade authFacde;
    private final NotificationFacade notificationFacade;

    @Transactional
    public UpdateReportResponse updateReport(Long reportId, UpdateReportRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

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
            boolean deactivated = deactivateTarget(report);
            if (deactivated) {
                reportedMember.increaseWarningCount();
                memberRepository.save(reportedMember);
            }
            resolvePendingReports(report);
            // targetMemberId로 알림 전송 TODO : 배포 후 테스트 예정
            notificationFacade.sendNotification(null, targetMemberId, AlarmType.REPORT, report.getAdminMemo()+"§"+reportedMember.getWarningCount());
        }
        return new UpdateReportResponse(report,reportedMember);
    }

    public Page<ReportsResponse> getAllReports(String reportType, String status, Pageable pageable) {
        return reportRepository.findAllWithFilters(reportType, status, pageable);
    }

    @Transactional
    public ReportResponse createReport(CreateReportRequest request) {
        Long memberId = authFacde.getCurrentUserId();
        Map<String, String> reportedContentMap = new HashMap<>();
        validateRequest(request, memberId, reportedContentMap);
        String reportedContent = reportedContentMap.get("content");
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
        CompletableFuture.runAsync(() -> {
            Map<String, String> moderationResult = reportModerationService.moderateText(reportedContent, request.getReasonType(), request.getReason());
            updateReportWithAIResult(savedReport.getId(), moderationResult);
        });
        return new ReportResponse(savedReport);
    }

    @Transactional
    public void updateReportWithAIResult(Long reportId, Map<String, String> moderationResult) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        String status = moderationResult.get("status"); // "PENDING" 또는 "RESOLVED"
        report = report.toBuilder()
                .reportStatus(ReportStatus.valueOf(status))
                .adminMemo("신고되었습니다.")
                .build();
        reportRepository.save(report);

        if ("RESOLVED".equalsIgnoreCase(status)) {
            if (deactivateTarget(report)) {
                Long targetMemberId = getTargetMemberId(report);
                Member reportedMember = memberRepository.findById(targetMemberId)
                        .orElseThrow(MemberNotFoundException::new);
                reportedMember.increaseWarningCount();
                memberRepository.save(reportedMember);
            }
            resolvePendingReports(report);
        }
    }

    private boolean deactivateTarget(Report report) {
        if (report.getLetterId() != null) {
            return deactivateLetter(report.getLetterId());
        } else if (report.getSharePostId() != null) {
            return deactivateSharePost(report.getSharePostId());
        } else if (report.getEventCommentId() != null) {
            return deactivateEventComment(report.getEventCommentId());
        }
        return false;
    }

    private boolean deactivateLetter(Long letterId) {
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(LetterNotFoundException::new);
        if (letter.isActive()) {
            letter.inactive();  // 활성 상태 -> 비활성 상태로 전환
            letterRepository.save(letter);
            return true;
        }
        return false;
    }

    private boolean deactivateSharePost(Long sharePostId) {
        SharePost sharePost = sharePostRepository.findById(sharePostId)
                .orElseThrow(SharePostNotFoundException::new);
        if (sharePost.isActive()) {
            sharePost.deactivate();
            sharePostRepository.save(sharePost);
            return true;
        }
        return false;
    }

    private boolean deactivateEventComment(Long eventCommentId) {
        EventComment eventComment = eventCommentRepository.findById(eventCommentId)
                .orElseThrow(EventCommentNotFoundException::new);
        if (eventComment.isActive()) {
            eventComment.softDelete();
            eventCommentRepository.save(eventComment);
            return true;
        }
        return false;
    }


    private Long getTargetMemberId(Report report) {
        if (report.getLetterId() != null) {
            return letterRepository.findById(report.getLetterId())
                    .orElseThrow(LetterNotFoundException::new)
                    .getWriterId();
        }
        if (report.getSharePostId() != null) {
            return shareProposalRepository.findById(report.getSharePostId())
                    .orElseThrow(ShareProposalNotFoundException::new)
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



    void validateRequest(CreateReportRequest request, Long memberId, Map<String, String> reportedContentMap) {
        // 공통: 신고 대상 ID 중 하나만 있어야 함
        boolean isLetter = request.getLetterId() != null;
        boolean isSharePost = request.getSharePostId() != null;
        boolean isEventComment = request.getEventCommentId() != null;
        int count = (isLetter ? 1 : 0) + (isSharePost ? 1 : 0) + (isEventComment ? 1 : 0);
        if(count != 1) {
            throw new InvalidReportRequestException();
        }
        checkDuplicateReport(request, memberId);
        fetchReportedContent(request, reportedContentMap);
    }

    void fetchReportedContent(CreateReportRequest request, Map<String, String> reportedContentMap) {
        switch (request.getReportType()) {
            case LETTER:
                Letter letter = letterRepository.findById(request.getLetterId())
                        .orElseThrow(LetterNotFoundException::new);
                reportedContentMap.put("content", "제목: " + letter.getTitle() + " 내용: " + letter.getContent());
                break;
            case SHARE_POST:
                SharePost sharePost = sharePostRepository.findById(request.getSharePostId())
                        .orElseThrow(SharePostNotFoundException::new);
                reportedContentMap.put("content", "내용: " + sharePost.getContent());
                break;
            case EVENT_COMMENT:
                EventComment eventComment = eventCommentRepository.findById(request.getEventCommentId())
                        .orElseThrow(EventCommentNotFoundException::new);
                reportedContentMap.put("content", "내용: " + eventComment.getContent());
                break;
        }
    }

    void checkDuplicateReport(CreateReportRequest request, Long memberId) {
        switch (request.getReportType()) {
            case LETTER:
                if (reportRepository.existsByLetterIdAndMemberId(request.getLetterId(), memberId)) {
                    throw new DuplicateReportException();
                }
                break;
            case SHARE_POST:
                if (reportRepository.existsBySharePostIdAndMemberId(request.getSharePostId(), memberId)) {
                    throw new DuplicateReportException();
                }
                break;
            case EVENT_COMMENT:
                if (reportRepository.existsByEventCommentIdAndMemberId(request.getEventCommentId(), memberId)) {
                    throw new DuplicateReportException();
                }
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
            throw new SharePostNotFoundException();
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
