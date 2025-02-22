package io.crops.warmletter.domain.eventpost.repository;

import io.crops.warmletter.domain.eventpost.entity.EventPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventPostRepository extends JpaRepository<EventPost, Long>{
    EventPost findFirstByIsUsed(boolean isUsed);
}
