package io.crops.warmletter.domain.letter.dto.request;

import io.crops.warmletter.domain.letter.enums.LetterEvaluation;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EvaluateLetterRequest {

    private LetterEvaluation evaluation;
}
