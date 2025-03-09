package io.crops.warmletter.domain.letter.repository;

import io.crops.warmletter.domain.letter.dto.response.LetterDraftResponse;
import io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse;
import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.Status;
import io.crops.warmletter.domain.timeline.dto.response.LetterAlarmResponse;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface LetterRepository extends JpaRepository<Letter, Long> {

    List<Letter> findLettersByParentLetterId(Long parentLetterId);

    @Query("SELECT new io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse(" +
            "l.id, l.writerId, l.title, m.zipCode, l.category, l.paperType, l.fontType, l.createdAt) " +
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

    @Query("SELECT l FROM Letter l WHERE l.matchingId = :matchingId " +
            "AND l.isActive = true " +
            "AND (l.writerId = :currentUserId OR l.status = 'DELIVERED') " +
            "AND (l.writerId != :currentUserId OR l.status != 'SAVED') " +
            "ORDER BY l.id DESC")
    Page<Letter> findDeliveredOrMyLettersByMatchingId(
            @Param("matchingId") Long matchingId,
            @Param("userId") Long currentUserId,
            Pageable pageable
    );
    List<Letter> findByReceiverIdAndStatus(Long currentUserId, Status status);

    @Query("SELECT new io.crops.warmletter.domain.letter.dto.response.LetterDraftResponse(" +
            "l.id, " +
            "l.writerId, " +
            "l.receiverId, " +
            "l.parentLetterId, " +
            "l.title, " +
            "l.content, " +
            "l.category, " +
            "l.paperType, " +
            "l.fontType, " +
            "l.status, " +
            "l.deliveryStartedAt, " +
            "l.deliveryCompletedAt, " +
            "l.matchingId) " +
            "FROM Letter l LEFT JOIN LetterMatching m ON l.matchingId = m.id " +
            "WHERE l.writerId = :writerId AND l.status = :status")
    List<LetterDraftResponse> findDraftLettersWithMatching(@Param("writerId") Long writerId,
                                                           @Param("status") Status status);

    @Query("SELECT count(l) from Letter l " +
            "where l.receiverId = :receiverId " +
            "and l.isRead = false " +
            "and l.isActive = true " +
            "and l.status = 'DELIVERED'")
    int countLetterUnreadCount(Long receiverId);

    @Query("SELECT l FROM Letter l " +
            "WHERE l.id = :id " +
            "AND l.writerId = :writerId " +
            "AND l.isActive = true " +
            "AND l.status = 'SAVED'")
    Optional<Letter> findByIdAndWriterIdAndStatusIsSAVED(Long id, Long writerId);

    List<Letter> findByStatusAndDeliveryCompletedAtLessThanEqual(Status status, LocalDateTime now);

    @Query("SELECT new io.crops.warmletter.domain.timeline.dto.response.LetterAlarmResponse(" +
            "l.writerId, m.zipCode) " +
            "FROM Letter l " +
            "JOIN Member m ON l.writerId = m.id " +
            "WHERE l.status = 'IN_DELIVERY' " +
            "AND l.deliveryCompletedAt <= :now")
    List<LetterAlarmResponse> findZipCodeByLettersToComplete(LocalDateTime now);
}
