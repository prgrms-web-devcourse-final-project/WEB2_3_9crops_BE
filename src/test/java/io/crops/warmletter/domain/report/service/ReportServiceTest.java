package io.crops.warmletter.domain.report.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.eventpost.exception.EventCommentNotFoundException;
import io.crops.warmletter.domain.eventpost.repository.EventCommentRepository;
import io.crops.warmletter.domain.letter.exception.LetterNotFoundException;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.report.dto.request.CreateReportRequest;
import io.crops.warmletter.domain.report.dto.response.ReportResponse;
import io.crops.warmletter.domain.report.entity.Report;
import io.crops.warmletter.domain.report.enums.ReasonType;
import io.crops.warmletter.domain.report.enums.ReportStatus;
import io.crops.warmletter.domain.report.enums.ReportType;
import io.crops.warmletter.domain.report.exception.DuplicateReportException;
import io.crops.warmletter.domain.report.exception.InvalidReportRequestException;
import io.crops.warmletter.domain.report.repository.ReportRepository;
import io.crops.warmletter.domain.share.repository.SharePostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ReportRepository reportRepository;
    @Mock private LetterRepository letterRepository;
    @Mock private SharePostRepository sharePostRepository;
    @Mock private EventCommentRepository eventCommentRepository;
    @Mock private AuthFacade authFacade;

    @InjectMocks private ReportService reportService;


    @Test
    @DisplayName("정상적인 신고 등록 (LETTER)")
    void createReport_Success_Letter() {
        CreateReportRequest request = new CreateReportRequest(ReportType.LETTER, ReasonType.ABUSE, "부적절한 내용", 1L, null, null);
        Report report = Report.builder()
                .id(1L)
                .memberId(1003L)
                .reportType(ReportType.LETTER)
                .reasonType(ReasonType.ABUSE)
                .reason("부적절한 내용")
                .reportStatus(ReportStatus.PENDING)
                .reportStartedAt(LocalDateTime.now())
                .letterId(1L)
                .build();

        when(authFacade.getCurrentUserId()).thenReturn(1003L);
        when(letterRepository.existsById(1L)).thenReturn(true);
        when(reportRepository.existsByLetterIdAndMemberId(1L,1003L)).thenReturn(false);
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        ReportResponse response = reportService.createReport(request);

        assertNotNull(response);
        assertEquals("LETTER", response.getReportType());
        assertEquals("ABUSE", response.getReasonType());
        assertEquals("부적절한 내용", response.getReason());
    }

    @Test
    @DisplayName("정상적인 신고 등록 (SHARE_POST)")
    void createReport_Success_SharePost() {
        CreateReportRequest request = new CreateReportRequest(ReportType.SHARE_POST, ReasonType.ETC, "스팸 게시물", null, 2L, null);
        Report report = Report.builder()
                .id(2L)
                .memberId(1003L)
                .reportType(ReportType.SHARE_POST)
                .reasonType(ReasonType.ETC)
                .reason("스팸 게시물")
                .reportStatus(ReportStatus.PENDING)
                .reportStartedAt(LocalDateTime.now())
                .sharePostId(2L)
                .build();

        when(authFacade.getCurrentUserId()).thenReturn(1003L);
        when(sharePostRepository.existsById(2L)).thenReturn(true);
        when(reportRepository.existsBySharePostIdAndMemberId(2L,1003L)).thenReturn(false);
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        ReportResponse response = reportService.createReport(request);

        assertNotNull(response);
        assertEquals("SHARE_POST", response.getReportType());
        assertEquals("ETC", response.getReasonType());
    }

    @Test
    @DisplayName("정상적인 신고 등록 (EVENT_COMMENT)")
    void createReport_Success_EventComment() {
        CreateReportRequest request = new CreateReportRequest(ReportType.EVENT_COMMENT, ReasonType.HARASSMENT, "혐오 발언", null, null, 3L);
        Report report = Report.builder()
                .id(3L)
                .memberId(1003L)
                .reportType(ReportType.EVENT_COMMENT)
                .reasonType(ReasonType.HARASSMENT)
                .reason("혐오 발언")
                .reportStatus(ReportStatus.PENDING)
                .reportStartedAt(LocalDateTime.now())
                .eventCommentId(3L)
                .build();

        when(authFacade.getCurrentUserId()).thenReturn(1003L);
        when(eventCommentRepository.existsById(3L)).thenReturn(true);
        when(reportRepository.existsByEventCommentIdAndMemberId(3L,1003L)).thenReturn(false);
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        ReportResponse response = reportService.createReport(request);

        assertNotNull(response);
        assertEquals("EVENT_COMMENT", response.getReportType());
        assertEquals("HARASSMENT", response.getReasonType());
    }

    @Test
    @DisplayName("예외 - 존재하지 않는 Letter 신고")
    void createReport_LetterNotFound_ThrowsException() {
        CreateReportRequest request = new CreateReportRequest(ReportType.LETTER, ReasonType.ABUSE, "부적절한 내용", 1L, null, null);

        when(authFacade.getCurrentUserId()).thenReturn(1003L);
        when(letterRepository.existsById(1L)).thenReturn(false);

        assertThrows(LetterNotFoundException.class, () -> reportService.createReport(request));
    }

    @Test
    @DisplayName("예외 - 중복 Letter 신고")
    void createReport_DuplicateLetterReport_ThrowsException() {
        CreateReportRequest request = new CreateReportRequest(ReportType.LETTER, ReasonType.ABUSE, "부적절한 내용", 1L, null, null);

        when(authFacade.getCurrentUserId()).thenReturn(1003L);
        when(letterRepository.existsById(1L)).thenReturn(true);
        when(reportRepository.existsByLetterIdAndMemberId(1L, 1003L)).thenReturn(true);

        assertThrows(DuplicateReportException.class, () -> reportService.createReport(request));
    }

    @Test
    @DisplayName("예외 - 유효하지 않은 신고 대상")
    void createReport_InvalidReportTarget_ThrowsException() {
        CreateReportRequest request = new CreateReportRequest(ReportType.LETTER, ReasonType.ABUSE, "잘못된 신고", 1L, 2L, null);

        when(authFacade.getCurrentUserId()).thenReturn(1003L);
        assertThrows(InvalidReportRequestException.class, () -> reportService.createReport(request));
    }

    @Test
    @DisplayName("예외 - 존재하지 않는 EventComment 신고")
    void createReport_EventCommentNotFound_ThrowsException() {
        CreateReportRequest request = new CreateReportRequest(ReportType.EVENT_COMMENT, ReasonType.HARASSMENT, "혐오 발언", null, null, 3L);

        when(authFacade.getCurrentUserId()).thenReturn(1003L);
        when(eventCommentRepository.existsById(3L)).thenReturn(false);

        assertThrows(EventCommentNotFoundException.class, () -> reportService.createReport(request));
    }




}
