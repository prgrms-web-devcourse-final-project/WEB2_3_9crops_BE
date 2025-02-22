package io.crops.warmletter.domain.eventpost.repository;

import io.crops.warmletter.domain.eventpost.entity.EventComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventCommentRepository extends JpaRepository<EventComment, Long>{
    @Query("SELECT ec.id,ec.content, m.zipCode FROM EventComment ec JOIN Member m ON ec.writerId = m.id WHERE ec.eventPostId = :eventPostId AND ec.isActive = true")
    List<Object[]> findEventCommentsWithZipCode(Long eventPostId);
}
