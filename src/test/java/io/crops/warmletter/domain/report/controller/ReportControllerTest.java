package io.crops.warmletter.domain.report.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.domain.eventpost.exception.EventCommentNotFoundException;
import io.crops.warmletter.domain.letter.exception.LetterNotFoundException;
import io.crops.warmletter.domain.report.dto.request.CreateReportRequest;
import io.crops.warmletter.domain.report.dto.request.UpdateReportRequest;
import io.crops.warmletter.domain.report.dto.response.ReportResponse;
import io.crops.warmletter.domain.report.dto.response.ReportsResponse;
import io.crops.warmletter.domain.report.dto.response.UpdateReportResponse;
import io.crops.warmletter.domain.report.entity.Report;
import io.crops.warmletter.domain.report.enums.ReasonType;
import io.crops.warmletter.domain.report.enums.ReportStatus;
import io.crops.warmletter.domain.report.enums.ReportType;
import io.crops.warmletter.domain.report.exception.InvalidReportRequestException;
import io.crops.warmletter.domain.report.service.ReportService;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;
import io.crops.warmletter.global.error.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;

@WebMvcTest(controllers = {ReportController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {


    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReportService reportService;

    @Test
    @DisplayName("신고 등록(편지) 성공 테스트")
    void createReportLetter_success() throws Exception {
        Long memberId = 1L;
        // given: CreateReportRequest 객체 생성 (LETTER 신고 예시)
        CreateReportRequest request = new CreateReportRequest(
                ReportType.LETTER,         // 신고 유형
                ReasonType.ABUSE,          // 신고 사유 유형
                "이 편지가 너무 불쾌합니다.", // 상세 신고 사유
                2001L,                   // letterId (신고 대상 편지 ID)
                null,                    // sharePostId
                null                     // eventCommentId
        );
        String jsonRequest = objectMapper.writeValueAsString(request);

        // given: 더미 Report 엔티티 생성 (빌더 사용)
        Report dummyReport = Report.builder()
                .id(1L)
                .memberId(memberId)
                .reportType(ReportType.LETTER)
                .reasonType(request.getReasonType())
                .reason(request.getReason())
                .letterId(request.getLetterId())
                .reportStatus(ReportStatus.PENDING)
                .reportStartedAt(LocalDateTime.now())
                .build();
        ReportResponse dummyResponse = new ReportResponse(dummyReport);

        // given: ReportService.createReport() Stub 처리
        when(reportService.createReport(any(CreateReportRequest.class)))
                .thenReturn(dummyResponse);

        // when & then: /api/reports POST 요청 후 결과 검증
        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(dummyResponse.getId().intValue())))
                .andExpect(jsonPath("$.message").value("신고 등록 성공"));
    }


    @Test
    @DisplayName("신고 등록(게시물) 성공 테스트")
    void createReportSharePost_success() throws Exception {
        Long memberId = 1L;
        // given: CreateReportRequest 객체 생성 (LETTER 신고 예시)
        CreateReportRequest request = new CreateReportRequest(
                ReportType.SHARE_POST,         // 신고 유형
                ReasonType.ABUSE,          // 신고 사유 유형
                "이 게시물 너무 불쾌합니다.", // 상세 신고 사유
                null,                   // letterId (신고 대상 편지 ID)
                2001L,                    // sharePostId
                null                     // eventCommentId
        );
        String jsonRequest = objectMapper.writeValueAsString(request);

        // given: 더미 Report 엔티티 생성 (빌더 사용)
        Report dummyReport = Report.builder()
                .id(1L)
                .memberId(memberId)
                .reportType(ReportType.SHARE_POST)
                .reasonType(request.getReasonType())
                .reason(request.getReason())
                .sharePostId(request.getSharePostId())
                .reportStatus(ReportStatus.PENDING)
                .reportStartedAt(LocalDateTime.now())
                .build();
        ReportResponse dummyResponse = new ReportResponse(dummyReport);

        // given: ReportService.createReport() Stub 처리
        when(reportService.createReport(any(CreateReportRequest.class)))
                .thenReturn(dummyResponse);

        // when & then: /api/reports POST 요청 후 결과 검증
        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(dummyResponse.getId().intValue())))
                .andExpect(jsonPath("$.message").value("신고 등록 성공"));
    }


    @Test
    @DisplayName("신고 등록(댓글) 성공 테스트")
    void createReportEventComment_success() throws Exception {
        Long memberId = 1L;
        // given: CreateReportRequest 객체 생성 (LETTER 신고 예시)
        CreateReportRequest request = new CreateReportRequest(
                ReportType.EVENT_COMMENT,         // 신고 유형
                ReasonType.ABUSE,          // 신고 사유 유형
                "이 게시물 너무 불쾌합니다.", // 상세 신고 사유
                null,                   // letterId (신고 대상 편지 ID)
                null,                    // sharePostId
                2001L                     // eventCommentId
        );
        String jsonRequest = objectMapper.writeValueAsString(request);

        // given: 더미 Report 엔티티 생성 (빌더 사용)
        Report dummyReport = Report.builder()
                .id(1L)
                .memberId(memberId)
                .reportType(ReportType.EVENT_COMMENT)
                .reasonType(request.getReasonType())
                .reason(request.getReason())
                .eventCommentId(request.getEventCommentId())
                .reportStatus(ReportStatus.PENDING)
                .reportStartedAt(LocalDateTime.now())
                .build();
        ReportResponse dummyResponse = new ReportResponse(dummyReport);

        // given: ReportService.createReport() Stub 처리
        when(reportService.createReport(any(CreateReportRequest.class)))
                .thenReturn(dummyResponse);

        // when & then: /api/reports POST 요청 후 결과 검증
        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(dummyResponse.getId().intValue())))
                .andExpect(jsonPath("$.message").value("신고 등록 성공"));
    }



    @Test
    @DisplayName("신고 대상 ID 누락 시 InvalidReportRequestException 발생")
    void createReport_invalidTarget() throws Exception {
        // 신고 타입이 LETTER인데, 아무런 신고 대상 ID도 제공하지 않음
        CreateReportRequest request = new CreateReportRequest(
                ReportType.LETTER,
                ReasonType.DEFAMATION,
                "이 편지가 너무 불쾌합니다.",
                null,  // letterId 누락
                null,
                null
        );
        String jsonRequest = objectMapper.writeValueAsString(request);

        // 서비스가 예외 발생하도록 Stub 처리
        when(reportService.createReport(any(CreateReportRequest.class)))
                .thenThrow(new InvalidReportRequestException());

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REPORT-001")) // ErrorCode.INVALID_REPORT_TARGET의 코드
                .andExpect(jsonPath("$.message").value("신고 요청이 잘못되었습니다."));
    }


    @Test
    @DisplayName("편지 존재하지 않을 경우 LetterNotFoundException 발생")
    void createReport_letterNotFound() throws Exception {
        CreateReportRequest request = new CreateReportRequest(
                ReportType.LETTER,
                ReasonType.ABUSE,
                "이 편지가 너무 불쾌합니다.",
                9999L,  // 존재하지 않는 letterId
                null,
                null
        );
        String jsonRequest = objectMapper.writeValueAsString(request);

        // 서비스가 편지를 찾지 못하는 경우 예외 발생하도록 Stub 처리
        when(reportService.createReport(any(CreateReportRequest.class)))
                .thenThrow(new LetterNotFoundException());

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("LET-001"))  // 예시로, LetterNotFoundException의 코드
                .andExpect(jsonPath("$.message").value("해당 편지를 찾을 수 없습니다."));
    }


    @Test
    @DisplayName("공유게시판이 등록되지 않을 경우 SharePostNotFoundException 발생")
    void createReport_SharePostNotFound() throws Exception {
        CreateReportRequest request = new CreateReportRequest(
                ReportType.LETTER,
                ReasonType.ABUSE,
                "이 게시글 너무 불쾌합니다.",
                null,    // letterId는 null
                9999L,   // sharePostId가 존재하지 않는 값
                null
        );
        String jsonRequest = objectMapper.writeValueAsString(request);

        // 서비스가 공유 게시글을 찾지 못하는 경우 예외 발생하도록 Stub 처리
        when(reportService.createReport(any(CreateReportRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.SHARE_POST_NOT_FOUND));

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SHARE-002"))
                .andExpect(jsonPath("$.message").value("해당 공유 게시글을 찾을 수 없습니다."));
    }



    @Test
    @DisplayName("이벤트게시판 댓글 등록되지 않을 경우 SharePostNotFoundException 발생")
    void createReport_EventCommentNotFound() throws Exception {
        CreateReportRequest request = new CreateReportRequest(
                ReportType.LETTER,
                ReasonType.ABUSE,
                "이 댓글 너무 불쾌합니다.",
                null,    // letterId는 null
                null,   // sharePostId가 존재하지 않는 값
                9999L
        );
        String jsonRequest = objectMapper.writeValueAsString(request);

        // 서비스가 공유 게시글을 찾지 못하는 경우 예외 발생하도록 Stub 처리
        when(reportService.createReport(any(CreateReportRequest.class)))
                .thenThrow(new EventCommentNotFoundException());

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT-003"))
                .andExpect(jsonPath("$.message").value("해당 이벤트 게시글의 댓글을 찾을 수 없습니다."));
    }


    @Test
    @DisplayName("신고 목록 조회 - 정상 요청")
    void getAllReports_Success() throws Exception {
        // Given (Mock 데이터 준비)
        Page<ReportsResponse> mockPage = new PageImpl<>(List.of(
                new ReportsResponse(
                        1L,                           // id
                        100L,                         // reporterId
                        "Reporter Name",              // reporterEmail
                        200L,                         // targetId
                        "Target User",                // targetEmail
                        ReportType.SHARE_POST.name(), // ✅ reportType (SHARE_POST)
                        ReasonType.ABUSE.name(),      // ✅ reasonType (ABUSE)
                        "욕설 포함",                   // reason
                        "PENDING",                    // status
                        LocalDateTime.now(),          // reportedAt
                        LocalDateTime.now(),
                        10L,                          // letterId
                        null,                         // sharePostId (게시글 신고가 아니므로 null)
                        null,                         // eventCommentId
                        null                          // contentDetail
                ),
                new ReportsResponse(
                        2L,
                        101L,
                        "Another Reporter",
                        201L,
                        "Target User 2",
                        ReportType.LETTER.name(),
                        ReasonType.THREATS.name(),
                        "협박 관련 신고",
                        "RESOLVED",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        null,
                        null,
                        null,
                        null
                )
        ));

        given(reportService.getAllReports(any(), any(), any())).willReturn(mockPage);

        // When & Then (API 요청 및 검증)
        mockMvc.perform(get("/api/reports")
                        .param("reportType", ReportType.SHARE_POST.name()) // "SHARE_POST"
                        .param("status", "PENDING")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "id,desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // HTTP 200 응답
                .andExpect(jsonPath("$.message").value("신고 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].reportType").value("SHARE_POST")) // ✅ 올바른 값으로 수정
                .andExpect(jsonPath("$.data.content[0].reasonType").value("ABUSE"))      // ✅ 올바른 값으로 수정
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"));
    }


    @Test
    @DisplayName("신고 목록 조회 - 빈 목록")
    void getAllReports_EmptyList() throws Exception {
        // Given (빈 데이터)
        Page<ReportsResponse> emptyPage = new PageImpl<>(Collections.emptyList());
        given(reportService.getAllReports(any(), any(), any())).willReturn(emptyPage);

        // When & Then (API 요청 및 검증)
        mockMvc.perform(get("/api/reports")
                        .param("reportType", ReportType.LETTER.name()) // "LETTER"
                        .param("status", "PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("신고 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").isEmpty()); // 신고 목록이 비어 있어야 함
    }

    @Test
    @DisplayName("신고 처리 - 성공")
    void updateReport_Success() throws Exception {
        // Given (입력 데이터 준비)
        Long reportId = 1L;
        int warningCount = 2;
        UpdateReportRequest request = new UpdateReportRequest(ReportStatus.RESOLVED, "욕설 확인되어 경고 조치함.");

        UpdateReportResponse response = new UpdateReportResponse(reportId, ReportStatus.RESOLVED, "욕설 확인되어 경고 조치함.", warningCount);

        // reportService.updateReport()가 호출되면 response 반환하도록 Mock 설정
        given(reportService.updateReport(eq(reportId), any(UpdateReportRequest.class)))
                .willReturn(response);

        // When & Then (MockMvc 요청 실행 & 검증)
        mockMvc.perform(patch("/api/reports/{reportId}", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))  // JSON 직렬화
                .andExpect(status().isOk()) // HTTP 200 확인
                .andExpect(jsonPath("$.data.id").value(reportId)) // 응답 검증
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.adminMemo").value("욕설 확인되어 경고 조치함."))
                .andExpect(jsonPath("$.message").value("신고 처리 완료")); // 메시지 검증

        // 서비스 호출 여부 확인
        Mockito.verify(reportService, Mockito.times(1)).updateReport(eq(reportId), any(UpdateReportRequest.class));
    }


}