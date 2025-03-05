package io.crops.warmletter.domain.letter.repository;

import io.crops.warmletter.domain.letter.entity.LetterTemporaryMatching;
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
public interface LetterTemporaryMatchingRepository extends JpaRepository<LetterTemporaryMatching, Long> {
    Optional<LetterTemporaryMatching> findBySecondMemberId(Long secondMemberId);

    Optional<LetterTemporaryMatching> findByLetterId(Long letterId);

    @Query("SELECT ltm FROM LetterTemporaryMatching ltm WHERE ltm.replyDeadLine <= :dateTime")
    Page<LetterTemporaryMatching> findExpiredMatchings(@Param("dateTime") LocalDateTime dateTime, Pageable pageable);



}
