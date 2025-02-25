package io.crops.warmletter.domain.timeline.repository;

import io.crops.warmletter.domain.timeline.dto.response.TimeLineResponse;
import io.crops.warmletter.domain.timeline.entity.Timeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeLineRepository extends JpaRepository<Timeline, Long> {
    @Query("SELECT new io.crops.warmletter.domain.timeline.dto.response.TimeLineResponse(" +
            "tl.id, tl.title, tl.alarmType, tl.isRead)" +
            "FROM Timeline tl " +
            "WHERE tl.memberId = :memberId")
    List<TimeLineResponse> findByMemberId(long memberId);
}
