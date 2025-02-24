package io.crops.warmletter.domain.letter.repository;

import io.crops.warmletter.domain.letter.entity.LetterTemporaryMatching;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LetterTemporaryMatchingRepository extends JpaRepository<LetterTemporaryMatching, Long> {
    Optional<LetterTemporaryMatching> findBySecondMemberId(Long secondMemberId);
}
