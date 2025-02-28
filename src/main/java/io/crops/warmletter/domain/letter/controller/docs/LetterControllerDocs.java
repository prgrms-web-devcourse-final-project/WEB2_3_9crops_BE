package io.crops.warmletter.domain.letter.controller.docs;

import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.dto.request.EvaluateLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.LetterResponse;
import io.crops.warmletter.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "편지 기능 API", description = "편지 생성, 조회, 삭제, 평가 등 편지 관련 기능의 API를 제공합니다.")
public interface LetterControllerDocs {

    @Operation(summary = "편지 작성하기",
            description = "새로운 편지를 작성합니다. 첫 작성 시 랜덤 편지로, 답장 시 주고받는 편지로 구분됩니다.")
    @PostMapping("/api/letters")
    ResponseEntity<BaseResponse<LetterResponse>> createLetter(@RequestBody @Valid CreateLetterRequest request);

    @Operation(summary = "이전 편지 조회",
            description = "지정된 letterId의 이전 편지 목록을 조회합니다.")
    @GetMapping("/api/v1/letters/{letterId}/previous")
    ResponseEntity<BaseResponse<List<LetterResponse>>> getPreviousLetters(@PathVariable Long letterId);

    @Operation(summary = "편지 삭제 (소프트 딜리트)",
            description = "지정된 letterId의 편지를 소프트 딜리트 처리합니다.")
    @DeleteMapping("/api/letters/{letterId}")
    ResponseEntity<BaseResponse<Void>> deleteLetter(@PathVariable Long letterId);

    @Operation(summary = "편지 단건 조회",
            description = "지정된 letterId의 편지를 조회합니다.")
    @GetMapping("/api/letters/{letterId}")
    ResponseEntity<BaseResponse<LetterResponse>> getLetterById(@PathVariable Long letterId);

    @Operation(summary = "편지 평가하기",
            description = "지정된 letterId의 편지를 평가합니다.")
    @PostMapping("/api/letters/{letterId}/evaluate")
    ResponseEntity<BaseResponse<Void>> evaluateLetter(@PathVariable Long letterId, @RequestBody @Valid EvaluateLetterRequest request);




}
