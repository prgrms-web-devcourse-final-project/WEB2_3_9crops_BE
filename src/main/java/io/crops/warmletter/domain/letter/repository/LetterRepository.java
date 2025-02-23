package io.crops.warmletter.domain.letter.repository;

import io.crops.warmletter.domain.letter.entity.Letter;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LetterRepository extends JpaRepository<Letter, Long> {

    List<Letter> findLettersByParentLetterId(Long parentLetterId);

    @Query(value =
            "SELECT * " +
            "FROM letters " +
            "WHERE category = :category " +
            "ORDER BY RAND() " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Letter> findRandomLettersByCategory(@Param("category") String category, @Param("limit") int limit);


    @Query(value =
            "SELECT * " +
            "FROM letters " +
            "ORDER BY RAND() " +
            "LIMIT :limit",
            nativeQuery = true)

    List<Letter> findRandomLetters(@Param("limit") int limit);

}