package io.crops.warmletter.domain.badword.repository;

import io.crops.warmletter.domain.badword.entity.BadWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadWordRepository extends JpaRepository<BadWord, Long> {

    @Query("SELECT m.id, m.word FROM BadWord m WHERE m.isUsed =true")
    List<Object[]> findAllWordsOnly();

    boolean existsByWord(String word);
}
