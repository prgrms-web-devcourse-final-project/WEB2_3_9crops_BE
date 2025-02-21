package io.crops.warmletter.domain.badword.repository;

import io.crops.warmletter.domain.badword.dto.response.BadWordResponse;
import io.crops.warmletter.domain.badword.entity.BadWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadWordRepository extends JpaRepository<BadWord, Long> {
    boolean existsByWord(String word);

    @Query("SELECT new io.crops.warmletter.domain.badword.dto.response.BadWordResponse(b.id, b.word) FROM BadWord b")
    List<BadWordResponse> findAllBadWords();

}
