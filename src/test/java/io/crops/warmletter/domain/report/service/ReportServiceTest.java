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
import io.crops.warmletter.domain.share.exception.SharePostNotFoundException;
import io.crops.warmletter.domain.share.repository.SharePostRepository;
import io.crops.warmletter.domain.timeline.dto.request.NotificationRequest;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.facade.NotificationFacade;
import io.crops.warmletter.global.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;


import java.time.LocalDateTime;
import java.util.*;

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
    @Mock private ApplicationEventPublisher notificationPublisher;
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
        // Given: 신고 요청 생성
        CreateReportRequest request = new CreateReportRequest(
                ReportType.LETTER,
                ReasonType.ABUSE,
                "부적절한 내용",
                1L,  // letterId
                null,
                null
        );

        // Letter 객체 생성 (fetchReportedContent 메서드에서 사용)
        Letter letter = Letter.builder()
                .writerId(1003L)
                .receiverId(null)
                .parentLetterId(null)
                .letterType(null)
                .category(null)
                .title("테스트 제목")
                .content("테스트 내용")
                .status(null)
                .fontType(null)
                .paperType(null)
                .matchingId(null)
                .build();
        // 빌드 후 Reflection을 사용해 id 값을 설정
        ReflectionTestUtils.setField(letter, "id", 1L);

        // Report 객체 생성 (reportRepository.save 반환값)
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

        // Stubbing
        when(authFacade.getCurrentUserId()).thenReturn(1003L);
        // 불필요한 existsById stubbing은 제거하거나 lenient()로 처리:
        // lenient().when(letterRepository.existsById(1L)).thenReturn(true);
        when(letterRepository.findById(1L)).thenReturn(Optional.of(letter));
        when(reportRepository.existsByLetterIdAndMemberId(1L, 1003L)).thenReturn(false);
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        // When: 신고 생성
        ReportResponse response = reportService.createReport(request);

        // Then: 결과 검증
        assertNotNull(response);
        assertEquals("LETTER", response.getReportType());
        assertEquals("ABUSE", response.getReasonType());
        assertEquals("부적절한 내용", response.getReason());

        // 추가 검증 (예: repository 호출 횟수 등)
        verify(authFacade, times(1)).getCurrentUserId();
        // verify(letterRepository, times(1)).existsById(1L);  // 필요없으면 제거
        verify(letterRepository, times(1)).findById(1L);
        verify(reportRepository, times(1)).existsByLetterIdAndMemberId(1L, 1003L);
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    @DisplayName("정상적인 신고 등록 (EVENT_COMMENT)")
    void createReport_Success_EventComment() {
        // Given: 신고 요청 생성
        CreateReportRequest request = new CreateReportRequest(
                ReportType.EVENT_COMMENT,
                ReasonType.HARASSMENT,
                "혐오 발언",
                null,
                null,
                3L    // eventCommentId
        );

        // Report 객체 생성 (reportRepository.save 반환값)
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

        // EventComment 객체를 모킹하여 fetchReportedContent에서 사용될 데이터를 제공
        EventComment mockEventComment = mock(EventComment.class);
        when(mockEventComment.getContent()).thenReturn("테스트 댓글 내용");

        // Stubbing
        when(authFacade.getCurrentUserId()).thenReturn(1003L);
        // fetchReportedContent 내부에서 eventCommentRepository.findById(3L)를 호출합니다.
        when(eventCommentRepository.findById(3L)).thenReturn(Optional.of(mockEventComment));
        when(reportRepository.existsByEventCommentIdAndMemberId(3L, 1003L)).thenReturn(false);
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        // When: 신고 생성
        ReportResponse response = reportService.createReport(request);

        // Then: 결과 검증
        assertNotNull(response);
        assertEquals("EVENT_COMMENT", response.getReportType());
        assertEquals("HARASSMENT", response.getReasonType());

        // 추가 검증: 각 의존성 호출 횟수 확인
        verify(authFacade, times(1)).getCurrentUserId();
        verify(eventCommentRepository, times(1)).findById(3L);
        verify(reportRepository, times(1)).existsByEventCommentIdAndMemberId(3L, 1003L);
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    @DisplayName("예외 - 존재하지 않는 Letter 신고")
    void createReport_LetterNotFound_ThrowsException() {
        CreateReportRequest request = new CreateReportRequest(ReportType.LETTER, ReasonType.ABUSE, "부적절한 내용", 1L, null, null);

        when(authFacade.getCurrentUserId()).thenReturn(1003L);
        // letterRepository.existsById 대신 findById가 호출되므로 findById를 빈 Optional로 설정
        when(letterRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(LetterNotFoundException.class, () -> reportService.createReport(request));
    }

    @Test
    @DisplayName("정상적인 신고 등록 (SHARE_POST)")
    void createReport_Success_SharePost() {
        // Given: 신고 요청 생성 (SHARE_POST 타입)
        CreateReportRequest request = new CreateReportRequest(
                ReportType.SHARE_POST,
                ReasonType.ETC,
                "신고 내용",
                null,   // letterId 없음
                2L,     // sharePostId
                null    // eventCommentId 없음
        );

        SharePost sharePost = mock(SharePost.class);
        when(sharePost.getContent()).thenReturn("공유 게시글 내용");

        // Report 객체 생성 (reportRepository.save 반환값)
        Report report = Report.builder()
                .id(1L)
                .memberId(1003L)
                .reportType(ReportType.SHARE_POST)
                .reasonType(ReasonType.ETC)
                .reason("신고 내용")
                .reportStatus(ReportStatus.PENDING)
                .reportStartedAt(LocalDateTime.now())
                .sharePostId(2L)
                .build();

        // Stubbing
        when(authFacade.getCurrentUserId()).thenReturn(1003L);
        when(sharePostRepository.findById(2L)).thenReturn(Optional.of(sharePost));
        when(reportRepository.existsBySharePostIdAndMemberId(2L, 1003L)).thenReturn(false);
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        // When: 신고 생성
        ReportResponse response = reportService.createReport(request);

        // Then: 결과 검증
        assertNotNull(response);
        assertEquals("SHARE_POST", response.getReportType());
        assertEquals("ETC", response.getReasonType());
        assertEquals("신고 내용", response.getReason());

        // 각 의존성 호출 횟수 검증
        verify(authFacade, times(1)).getCurrentUserId();
        verify(sharePostRepository, times(1)).findById(2L);
        verify(reportRepository, times(1)).existsBySharePostIdAndMemberId(2L, 1003L);
        verify(reportRepository, times(1)).save(any(Report.class));
    }


    @Test
    @DisplayName("예외 - 중복 Letter 신고")
    void createReport_DuplicateLetterReport_ThrowsException() {
        CreateReportRequest request = new CreateReportRequest(ReportType.LETTER, ReasonType.ABUSE, "부적절한 내용", 1L, null, null);

        when(authFacade.getCurrentUserId()).thenReturn(1003L);
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
        // existsById 대신 findById를 스텁합니다.
        when(eventCommentRepository.findById(3L)).thenReturn(Optional.empty());

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
        verify(notificationPublisher).publishEvent(any(NotificationRequest.class));
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
    @DisplayName("deactivateTarget 테스트: Letter 비활성화")
    void deactivateTarget_Letter() {
        // given
        Long letterId = 10L;
        Report report = Report.builder().letterId(letterId).build();
        Letter mockLetter = mock(Letter.class);
        given(letterRepository.findById(letterId)).willReturn(Optional.of(mockLetter));
        // 모킹: mockLetter가 활성 상태라고 가정
        when(mockLetter.isActive()).thenReturn(true);

        // when: private 메서드 deactivateLetter 호출
        Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(reportService, "deactivateLetter", letterId);

        // then
        assertTrue(result, "활성 상태인 Letter에 대해 deactivateLetter는 true를 반환해야 합니다.");
        verify(mockLetter, times(1)).inactive();
        verify(letterRepository, times(1)).save(mockLetter);
    }
    @Test
    @DisplayName("deactivateTarget 테스트 SharePost 비활성화")
    void deactivateTarget_SharePost() {
        Long sharePostId = 20L;
        Report report = Report.builder().sharePostId(sharePostId).build();
        SharePost mockPost = mock(SharePost.class);
        when(mockPost.isActive()).thenReturn(true);

        given(sharePostRepository.findById(sharePostId)).willReturn(Optional.of(mockPost));

        // private 메서드이므로 ReflectionTestUtils로 호출
        Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(reportService, "deactivateSharePost", sharePostId);
        assertTrue(result);

        verify(mockPost, times(1)).deactivate();
        verify(sharePostRepository, times(1)).save(mockPost);
    }

    @Test
    @DisplayName("deactivateTarget 테스트 EventComment 비활성화")
    void deactivateTarget_EventComment() {
        Long eventCommentId = 30L;
        Report report = Report.builder().eventCommentId(eventCommentId).build();
        EventComment mockEventComment = mock(EventComment.class);
        when(mockEventComment.isActive()).thenReturn(true);

        given(eventCommentRepository.findById(eventCommentId)).willReturn(Optional.of(mockEventComment));

        // private 메서드 호출
        Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(reportService, "deactivateEventComment", eventCommentId);
        assertTrue(result);

        verify(mockEventComment, times(1)).softDelete();
        verify(eventCommentRepository, times(1)).save(mockEventComment);
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

        assertThrows(LetterNotFoundException.class, () -> {
            ReflectionTestUtils.invokeMethod(reportService, "deactivateTarget", report);
        });
    }

    @Test
    @DisplayName("deactivateTarget - SharePostNotFoundException 발생")
    void deactivateTarget_SharePostNotFound() {
        Report report = Report.builder().sharePostId(999L).build(); // 존재하지 않는 ID

        given(sharePostRepository.findById(999L)).willReturn(Optional.empty());

        assertThrows(SharePostNotFoundException.class, () -> {
            ReflectionTestUtils.invokeMethod(reportService, "deactivateTarget", report);
        });
    }

    @Test
    @DisplayName("deactivateTarget - EventCommentNotFoundException 발생")
    void deactivateTarget_EventCommentNotFound() {
        Report report = Report.builder().eventCommentId(999L).build(); // 존재하지 않는 ID

        given(eventCommentRepository.findById(999L)).willReturn(Optional.empty());

        assertThrows(EventCommentNotFoundException.class, () -> {
            ReflectionTestUtils.invokeMethod(reportService, "deactivateTarget", report);
        });
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


    @Test
    @DisplayName("updateReportWithAIResult 테스트: moderationResult가 PENDING인 경우")
    void updateReportWithAIResult_Pending() {
        // 기존 신고 Report 생성
        Report existingReport = Report.builder()
                .id(1L)
                .letterId(10L)
                .reportStatus(ReportStatus.PENDING)
                .build();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(existingReport));

        // 모더레이션 결과가 PENDING인 경우
        Map<String, String> moderationResult = Map.of("status", "PENDING", "adminMemo", "검토중");

        reportService.updateReportWithAIResult(1L, moderationResult);

        // 신고 상태는 그대로 PENDING, deactivation 및 Warning Count 증가가 일어나지 않음
        verify(reportRepository, times(1)).save(any(Report.class));
        verify(memberRepository, never()).save(any(Member.class));
    }


    @Test
    @DisplayName("updateReportWithAIResult 테스트: moderationResult가 RESOLVED, deactivation false인 경우")
    void updateReportWithAIResult_Resolved_DeactivationFalse() {
        // 기존 신고 Report 생성 (letterId가 10L)
        Report existingReport = Report.builder()
                .id(1L)
                .letterId(10L)
                .reportStatus(ReportStatus.PENDING)
                .build();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(existingReport));

        // Letter 모킹: 이미 비활성 상태 -> deactivateLetter가 false 반환
        Letter letterMock = mock(Letter.class);
        when(letterMock.isActive()).thenReturn(false);
        when(letterRepository.findById(10L)).thenReturn(Optional.of(letterMock));
        // getTargetMemberId는 호출되지 않으므로 memberRepository 관련은 스텁하지 않음

        // moderationResult가 RESOLVED인 경우
        Map<String, String> moderationResult = Map.of("status", "RESOLVED", "adminMemo", "신고 처리됨");

        reportService.updateReportWithAIResult(1L, moderationResult);

        // 신고 상태는 업데이트되지만, deactivateTarget가 false이므로 Warning Count 증가 및 Notification 미발송
        verify(reportRepository, times(1)).save(any(Report.class));
        verify(memberRepository, never()).save(any(Member.class));
        verify(notificationPublisher, never()).publishEvent(any(NotificationRequest.class));
    }

    // 추가된 테스트 케이스들 (추가된 부분만 발췌)


    @Test
    @DisplayName("updateReportWithAIResult 테스트: 상태 PENDING일 때")
    void testUpdateReportWithAIResult_Pending() {
        // Given: 기존 Report가 존재하고, PENDING 상태인 경우
        Report existingReport = Report.builder()
                .id(1L)
                .letterId(10L)
                .reportStatus(ReportStatus.PENDING)
                .build();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(existingReport));

        // moderationResult가 PENDING인 경우
        Map<String, String> moderationResult = new HashMap<>();
        moderationResult.put("status", "PENDING");
        moderationResult.put("adminMemo", "검토 중");

        // When: updateReportWithAIResult 호출
        reportService.updateReportWithAIResult(1L, moderationResult);

        // Then: Report의 상태가 PENDING으로 업데이트되고, 알림 전송은 발생하지 않아야 함
        verify(reportRepository, atLeastOnce()).save(argThat(r ->
                r.getReportStatus() == ReportStatus.PENDING &&
                        "신고되었습니다.".equals(r.getAdminMemo())
        ));
        verify(notificationPublisher,never()).publishEvent(any(NotificationRequest.class));
    }

    @Test
    @DisplayName("updateReportWithAIResult 테스트: 상태 RESOLVED, deactivateTarget가 true인 경우")
    void testUpdateReportWithAIResult_Resolved_DeactivationTrue() {
        // Given: 기존 Report가 존재하는 경우
        Report existingReport = Report.builder()
                .id(1L)
                .letterId(10L)
                .reportStatus(ReportStatus.PENDING)
                .build();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(existingReport));

        // ReportService를 spy로 생성하여 deactivateTarget을 true로 강제 설정
        ReportService spyService = spy(reportService);

        // LetterRepository의 동작을 통해 getTargetMemberId를 위한 writerId를 1003L로 반환하도록 설정
        Letter letter = Letter.builder().writerId(1003L).build();
        ReflectionTestUtils.setField(letter, "id", 10L);
        when(letterRepository.findById(10L)).thenReturn(Optional.of(letter));

        // Member 준비: 초기 warningCount 0
        Member member = Member.builder()
                .socialUniqueId("test123")
                .email("test@email.com")
                .zipCode("12345")
                .password("password")
                .role(Role.USER)
                .lastMatchedAt(LocalDateTime.now())
                .build();
        when(memberRepository.findById(1003L)).thenReturn(Optional.of(member));

        // moderationResult가 RESOLVED인 경우
        Map<String, String> moderationResult = new HashMap<>();
        moderationResult.put("status", "RESOLVED");
        moderationResult.put("adminMemo", "처리됨");

        // When: updateReportWithAIResult 호출
        spyService.updateReportWithAIResult(1L, moderationResult);

        // Then: Report의 상태가 RESOLVED로 업데이트되고, deactivateTarget가 true이므로 WarningCount 증가 및 알림 전송 발생
        verify(reportRepository, atLeastOnce()).save(argThat(r ->
                r.getReportStatus() == ReportStatus.RESOLVED &&
                        "신고되었습니다.".equals(r.getAdminMemo())
        ));
        verify(memberRepository, times(1)).save(any(Member.class));
        assertEquals(1, member.getWarningCount(), "Warning Count가 1 증가해야 합니다.");
    }


    @Test
    @DisplayName("deactivateSharePost 테스트: 활성 SharePost -> 비활성화 및 true 반환")
    void testDeactivateSharePost_Success() {
        Long sharePostId = 20L;
        SharePost sharePost = mock(SharePost.class);
        when(sharePost.isActive()).thenReturn(true);
        given(sharePostRepository.findById(sharePostId)).willReturn(Optional.of(sharePost));
        Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(reportService, "deactivateSharePost", sharePostId);
        assertTrue(result);
        verify(sharePost, times(1)).deactivate();
        verify(sharePostRepository, times(1)).save(sharePost);
    }

    @Test
    @DisplayName("deactivateEventComment 테스트: 활성 EventComment -> 비활성화 및 true 반환")
    void testDeactivateEventComment_Success() {
        Long eventCommentId = 30L;
        EventComment eventComment = mock(EventComment.class);
        when(eventComment.isActive()).thenReturn(true);;
        given(eventCommentRepository.findById(eventCommentId)).willReturn(Optional.of(eventComment));
        Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(reportService, "deactivateEventComment", eventCommentId);
        assertTrue(result);
        verify(eventComment, times(1)).softDelete();
        verify(eventCommentRepository, times(1)).save(eventComment);
    }

    @Test
    @DisplayName("deactivateLetter 테스트: 존재하지 않는 Letter -> LetterNotFoundException")
    void testDeactivateLetter_NotFound() {
        Long letterId = 999L;
        given(letterRepository.findById(letterId)).willReturn(Optional.empty());
        assertThrows(LetterNotFoundException.class, () ->
                ReflectionTestUtils.invokeMethod(reportService, "deactivateLetter", letterId)
        );
    }

    @Test
    @DisplayName("deactivateSharePost 테스트: 존재하지 않는 SharePost -> SharePostNotFoundException")
    void testDeactivateSharePost_NotFound() {
        Long sharePostId = 999L;
        given(sharePostRepository.findById(sharePostId)).willReturn(Optional.empty());
        assertThrows(SharePostNotFoundException.class, () ->
                ReflectionTestUtils.invokeMethod(reportService, "deactivateSharePost", sharePostId)
        );
    }

    @Test
    @DisplayName("deactivateEventComment 테스트: 존재하지 않는 EventComment -> EventCommentNotFoundException")
    void testDeactivateEventComment_NotFound() {
        Long eventCommentId = 999L;
        given(eventCommentRepository.findById(eventCommentId)).willReturn(Optional.empty());
        assertThrows(EventCommentNotFoundException.class, () ->
                ReflectionTestUtils.invokeMethod(reportService, "deactivateEventComment", eventCommentId)
        );
    }

    @Test
    @DisplayName("validateRequest 테스트: 신고 대상 ID 개수가 1개가 아니면 예외 발생")
    void testValidateRequest_InvalidCount() {
        CreateReportRequest request = new CreateReportRequest(
                ReportType.LETTER, ReasonType.ABUSE, "내용", 1L, 2L, 3L
        );
        assertThrows(InvalidReportRequestException.class, () ->
                reportService.validateRequest(request, 1003L, new HashMap<>())
        );
    }

    @Test
    @DisplayName("fetchReportedContent 테스트: Letter 분기")
    void testFetchReportedContent_Letter() {
        CreateReportRequest request = new CreateReportRequest(
                ReportType.LETTER, ReasonType.ABUSE, "내용", 1L, null, null
        );
        Letter letter = Letter.builder()
                .writerId(555L)
                .title("제목")
                .content("내용")
                .build();
        ReflectionTestUtils.setField(letter, "id", 1L);
        given(letterRepository.findById(1L)).willReturn(Optional.of(letter));

        Map<String, String> contentMap = new HashMap<>();
        reportService.fetchReportedContent(request, contentMap);
        assertTrue(contentMap.get("content").contains("제목"));
        assertTrue(contentMap.get("content").contains("내용"));
    }

    @Test
    @DisplayName("fetchReportedContent 테스트: SharePost 분기")
    void testFetchReportedContent_SharePost() {
        CreateReportRequest request = new CreateReportRequest(
                ReportType.SHARE_POST, ReasonType.ETC, "내용", null, 2L, null
        );
        SharePost sharePost = mock(SharePost.class);
        when(sharePost.getContent()).thenReturn("공유 게시글 내용");
        given(sharePostRepository.findById(2L)).willReturn(Optional.of(sharePost));

        Map<String, String> contentMap = new HashMap<>();
        reportService.fetchReportedContent(request, contentMap);
        assertTrue(contentMap.get("content").contains("공유 게시글 내용"));
    }

    @Test
    @DisplayName("fetchReportedContent 테스트: EventComment 분기")
    void testFetchReportedContent_EventComment() {
        CreateReportRequest request = new CreateReportRequest(
                ReportType.EVENT_COMMENT, ReasonType.ETC, "내용", null, null, 3L
        );
        EventComment eventComment = mock(EventComment.class);
        when(eventComment.getContent()).thenReturn("댓글 내용");
        given(eventCommentRepository.findById(3L)).willReturn(Optional.of(eventComment));

        Map<String, String> contentMap = new HashMap<>();
        reportService.fetchReportedContent(request, contentMap);
        assertTrue(contentMap.get("content").contains("댓글 내용"));
    }

    @Test
    @DisplayName("checkDuplicateReport 테스트: 중복 Letter 신고 예외")
    void testCheckDuplicateReport_DuplicateLetter() {
        CreateReportRequest request = new CreateReportRequest(
                ReportType.LETTER, ReasonType.ABUSE, "내용", 1L, null, null
        );
        when(reportRepository.existsByLetterIdAndMemberId(1L, 1003L)).thenReturn(true);
        assertThrows(DuplicateReportException.class, () ->
                reportService.checkDuplicateReport(request, 1003L)
        );
    }

    @Test
    @DisplayName("checkDuplicateReport 테스트: 중복 SharePost 신고 예외")
    void testCheckDuplicateReport_DuplicateSharePost() {
        CreateReportRequest request = new CreateReportRequest(
                ReportType.SHARE_POST, ReasonType.ETC, "내용", null, 2L, null
        );
        when(reportRepository.existsBySharePostIdAndMemberId(2L, 1003L)).thenReturn(true);
        assertThrows(DuplicateReportException.class, () ->
                reportService.checkDuplicateReport(request, 1003L)
        );
    }

    @Test
    @DisplayName("checkDuplicateReport 테스트: 중복 EventComment 신고 예외")
    void testCheckDuplicateReport_DuplicateEventComment() {
        CreateReportRequest request = new CreateReportRequest(
                ReportType.EVENT_COMMENT, ReasonType.ETC, "내용", null, null, 3L
        );
        when(reportRepository.existsByEventCommentIdAndMemberId(3L, 1003L)).thenReturn(true);
        assertThrows(DuplicateReportException.class, () ->
                reportService.checkDuplicateReport(request, 1003L)
        );
    }

    @Test
    @DisplayName("updateReportWithAIResult 테스트: 상태 RESOLVED, deactivateTarget가 false인 경우")
    void updateReportWithAIResult_Resolved_NoDeactivation() {
        // Given: 기존 Report 생성 (letterId가 10L)
        Report existingReport = Report.builder()
                .id(1L)
                .letterId(10L)
                .reportStatus(ReportStatus.PENDING)
                .build();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(existingReport));

        // 모킹: Letter 객체 생성 및 isActive()가 false를 반환하도록 설정
        Letter letterMock = mock(Letter.class);
        // deactivateLetter()에서는 letter.isActive()만 사용하므로 getWriterId() 스터빙은 필요하지 않음
        lenient().when(letterMock.isActive()).thenReturn(false);
        when(letterRepository.findById(10L)).thenReturn(Optional.of(letterMock));

        // Warning Count 증가를 위해 Member 준비 (초기 warningCount 0)
        Member member = Member.builder()
                .socialUniqueId("test123")
                .email("test@email.com")
                .zipCode("12345")
                .password("password")
                .role(Role.USER)
                .lastMatchedAt(LocalDateTime.now())
                .build();
        // 해당 stubbing은 deactivateTarget 분기에서는 호출되지 않으므로 lenient 처리
        lenient().when(memberRepository.findById(1003L)).thenReturn(Optional.of(member));

        // moderationResult가 RESOLVED인 경우 설정
        Map<String, String> moderationResult = new HashMap<>();
        moderationResult.put("status", "RESOLVED");
        moderationResult.put("adminMemo", "처리됨");

        // When: updateReportWithAIResult 호출
        reportService.updateReportWithAIResult(1L, moderationResult);

        // Then: 신고 상태가 RESOLVED로 업데이트되지만, deactivateTarget가 false이므로 WarningCount 증가 및 알림 전송은 발생하지 않음
        verify(reportRepository, atLeastOnce()).save(argThat(r ->
                r.getReportStatus() == ReportStatus.RESOLVED &&
                        "신고되었습니다.".equals(r.getAdminMemo())
        ));
        verify(memberRepository, never()).save(any(Member.class));
        // notificationFacade.sendNotification는 호출되지 않아야 하므로 별도 검증은 생략
        assertEquals(0, member.getWarningCount(), "Warning Count는 증가하지 않아야 합니다.");
    }

    @Test
    @DisplayName("getTargetMemberId 테스트: 신고 대상이 없는 경우 InvalidReportRequestException 발생")
    void testGetTargetMemberId_Invalid() {
        // Report에 아무 대상도 설정하지 않음
        Report report = Report.builder().build();
        assertThrows(InvalidReportRequestException.class, () ->
                ReflectionTestUtils.invokeMethod(reportService, "getTargetMemberId", report)
        );
    }

    @Test
    @DisplayName("deactivateTarget 테스트: 신고 대상이 없는 경우 false 반환")
    void testDeactivateTarget_NoTarget() {
        // Report에 letterId, sharePostId, eventCommentId 모두 null인 경우
        Report report = Report.builder().build();
        boolean result = ReflectionTestUtils.invokeMethod(reportService, "deactivateTarget", report);
        assertFalse(result, "신고 대상이 없는 경우 deactivateTarget은 false를 반환해야 합니다.");
    }

}
