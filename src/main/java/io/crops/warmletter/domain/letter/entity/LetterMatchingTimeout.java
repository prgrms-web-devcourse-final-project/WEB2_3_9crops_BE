package io.crops.warmletter.domain.letter.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "letter_matching_timeouts")
public class LetterMatchingTimeout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}

