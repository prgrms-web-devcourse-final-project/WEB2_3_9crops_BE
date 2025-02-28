package io.crops.warmletter.domain.report.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.eventpost.entity.EventComment;
import io.crops.warmletter.domain.eventpost.exception.EventCommentNotFoundException;
import io.crops.warmletter.domain.eventpost.repository.EventCommentRepository;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.*;
import io.crops.warmletter.domain.letter.exception.LetterNotFoundException;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import io.crops.warmletter.domain.report.dto.request.CreateReportRequest;
import io.crops.warmletter.domain.report.dto.request.UpdateReportRequest;
import io.crops.warmletter.domain.report.dto.response.ReportResponse;
import io.crops.warmletter.domain.report.dto.response.ReportsResponse;
import io.crops.warmletter.domain.report.dto.response.UpdateReportResponse;
import io.crops.warmletter.domain.report.entity.Report;
import io.crops.warmletter.domain.report.enums.ReasonType;
import io.crops.warmletter.domain.report.enums.ReportStatus;
import io.crops.warmletter.domain.report.enums.ReportType;
import io.crops.warmletter.domain.report.exception.DuplicateReportException;
import io.crops.warmletter.domain.report.exception.InvalidReportRequestException;
import io.crops.warmletter.domain.report.exception.ReportNotFoundException;
import io.crops.warmletter.domain.report.repository.ReportRepository;
import io.crops.warmletter.domain.share.entity.SharePost;
import io.crops.warmletter.domain.share.repository.SharePostRepository;
import io.crops.warmletter.domain.timeline.facade.NotificationFacade;
import io.crops.warmletter.global.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    @Mock private NotificationFacade notificationFacade;
    @Mock private MemberRepository memberRepository;
    private Report report;
    private Member reportedMember;
    @InjectMocks private ReportService reportService;


    @BeforeEach
    void setUp() {
        reportedMember = Member.builder()
                .socialUniqueId("test123")
                .email("test@email.com")
                .zipCode("12345")
                .password("password")
                .preferredLetterCategory(null)
                .role(Role.USER)
                .lastMatchedAt(LocalDateTime.now())
                .build();

        report = Report.builder()
                .id(1L)
                .reportStatus(ReportStatus.PENDING)
                .adminMemo(null)
                .letterId(10L)
                .build();
    }


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
                        "협박 신고", "RESOLVED", LocalDateTime.now(),LocalDateTime.now(), null, 30L, null, null),
                new ReportsResponse(2L, 102L, "ReporterB", 202L, "TargetB",
                        ReportType.EVENT_COMMENT.name(), ReasonType.DEFAMATION.name(),
                        "비방 신고", "PENDING", LocalDateTime.now(), LocalDateTime.now(),null, null, 40L, null)
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

        Page<ReportsResponse> mockPage = new PageImpl<>(Arrays.asList(
                new ReportsResponse(
                        1L, 100L, "Reporter1", 200L, "Target1",
                        ReportType.LETTER.name(), ReasonType.ABUSE.name(),
                        "욕설 포함", "PENDING", LocalDateTime.now(), LocalDateTime.now(),
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


    @Test
    @DisplayName("신고 처리 - RESOLVED 시 Warning Count 증가 및 비활성화")
    void updateReport_Resolved_IncreasesWarningCount() {
        // Given
        UpdateReportRequest request = new UpdateReportRequest(ReportStatus.RESOLVED, "욕설 확인되어 경고 조치함.");
        given(reportRepository.findById(1L)).willReturn(Optional.of(report));
        given(memberRepository.findById(any())).willReturn(Optional.of(reportedMember));
        given(letterRepository.findById(any())).willReturn(Optional.of(
                Letter.builder()
                        .writerId(10L)
                        .receiverId(20L)
                        .parentLetterId(null) // 부모 편지 없음
                        .letterType(LetterType.RANDOM)
                        .category(Category.ETC)
                        .title("testTitle")
                        .content("testContent")
                        .status(Status.SAVED)
                        .fontType(FontType.DEFAULT)
                        .paperType(PaperType.PAPER)
                        .build()
        ));

        // When
        UpdateReportResponse response = reportService.updateReport(1L, request);

        // Then
        assertThat(response.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(reportedMember.getWarningCount()).isEqualTo(1); // increaseWarningCount 호출됨
        verify(notificationFacade, times(1)).sendNotification(any(), any(), any(), any());
        verify(reportRepository, times(1)).save(any(Report.class));
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("신고 처리 - PENDING 신고들이 자동 RESOLVED 되는지 확인")
    void resolvePendingReports_UpdatesStatus() {
        // Given
        Report anotherReport = Report.builder()
                .id(2L)
                .reportStatus(ReportStatus.PENDING)
                .letterId(10L)
                .build();

        List<Report> pendingReports = List.of(anotherReport);
        given(reportRepository.findBySameTargetAndStatus(eq(10L), eq(null), eq(null), eq(ReportStatus.PENDING)))
                .willReturn(pendingReports);

        // When
        reportService.resolvePendingReports(report);

        // Then
        assertThat(anotherReport.getReportStatus()).isEqualTo(ReportStatus.RESOLVED);
        verify(reportRepository, times(1)).flush();
    }

    @Test
    @DisplayName("updateReport - 존재하지 않는 신고 ID로 요청 시 예외 발생")
    void updateReport_ReportNotFound_ThrowsException() {
        // Given
        Long reportId = 999L; // 존재하지 않는 ID
        UpdateReportRequest request = new UpdateReportRequest(ReportStatus.RESOLVED, "관리자 메모");

        // reportRepository에서 해당 ID의 Report가 없다고 응답
        given(reportRepository.findById(reportId)).willReturn(Optional.empty());

        // When & Then
        assertThrows(ReportNotFoundException.class, () -> {
            reportService.updateReport(reportId, request);
        });

        // reportRepository.findById()가 호출되었는지 확인
        verify(reportRepository, times(1)).findById(reportId);
    }


    @Test
    @DisplayName("deactivateTarget 테스트 Letter 비활성화")
    void deactivateTarget_Letter() {
        Report report = Report.builder().letterId(10L).build();
        Letter mockLetter = mock(Letter.class);

        given(letterRepository.findById(10L)).willReturn(Optional.of(mockLetter));

        reportService.deactivateTarget(report);

        verify(mockLetter, times(1)).inactive();
    }

    @Test
    @DisplayName("deactivateTarget 테스트 SharePost 비활성화")
    void deactivateTarget_SharePost() {
        Report report = Report.builder().sharePostId(20L).build();
        SharePost mockPost = mock(SharePost.class);

        given(sharePostRepository.findById(20L)).willReturn(Optional.of(mockPost));

        reportService.deactivateTarget(report);

        verify(mockPost, times(1)).deactivate();
    }

    @Test
    @DisplayName("deactivateTarget 테스트 EventComment 비활성화")
    void deactivateTarget_EventComment() {
        Report report = Report.builder().eventCommentId(30L).build();
        EventComment mockEventComment = mock(EventComment.class);

        given(eventCommentRepository.findById(30L)).willReturn(Optional.of(mockEventComment));

        reportService.deactivateTarget(report);

        verify(mockEventComment, times(1)).softDelete();
    }


    @Test
    @DisplayName("PENDING 상태 신고들을 RESOLVED로 변경")
    void resolvePendingReports_Success() {
        Report report1 = Report.builder()
                .id(1L)
                .letterId(10L)
                .reportStatus(ReportStatus.PENDING)
                .build();

        Report report2 = Report.builder()
                .id(2L)
                .letterId(10L)
                .reportStatus(ReportStatus.PENDING)
                .build();

        List<Report> pendingReports = List.of(report1, report2);

        given(reportRepository.findBySameTargetAndStatus(10L, null, null, ReportStatus.PENDING))
                .willReturn(pendingReports);
        reportService.resolvePendingReports(report1);

        //검증
        verify(reportRepository, times(1)).findBySameTargetAndStatus(10L, null, null, ReportStatus.PENDING);
        verify(reportRepository, times(1)).flush();
    }

    @Test
    @DisplayName("deactivateTarget - LetterNotFoundException 발생")
    void deactivateTarget_LetterNotFound() {
        Report report = Report.builder().letterId(999L).build(); // 존재하지 않는 ID

        given(letterRepository.findById(999L)).willReturn(Optional.empty());

        assertThrows(LetterNotFoundException.class, () -> reportService.deactivateTarget(report));
    }

    @Test
    @DisplayName("deactivateTarget - SharePostNotFoundException 발생")
    void deactivateTarget_SharePostNotFound() {
        Report report = Report.builder().sharePostId(999L).build(); // 존재하지 않는 ID

        given(sharePostRepository.findById(999L)).willReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> reportService.deactivateTarget(report));
    }

    @Test
    @DisplayName("deactivateTarget - EventCommentNotFoundException 발생")
    void deactivateTarget_EventCommentNotFound() {
        Report report = Report.builder().eventCommentId(999L).build(); // 존재하지 않는 ID

        given(eventCommentRepository.findById(999L)).willReturn(Optional.empty());

        assertThrows(EventCommentNotFoundException.class, () -> reportService.deactivateTarget(report));
    }


    @Test
    @DisplayName("validateLetterReport - 존재하지 않는 Letter 신고 → 예외 발생")
    void validateLetterReport_LetterNotFound_ThrowsException() {
        CreateReportRequest request = new CreateReportRequest(
                ReportType.LETTER, ReasonType.ABUSE, "부적절한 내용",
                1L, null, null
        );

        when(letterRepository.existsById(1L)).thenReturn(false);

        assertThrows(LetterNotFoundException.class, () -> reportService.validateLetterReport(request, 1003L));
    }

    @Test
    @DisplayName("validateLetterReport - 중복 Letter 신고 → 예외 발생")
    void validateLetterReport_DuplicateReport_ThrowsException() {
        CreateReportRequest request = new CreateReportRequest(
                ReportType.LETTER, ReasonType.ABUSE, "부적절한 내용",
                1L, null, null
        );

        when(letterRepository.existsById(1L)).thenReturn(true);
        when(reportRepository.existsByLetterIdAndMemberId(1L, 1003L)).thenReturn(true);

        assertThrows(DuplicateReportException.class, () -> reportService.validateLetterReport(request, 1003L));
    }


    @Test
    @DisplayName("validateSharePostReport - 존재하지 않는 SharePost 신고 → 예외 발생")
    void validateSharePostReport_SharePostNotFound_ThrowsException() {
        CreateReportRequest request = new CreateReportRequest(
                ReportType.SHARE_POST, ReasonType.ETC, "기타",
                null, 2L, null
        );

        when(sharePostRepository.existsById(2L)).thenReturn(false);

        assertThrows(BusinessException.class, () -> {
            reportService.validateSharePostReport(request, 1003L);
        });
    }

    @Test
    @DisplayName("validateSharePostReport - 중복 SharePost 신고 → 예외 발생")
    void validateSharePostReport_DuplicateReport_ThrowsException() {
        CreateReportRequest request = new CreateReportRequest(
                ReportType.SHARE_POST, ReasonType.ETC, "기타",
                null, 2L, null
        );

        when(sharePostRepository.existsById(2L)).thenReturn(true);
        when(reportRepository.existsBySharePostIdAndMemberId(2L, 1003L)).thenReturn(true);

        assertThrows(DuplicateReportException.class, () -> {
            reportService.validateSharePostReport(request, 1003L);
        });
    }

    @Test
    @DisplayName("validateEventCommentReport - 존재하지 않는 EventComment 신고 → 예외 발생")
    void validateEventCommentReport_EventCommentNotFound_ThrowsException() {
        CreateReportRequest request = new CreateReportRequest(
                ReportType.EVENT_COMMENT, ReasonType.ETC, "기타",
                null, null, 3L
        );

        when(eventCommentRepository.existsById(3L)).thenReturn(false);

        assertThrows(EventCommentNotFoundException.class, () -> {
            reportService.validateEventCommentReport(request, 1003L);
        });
    }

    @Test
    @DisplayName("validateEventCommentReport - 중복 EventComment 신고 → 예외 발생")
    void validateEventCommentReport_DuplicateReport_ThrowsException() {
        CreateReportRequest request = new CreateReportRequest(
                ReportType.EVENT_COMMENT, ReasonType.ETC, "기타",
                null, null, 3L
        );

        when(eventCommentRepository.existsById(3L)).thenReturn(true);
        when(reportRepository.existsByEventCommentIdAndMemberId(3L, 1003L)).thenReturn(true);

        assertThrows(DuplicateReportException.class, () -> {
            reportService.validateEventCommentReport(request, 1003L);
        });
    }

}
