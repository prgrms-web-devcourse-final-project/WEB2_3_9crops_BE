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

}
