package io.crops.warmletter.domain.letter.repository;

import io.crops.warmletter.domain.letter.entity.Letter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LettersRepository extends JpaRepository<Letter, Long> {
}
