package io.crops.warmletter.domain.letter.repository;

import io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.Category;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LetterRepository extends JpaRepository<Letter, Long> {

    List<Letter> findLettersByParentLetterId(Long parentLetterId);

    @Query("SELECT new io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse(" +
            "l.id, l.content, m.zipCode, l.category, l.paperType, l.fontType, l.createdAt) " +
            "FROM Letter l JOIN Member m ON l.writerId = m.id " +
            "WHERE (:category IS NULL OR l.category = :category) " +
            "ORDER BY function('RAND')")
    List<RandomLetterResponse> findRandomLettersByCategory(@Param("category") Category category, Pageable pageable);

    Optional<Letter> findByIdAndReceiverId(Long id, Long receiverId);
}