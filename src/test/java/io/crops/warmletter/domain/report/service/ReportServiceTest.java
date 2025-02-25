package io.crops.warmletter.domain.report.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.eventpost.exception.EventCommentNotFoundException;
import io.crops.warmletter.domain.eventpost.repository.EventCommentRepository;
import io.crops.warmletter.domain.letter.exception.LetterNotFoundException;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.report.dto.request.CreateReportRequest;
import io.crops.warmletter.domain.report.dto.response.ReportResponse;
import io.crops.warmletter.domain.report.dto.response.ReportsResponse;
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
import org.springframework.data.domain.*;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;


import java.time.LocalDateTime;
import java.util.List;

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

    @Test
    @DisplayName("신고 목록 조회 - 모든 신고 조회")
    void getAllReports_AllReports_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ReportsResponse> mockPage = new PageImpl<>(List.of(
                new ReportsResponse(1L, 101L, "ReporterA", 201L, "TargetA",
                        ReportType.SHARE_POST.name(), ReasonType.THREATS.name(),
                        "협박 신고", "RESOLVED", LocalDateTime.now(), null, 30L, null, null),
                new ReportsResponse(2L, 102L, "ReporterB", 202L, "TargetB",
                        ReportType.EVENT_COMMENT.name(), ReasonType.DEFAMATION.name(),
                        "비방 신고", "PENDING", LocalDateTime.now(), null, null, 40L, null)
        ));

        given(reportRepository.findAllWithFilters(isNull(), isNull(), any(Pageable.class)))
                .willReturn(mockPage);

        // When
        Page<ReportsResponse> result = reportService.getAllReports(null, null, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(reportRepository, times(1)).findAllWithFilters(isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("신고 목록 조회 - reportType=LETTER, status=PENDING")
    void getAllReports_LetterPending_Success() {
        // Given (Mock 데이터 준비)
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());

        Page<ReportsResponse> mockPage = new PageImpl<>(List.of(
                new ReportsResponse(
                        1L, 100L, "Reporter1", 200L, "Target1",
                        ReportType.LETTER.name(), ReasonType.ABUSE.name(),
                        "욕설 포함", "PENDING", LocalDateTime.now(),
                        10L, null, null, null
                )
        ));

        given(reportRepository.findAllWithFilters(eq("LETTER"), eq("PENDING"), any(Pageable.class)))
                .willReturn(mockPage);

        // When (서비스 메서드 호출)
        Page<ReportsResponse> result = reportService.getAllReports("LETTER", "PENDING", pageable);

        // Then (검증)
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getReportType()).isEqualTo("LETTER");
        assertThat(result.getContent().get(0).getStatus()).isEqualTo("PENDING");

        // Mock 검증
        verify(reportRepository, times(1)).findAllWithFilters(eq("LETTER"), eq("PENDING"), any(Pageable.class));
    }

}
