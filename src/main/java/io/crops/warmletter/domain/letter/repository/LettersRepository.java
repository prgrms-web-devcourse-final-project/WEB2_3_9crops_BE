package io.crops.warmletter.domain.letter.repository;

import io.crops.warmletter.domain.letter.entity.Letters;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LettersRepository extends JpaRepository<Letters, Long> {
}
