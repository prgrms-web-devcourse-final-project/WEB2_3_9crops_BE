package io.crops.warmletter.domain.letter.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LetterMatchingTest {

    @Test
    @DisplayName("LetterMatching 생성 성공")
    void createLetterMatching() {

        LetterMatching letterMatching = LetterMatching.builder()
                .letterId(1L)
                .firstMemberId(1L)
                .secondMemberId(2L)
                .build();

        assertAll(
                () -> assertThat(letterMatching.getLetterId()).isEqualTo(1L),
                () -> assertThat(letterMatching.getFirstMemberId()).isEqualTo(1L),
                () -> assertThat(letterMatching.getSecondMemberId()).isEqualTo(2L)
        );
    }

}