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
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;


    @PostMapping
    public ResponseEntity<BaseResponse<ReportResponse>> createReport(@RequestBody CreateReportRequest request) {
        ReportResponse response = reportService.createReport(request);
        return ResponseEntity.ok(BaseResponse.of(response, "신고 등록 성공"));
    }

    @GetMapping
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
    public ResponseEntity<BaseResponse<UpdateReportResponse>> updateReport(@PathVariable Long reportId, @RequestBody @Valid UpdateReportRequest request) {
        UpdateReportResponse response = reportService.updateReport(reportId, request);
        return ResponseEntity.ok(BaseResponse.of(response, "신고 처리 완료"));
    }

}
