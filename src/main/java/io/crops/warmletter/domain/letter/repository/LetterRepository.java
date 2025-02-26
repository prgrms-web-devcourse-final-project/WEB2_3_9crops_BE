package io.crops.warmletter.domain.letter.repository;

import io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.Status;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LetterRepository extends JpaRepository<Letter, Long> {

    List<Letter> findLettersByParentLetterId(Long parentLetterId);

    @Query("SELECT new io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse(" +
            "l.id, l.content, m.zipCode, l.category, l.paperType, l.fontType, l.createdAt) " +
            "FROM Letter l JOIN Member m ON l.writerId = m.id " +
            "WHERE (:category IS NULL OR l.category = :category) " +
            "ORDER BY function('RAND')")
    List<RandomLetterResponse> findRandomLettersByCategory(@Param("category") Category category, Pageable pageable);

    @Query("SELECT l FROM Letter l WHERE l.receiverId = :memberId AND l.status = :status " +
            "ORDER BY l.deliveryCompletedAt ASC")
    List<Letter> findInDeliveryLetters(
            @Param("memberId") Long memberId,
            @Param("status") Status status);

    @Query("SELECT l FROM Letter l WHERE l.writerId = :memberId AND l.status = :status " +
            "ORDER BY l.createdAt DESC")
    List<Letter> findSavedLetters(
            @Param("memberId") Long memberId,
            @Param("status") Status status);
}
