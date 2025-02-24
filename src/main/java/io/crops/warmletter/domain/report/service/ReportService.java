package io.crops.warmletter.domain.report.service;


import io.crops.warmletter.domain.eventpost.exception.EventCommentNotFoundException;
import io.crops.warmletter.domain.eventpost.repository.EventCommentRepository;
import io.crops.warmletter.domain.letter.exception.LetterNotFoundException;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.report.dto.request.CreateReportRequest;
import io.crops.warmletter.domain.report.dto.response.ReportResponse;
import io.crops.warmletter.domain.report.entity.Report;
import io.crops.warmletter.domain.report.enums.ReportStatus;
import io.crops.warmletter.domain.report.enums.ReportType;
import io.crops.warmletter.domain.report.exception.DuplicateReportException;
import io.crops.warmletter.domain.report.exception.InvalidReportRequestException;
import io.crops.warmletter.domain.report.repository.ReportRepository;
import io.crops.warmletter.domain.share.repository.SharePostRepository;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final LetterRepository letterRepository;
    private final SharePostRepository sharePostRepository;
    private final EventCommentRepository eventCommentRepository;


    @Transactional
    public ReportResponse createReport(CreateReportRequest request) {
        Long memberId = 1003L;
        validateRequest(request);
        Report.ReportBuilder builder = Report.builder()
                .memberId(memberId)  // 고정 신고자 ID 사용
                .reasonType(request.getReasonType())
                .reason(request.getReason())
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

    private void validateRequest(CreateReportRequest request) {
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
                validateLetterReport(request);
                break;
            case SHARE_POST:
                validateSharePostReport(request);
                break;
            case EVENT_COMMENT:
                validateEventCommentReport(request);
                break;
        }
    }

    private void validateLetterReport(CreateReportRequest request) {
        if (!letterRepository.existsById(request.getLetterId())) {
            throw new LetterNotFoundException();
        }
        if(reportRepository.existsByLetterId(request.getLetterId())) {
            throw new DuplicateReportException();
        }
    }

    private void validateSharePostReport(CreateReportRequest request) {
        if(!sharePostRepository.existsById(request.getSharePostId())) {
            throw new BusinessException(ErrorCode.SHARE_POST_NOT_FOUND);
        }
        // sharePostRepository.existsById() 등 추가 검증 가능
        if(reportRepository.existsBySharePostId(request.getSharePostId())) {
            throw new DuplicateReportException();
        }
    }


    private void validateEventCommentReport(CreateReportRequest request) {
        if(!eventCommentRepository.existsById(request.getEventCommentId())) {
            throw new EventCommentNotFoundException();
        }
        if(reportRepository.existsByEventCommentId(request.getEventCommentId())) {
            throw new DuplicateReportException();
        }
    }

}
