package io.crops.warmletter.domain.badword.controller;


import io.crops.warmletter.domain.badword.dto.request.CreateBadWordRequest;
import io.crops.warmletter.domain.badword.dto.request.UpdateBadWordRequest;
import io.crops.warmletter.domain.badword.dto.request.UpdateBadWordStatusRequest;
import io.crops.warmletter.domain.badword.dto.response.UpdateBadWordResponse;
import io.crops.warmletter.domain.badword.service.BadWordService;
import io.crops.warmletter.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bad-words")
@Tag(name = "BadWord", description = "신고 관련 API")
@RequiredArgsConstructor
public class BadWordController {


    private final BadWordService badWordService;

    @PostMapping
    @Operation(summary = "금칙어 등록", description = "금칙어 등록하는 API입니다.")
    public ResponseEntity<BaseResponse<Void>> createBadWord(@RequestBody @Valid CreateBadWordRequest request) {
        badWordService.createBadWord(request);
        return ResponseEntity.ok(BaseResponse.of(null, "금칙어 등록완료"));
    }

    @PatchMapping("/{badWordId}/status")
    @Operation(summary = "금칙어 상태변경", description = "금칙어 상태변경 활성여부 API입니다.")
    public ResponseEntity<BaseResponse<Void>> updateBadWordStatus(
            @PathVariable Long badWordId,
            @RequestBody @Valid UpdateBadWordStatusRequest request) {

        badWordService.updateBadWordStatus(badWordId, request);
        return ResponseEntity.ok(BaseResponse.of(null, "금칙어 상태 변경 완료"));
    }

    @GetMapping
    @Operation(summary = "금칙어 조회", description = "등록된 금칙어 조회하는 API입니다.")
    public ResponseEntity<BaseResponse<List<Map<String, String>>>> getBadWords() {
        List<Map<String, String>> response = badWordService.getBadWords();
        return ResponseEntity.ok(BaseResponse.of(response, "금칙어 조회"));
    }

    @PatchMapping("/{badWordId}")
    @Operation(summary = "금칙어 변경", description = "기존에 있는 금칙어를 변경하는 API입니다.")
    public ResponseEntity<BaseResponse<UpdateBadWordResponse>> updateBadWord(
            @PathVariable Long badWordId,
            @RequestBody @Valid UpdateBadWordRequest request) {
        UpdateBadWordResponse response = badWordService.updateBadWord(badWordId, request);
        return ResponseEntity.ok(BaseResponse.of(response, "금칙어 변경 성공"));
    }

}
