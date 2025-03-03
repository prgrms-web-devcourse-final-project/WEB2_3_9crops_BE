package io.crops.warmletter.domain.eventpost.repository;

import io.crops.warmletter.domain.eventpost.dto.response.EventPostsResponse;
import io.crops.warmletter.domain.eventpost.entity.EventPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventPostRepository extends JpaRepository<EventPost, Long>{
    Optional<EventPost> findByIsUsed(boolean isUsed);

    boolean existsByIsUsedTrue();

    @Query("SELECT new io.crops.warmletter.domain.eventpost.dto.response.EventPostsResponse(" +
            "ep.id,ep.title,ep.isUsed)" +
            "FROM EventPost ep " +
            "WHERE ep.isActive = true")
    Page<EventPostsResponse> findByActiveIsTrue(Pageable pageable);
}
