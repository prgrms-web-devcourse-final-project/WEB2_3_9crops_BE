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

    //firstMemberId 1이고 secondMemberId: 2면 만약 memberId가 1이면 2반환 아니면 1반환
    @Query("SELECT DISTINCT CASE " +
            "WHEN lm.firstMemberId = :memberId THEN lm.secondMemberId " +
            "ELSE lm.firstMemberId END " +
            "FROM LetterMatching lm " +
            "WHERE lm.firstMemberId = :memberId OR lm.secondMemberId = :memberId")
    List<Long> findMatchedMembers(@Param("memberId") Long memberId);


    @Query("SELECT lm FROM LetterMatching lm " +
            "WHERE (lm.firstMemberId = :myId AND lm.secondMemberId = :id) " +
            "OR (lm.firstMemberId = :id AND lm.secondMemberId = :myId)")
    List<LetterMatching> findMatchingIdsByMembers(Long myId, Long id);

    boolean existsByIdAndFirstMemberIdOrSecondMemberId(Long Id, Long firstMemberId, Long secondMemberId);

    /**
     * 	•	만약 모든 편지가 읽혔다면 SUM 값이 0이 되어 true를 반환하고,
     * 	•	하나라도 읽지 않은 편지가 있으면 SUM이 0보다 커서 false를 반환합니다.
     *      즉, “모든 관련 편지가 다 읽혔다면 true, 그렇지 않으면 false”가 맞습니다.
     */
    @Query("SELECT new io.crops.warmletter.domain.letter.dto.response.MailboxResponse(" +
            "lm.id, " +
            "m.zipCode, " +
            "lm.isActive, " +
            "CASE WHEN COALESCE(SUM(CASE WHEN l.isRead = false AND l.writerId <> :myId THEN 1 ELSE 0 END), 0) = 0 THEN true ELSE false END, " +
            "COALESCE(COUNT(l), 0)" +
            ") " +
            "FROM LetterMatching lm " +
            "LEFT JOIN Letter l ON lm.id = l.matchingId AND (l.status = 'DELIVERED' OR (l.writerId = :myId AND l.status != 'SAVED')) AND l.isActive = true " +
            "JOIN Member m ON (CASE WHEN lm.firstMemberId = :myId THEN lm.secondMemberId ELSE lm.firstMemberId END) = m.id " +
            "WHERE (lm.firstMemberId = :myId OR lm.secondMemberId = :myId) " +
            "GROUP BY lm.id, m.zipCode, lm.isActive " +
            "ORDER BY lm.id DESC")
    List<MailboxResponse> findMailboxDetails(@Param("myId") Long myId);


//    @Query("SELECT new io.crops.warmletter.domain.letter.dto.response.MailboxResponse(" +
//            "lm.id, " +
//            "m.zipCode, " +
//            "lm.isActive, " +
//            "CASE WHEN COUNT(l) > 0 AND COUNT(l) = SUM(CASE WHEN l.isRead = true THEN 1 ELSE 0 END) THEN true ELSE false END, " +
//            "COUNT(l)) " +
//            "FROM LetterMatching lm " +
//            "JOIN Member m ON m.id = CASE WHEN lm.firstMemberId = :myId THEN lm.secondMemberId ELSE lm.firstMemberId END " +
//            "LEFT JOIN Letter l ON l.matchingId = lm.id " +
//            "WHERE lm.firstMemberId = :myId OR lm.secondMemberId = :myId " +
//            "GROUP BY lm.id, m.zipCode, lm.isActive " +
//            "ORDER BY lm.id DESC")
//    List<MailboxResponse> findMailboxData(@Param("myId") Long myId);

}
