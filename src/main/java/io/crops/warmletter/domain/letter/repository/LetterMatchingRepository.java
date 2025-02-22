package io.crops.warmletter.domain.letter.repository;

import io.crops.warmletter.domain.letter.entity.LetterMatching;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LetterMatchingRepository extends JpaRepository<LetterMatching, Long> {

    //firstMemberId 1이고 secondMemberId: 2면 만약 memberId가 1이면 2반환 아니면 1반환
    @Query("SELECT DISTINCT CASE " +
            "WHEN lm.firstMemberId = :memberId THEN lm.secondMemberId " +
            "ELSE lm.firstMemberId END " +
            "FROM LetterMatching lm " +
            "WHERE lm.firstMemberId = :memberId OR lm.secondMemberId = :memberId")
    List<Long> findMatchedMembers(@Param("memberId") Long memberId);


    @Query("SELECT lm FROM LetterMatching lm " +
            "WHERE (lm.firstMemberId = :user1Id AND lm.secondMemberId = :user2Id) " +
            "OR (lm.firstMemberId = :user2Id AND lm.secondMemberId = :user1Id)")
    List<LetterMatching> findMatchingIdsByMembers(Long user1Id, Long user2Id);

}
