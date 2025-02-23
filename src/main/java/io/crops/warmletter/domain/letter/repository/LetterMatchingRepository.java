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
            "WHERE (lm.firstMemberId = :myId AND lm.secondMemberId = :id) " +
            "OR (lm.firstMemberId = :id AND lm.secondMemberId = :myId)")
    List<LetterMatching> findMatchingIdsByMembers(Long myId, Long id);

}
