package io.crops.warmletter.domain.letter.facade;

import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.LetterResponse;
import io.crops.warmletter.domain.letter.service.LetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LetterFacade {

    private final LetterService letterService;

    public LetterResponse createLetter(CreateLetterRequest request) {
        return letterService.createLetter(request);
    }
}
