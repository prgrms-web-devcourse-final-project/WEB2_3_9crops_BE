package io.crops.warmletter.domain.letter.controller;

import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.LetterResponse;
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
@RequestMapping("/api")
public class LetterController {

    private final LetterService letterService;

    /**
     * 편지를 처음 쓰는지
     * 답장을 보내는지
     */
    @PostMapping("/letters")
    public ResponseEntity<BaseResponse<LetterResponse>> createLetter(@RequestBody @Valid CreateLetterRequest request) {
        LetterResponse letterResponse = letterService.createLetter(request);
        BaseResponse<LetterResponse> response = BaseResponse.of(letterResponse, "편지가 성공적으로 생성되었습니다.");
        return ResponseEntity.ok(response);
    }


    /**
     * 지정된 letterId의 이전 편지를 조회합니다.
     */
    @GetMapping("/v1/letters/{letterId}/previous")
    public ResponseEntity<BaseResponse<List<LetterResponse>>> getPreviousLetters(@PathVariable Long letterId) {
        List<LetterResponse> previousLetters = letterService.getPreviousLetters(letterId);
        BaseResponse<List<LetterResponse>> response = BaseResponse.of(previousLetters, "이전 편지가 전송 완료.");
        return ResponseEntity.ok(response);
    }

    /**
     * 지정된 letterId 삭제 (softDelete)
     */
    @DeleteMapping("/letters/{letterId}")
    public ResponseEntity<BaseResponse<Void>> deleteLetter(@PathVariable Long letterId) {
        letterService.deleteLetter(letterId);
        BaseResponse<Void> response = BaseResponse.of(null, "편지 삭제 완료");
        return ResponseEntity.ok(response);
    }


    /**
     * 지정된 letterId로 편지 단건 조회
     */
    @GetMapping("/letters/{letterId}")
    public ResponseEntity<BaseResponse<LetterResponse>> getLetterById(@PathVariable Long letterId) {
        LetterResponse letterResponse = letterService.getLetterById(letterId);
        BaseResponse<LetterResponse> response = BaseResponse.of(letterResponse, "편지 조회 완료");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/letters/{letterId}/evaluate")
    public ResponseEntity<BaseResponse<Void>> evaluateLetter(@PathVariable Long letterId) {
        letterService.evaluateLetter(letterId);
        return ResponseEntity.ok(BaseResponse.of(null, "편지 평가 완료"));
    }
}
