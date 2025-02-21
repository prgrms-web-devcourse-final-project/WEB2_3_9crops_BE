package io.crops.warmletter.domain.letter.repository;

import io.crops.warmletter.domain.letter.entity.Letter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LetterRepository extends JpaRepository<Letter, Long> {

    List<Letter> findLettersByParentLetterId(Long parentLetterId);

}
