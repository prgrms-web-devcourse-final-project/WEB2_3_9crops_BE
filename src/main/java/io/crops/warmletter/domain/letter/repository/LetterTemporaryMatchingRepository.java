package io.crops.warmletter.domain.letter.repository;

import io.crops.warmletter.domain.letter.entity.LetterTemporaryMatching;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LetterTemporaryMatchingRepository extends JpaRepository<LetterTemporaryMatching, Long> {
    Optional<LetterTemporaryMatching> findBySecondMemberId(Long secondMemberId);

    // 편지 ID에 대한 비관적 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ltm FROM LetterTemporaryMatching ltm WHERE ltm.letterId = :letterId")
    Optional<LetterTemporaryMatching> findByLetterIdForUpdate(@Param("letterId") Long letterId);

}
