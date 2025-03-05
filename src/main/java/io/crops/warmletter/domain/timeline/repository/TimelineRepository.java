package io.crops.warmletter.domain.timeline.repository;

import io.crops.warmletter.domain.timeline.dto.response.TimelineResponse;
import io.crops.warmletter.domain.timeline.entity.Timeline;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimelineRepository extends JpaRepository<Timeline, Long> {
    @Query("SELECT new io.crops.warmletter.domain.timeline.dto.response.TimelineResponse(" +
            "tl.id, tl.title, tl.content, tl.alarmType, tl.isRead)" +
            "FROM Timeline tl " +
            "WHERE tl.memberId = :memberId")
    Page<TimelineResponse> findByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Modifying(clearAutomatically=true)
    @Query("UPDATE Timeline tl SET tl.isRead=true WHERE tl.memberId = :memberId AND tl.isRead=false")
    void updateIsReadByMemberIdAndIsReadFalse(@Param("memberId") Long memberId);

    List<Timeline> findByMemberIdAndIsReadFalse(Long memberId);

    Optional<Timeline> findByIdAndMemberId(Long id, Long memberId);

    @Query("SELECT tl FROM Timeline tl WHERE tl.id IN :ids")
    List<Timeline> findByIds(@Param("ids") List<Long> ids);


}
