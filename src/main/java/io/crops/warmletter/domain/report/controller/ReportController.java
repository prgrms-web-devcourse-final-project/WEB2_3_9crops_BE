package io.crops.warmletter.domain.report.controller;

import io.crops.warmletter.domain.report.dto.request.CreateReportRequest;
import io.crops.warmletter.domain.report.dto.request.UpdateReportRequest;
import io.crops.warmletter.domain.report.dto.response.ReportResponse;
import io.crops.warmletter.domain.report.dto.response.ReportsResponse;
import io.crops.warmletter.domain.report.dto.response.UpdateReportResponse;
import io.crops.warmletter.domain.report.enums.ReportStatus;
import io.crops.warmletter.domain.report.enums.ReportType;
import io.crops.warmletter.domain.report.service.ReportService;
import io.crops.warmletter.global.response.BaseResponse;
import io.crops.warmletter.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Report", description = "신고 관련 API")
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;


    @PostMapping
    @Operation(summary = "신고 등록", description = "사용자는 reportType에 맞는 ID(예: letterId, sharePostId, eventCommentId) 중 하나만 입력해야 합니다.")
    public ResponseEntity<BaseResponse<ReportResponse>> createReport(@RequestBody CreateReportRequest request) {
        ReportResponse response = reportService.createReport(request);
        return ResponseEntity.ok(BaseResponse.of(response, "신고 등록 성공"));
    }

    @GetMapping
    @Operation(summary = "신고 목록 조회", description = "신고 목록 조회하는 API입니다.")
    public ResponseEntity<BaseResponse<PageResponse<ReportsResponse>>> getAllReports(
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Pageable adjustedPageable = PageRequest.of(
                pageable.getPageNumber() > 0 ? pageable.getPageNumber() - 1 : 0,
                pageable.getPageSize(),
                pageable.getSort()
        );
        Page<ReportsResponse> reports = reportService.getAllReports(reportType, status, adjustedPageable);
        return ResponseEntity.ok(BaseResponse.of(new PageResponse<>(reports), "신고 목록 조회 성공"));
    }

    @PatchMapping("/{reportId}")
    @Operation(summary = "신고 처리", description = "신고 처리해주는 API 입니다 . PENDING-미처리, RESOLVED-해결 ,REJECTED-거절 ")
    public ResponseEntity<BaseResponse<UpdateReportResponse>> updateReport(@PathVariable Long reportId, @RequestBody @Valid UpdateReportRequest request) {
        UpdateReportResponse response = reportService.updateReport(reportId, request);
        return ResponseEntity.ok(BaseResponse.of(response, "신고 처리 완료"));
    }

}
