package io.crops.warmletter.domain.letter.repository;

import io.crops.warmletter.domain.letter.entity.LetterTemporaryMatching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LetterTemporaryMatchingRepository extends JpaRepository<LetterTemporaryMatching, Long> {
    Optional<LetterTemporaryMatching> findBySecondMemberId(Long secondMemberId);
}
