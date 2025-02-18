package io.crops.warmletter.domain.letter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class LetterMatchingTimeouts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}

