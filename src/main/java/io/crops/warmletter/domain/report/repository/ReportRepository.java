package io.crops.warmletter.domain.report.repository;

import io.crops.warmletter.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByLetterId(Long letterId);
    boolean existsBySharePostId(Long shareId);
    boolean existsByEventCommentId(Long eventCommentId);
}
