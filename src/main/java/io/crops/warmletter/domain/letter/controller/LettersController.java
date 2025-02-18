package io.crops.warmletter.domain.letter.controller;

import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.service.LettersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LettersController {

    private final LettersService lettersService;

    /**
     * 편지를 처음 쓰는지
     * 답장을 보내는지
     */
    @PostMapping("/api/letters")
    public void createLetters(@RequestBody @Valid CreateLetterRequest lettersCreate) {
        lettersService.write(lettersCreate);

    }
}
