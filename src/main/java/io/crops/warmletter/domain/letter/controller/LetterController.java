package io.crops.warmletter.domain.letter.controller;

import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.CreateLetterResponse;
import io.crops.warmletter.domain.letter.service.LetterService;
import io.crops.warmletter.global.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


    /**
     * 지정된 letterId의 이전 편지를 조회합니다.
     */
    @GetMapping("/api/v1/letters/{letterId}/previous")
    public ResponseEntity<BaseResponse<List<CreateLetterResponse>>> getPreviousLetters(@PathVariable Long letterId) {
        List<CreateLetterResponse> previousLetters = letterService.getPreviousLetters(letterId);
        BaseResponse<List<CreateLetterResponse>> response = BaseResponse.of(previousLetters, "이전 편지가 성공적으로 조회되었습니다.");
        return ResponseEntity.ok(response);
    }
}
