package io.crops.warmletter.domain.report.repository;

import io.crops.warmletter.domain.report.dto.response.ReportsResponse;
import io.crops.warmletter.domain.report.enums.ReportStatus;
import io.crops.warmletter.domain.report.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportRepositoryCustom {

    Page<ReportsResponse> findAllWithFilters(String reportType, String status, Pageable pageable);
}
