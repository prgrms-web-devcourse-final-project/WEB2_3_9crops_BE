package io.crops.warmletter.domain.report.repository;

import io.crops.warmletter.domain.report.entity.Report;
import io.crops.warmletter.domain.report.enums.ReportStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long>, ReportRepositoryCustom {
    boolean existsByLetterIdAndMemberId(Long letterId, Long MemberId);
    boolean existsBySharePostIdAndMemberId(Long shareId, Long MemberId);
    boolean existsByEventCommentIdAndMemberId(Long eventCommentId, Long MemberId);


    @Query("SELECT r FROM Report r WHERE "
            + "(:letterId IS NOT NULL AND r.letterId = :letterId OR "
            + " :sharePostId IS NOT NULL AND r.sharePostId = :sharePostId OR "
            + " :eventCommentId IS NOT NULL AND r.eventCommentId = :eventCommentId) "
            + "AND r.reportStatus = :status")
    List<Report> findBySameTargetAndStatus(
            @Param("letterId") Long letterId,
            @Param("sharePostId") Long sharePostId,
            @Param("eventCommentId") Long eventCommentId,
            @Param("status") ReportStatus status
    );

}
