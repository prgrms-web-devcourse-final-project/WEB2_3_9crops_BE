package io.crops.warmletter.domain.moderation.repository;

import io.crops.warmletter.domain.moderation.entity.Moderation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModerationRepository extends JpaRepository<Moderation, Long> {

    @Query("SELECT m.word FROM Moderation m")
    List<String> findAllWordsOnly();

    boolean existsByWord(String word);
}
