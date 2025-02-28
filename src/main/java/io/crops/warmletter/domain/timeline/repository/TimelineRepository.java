package io.crops.warmletter.domain.timeline.repository;

import io.crops.warmletter.domain.timeline.dto.response.ReadNotificationResponse;
import io.crops.warmletter.domain.timeline.dto.response.TimelineResponse;
import io.crops.warmletter.domain.timeline.entity.Timeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimelineRepository extends JpaRepository<Timeline, Long> {
    @Query("SELECT new io.crops.warmletter.domain.timeline.dto.response.TimelineResponse(" +
            "tl.id, tl.title, tl.alarmType, tl.isRead)" +
            "FROM Timeline tl " +
            "WHERE tl.memberId = :memberId")
    List<TimelineResponse> findByMemberId(Long memberId);

    @Modifying(clearAutomatically=true)
    @Query("UPDATE Timeline tl SET tl.isRead=true WHERE tl.memberId = :memberId AND tl.isRead=false")
    void updateIsReadByMemberIdAndIsReadFalse(Long memberId);

    List<Timeline> findByMemberIdAndIsReadFalse(Long memberId);

    Optional<Timeline> findByIdAndMemberId(Long id, Long memberId);
}
