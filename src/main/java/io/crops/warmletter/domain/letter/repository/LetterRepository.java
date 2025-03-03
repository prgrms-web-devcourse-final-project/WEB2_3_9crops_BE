package io.crops.warmletter.domain.letter.repository;

import io.crops.warmletter.domain.letter.dto.response.LetterDraftResponse;
import io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.Status;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

import static io.crops.warmletter.domain.letter.enums.Status.IN_DELIVERY;

@Repository
public interface LetterRepository extends JpaRepository<Letter, Long> {

    List<Letter> findLettersByParentLetterId(Long parentLetterId);

    @Query("SELECT new io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse(" +
            "l.id, l.writerId, l.title, m.zipCode, l.category, l.createdAt) " +
            "FROM Letter l JOIN Member m ON l.writerId = m.id " +
            "WHERE l.letterType = 'RANDOM' " +
            "  AND l.status = 'DELIVERED' " +
            "  AND l.isActive = TRUE" +
            "  AND l.writerId <> :currentUserId " +
            "  AND (:category = io.crops.warmletter.domain.letter.enums.Category.ALL OR l.category = :category) " +
            "ORDER BY function('RAND')")
    List<RandomLetterResponse> findRandomLettersByCategory(@Param("category") Category category, Long currentUserId, Pageable pageable);

    Optional<Letter> findByIdAndReceiverId(Long id, Long receiverId);

    Optional<Letter> findByIdAndWriterId(Long letterId, Long writerId);

    Page<Letter> findByMatchingIdOrderByIdDesc(Long matchingId, Pageable pageable);

    List<Letter> findByReceiverIdAndStatus(Long currentUserId, Status status);

    @Query("SELECT new io.crops.warmletter.domain.letter.dto.response.LetterDraftResponse(" +
            "l.id, l.writerId, l.receiverId, l.parentLetterId, " +
            "l.title, l.content, l.category, l.paperType, l.fontType, l.status, " +
            "CASE WHEN m.id IS NOT NULL THEN m.isActive ELSE false END, " +
            "l.deliveryStartedAt, l.deliveryCompletedAt, l.matchingId) " +
            "FROM Letter l LEFT JOIN LetterMatching m ON l.matchingId = m.id " +
            "WHERE l.writerId = :writerId AND l.status = :status")
    List<LetterDraftResponse> findDraftLettersWithMatching(@Param("writerId") Long writerId,
                                                           @Param("status") Status status);

}
