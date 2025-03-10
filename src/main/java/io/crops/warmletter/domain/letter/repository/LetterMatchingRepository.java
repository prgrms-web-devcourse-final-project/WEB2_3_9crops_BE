package io.crops.warmletter.domain.letter.repository;

import io.crops.warmletter.domain.letter.dto.response.MailboxResponse;
import io.crops.warmletter.domain.letter.entity.LetterMatching;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LetterMatchingRepository extends JpaRepository<LetterMatching, Long> {

    boolean existsByIdAndFirstMemberIdOrSecondMemberId(Long Id, Long firstMemberId, Long secondMemberId);


    @Query("SELECT new io.crops.warmletter.domain.letter.dto.response.MailboxResponse(" +
            "lm.id, " +
            "m.zipCode, " +
            "m.id, " +
            "lm.isActive, " +
            "CASE WHEN COALESCE(SUM(CASE WHEN l.isRead = false AND l.writerId <> :myId THEN 1 ELSE 0 END), 0) = 0 THEN true ELSE false END, " +
            "COALESCE(CAST(COUNT(l) AS long), 0L)" +
            ") " +
            "FROM LetterMatching lm " +
            "LEFT JOIN Letter l ON lm.id = l.matchingId AND (l.status = 'DELIVERED' OR (l.writerId = :myId AND l.status != 'SAVED')) AND l.isActive = true " +
            "JOIN Member m ON (CASE WHEN lm.firstMemberId = :myId THEN lm.secondMemberId ELSE lm.firstMemberId END) = m.id " +
            "WHERE (lm.firstMemberId = :myId OR lm.secondMemberId = :myId) " +
            "GROUP BY lm.id, m.zipCode, m.id, lm.isActive " +
            "ORDER BY lm.id DESC")
    List<MailboxResponse> findMailboxDetails(@Param("myId") Long myId);

}
