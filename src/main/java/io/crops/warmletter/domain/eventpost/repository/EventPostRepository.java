package io.crops.warmletter.domain.eventpost.repository;

import io.crops.warmletter.domain.eventpost.entity.EventPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventPostRepository extends JpaRepository<EventPost, Long>{
    Optional<EventPost> findByIsUsed(boolean isUsed);

    boolean existsByIsUsedTrue();
}
