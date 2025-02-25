package io.crops.warmletter.domain.report.repository;

import io.crops.warmletter.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long>, ReportRepositoryCustom {
    boolean existsByLetterIdAndMemberId(Long letterId, Long MemberId);
    boolean existsBySharePostIdAndMemberId(Long shareId, Long MemberId);
    boolean existsByEventCommentIdAndMemberId(Long eventCommentId, Long MemberId);
}
