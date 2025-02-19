package io.crops.warmletter.domain.letter.controller;

import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.CreateLetterResponse;
import io.crops.warmletter.domain.letter.service.LetterService;
import io.crops.warmletter.global.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;

    /**
     * 편지를 처음 쓰는지
     * 답장을 보내는지
     */
    @PostMapping("/api/letters")
    public ResponseEntity<BaseResponse<CreateLetterResponse>> createLetter(@RequestBody @Valid CreateLetterRequest lettersCreate) {
        CreateLetterResponse letterResponse = letterService.createLetter(lettersCreate);
        BaseResponse<CreateLetterResponse> response = BaseResponse.of(letterResponse, "편지가 성공적으로 생성되었습니다.");
        return ResponseEntity.ok(response);
    }
}
