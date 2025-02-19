package io.crops.warmletter.domain.badword.controller;


import io.crops.warmletter.domain.badword.dto.request.CreateBadWordRequest;
import io.crops.warmletter.domain.badword.dto.request.UpdateBadWordStatusRequest;
import io.crops.warmletter.domain.badword.service.BadWordService;
import io.crops.warmletter.global.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bad-words")
@RequiredArgsConstructor
public class BadWordController {


    private final BadWordService badWordService;

    @PostMapping
    public ResponseEntity<BaseResponse<Void>> createBadWord(@RequestBody @Valid CreateBadWordRequest request) {
        badWordService.createBadWord(request);
        return ResponseEntity.ok(BaseResponse.of(null, "검열단어 등록완료"));
    }

    @PatchMapping("/{badWordId}/status")
    public ResponseEntity<BaseResponse<Void>> updateBadWordStatus(
            @PathVariable Long badWordId,
            @RequestBody @Valid UpdateBadWordStatusRequest request) {

        badWordService.updateBadWordStatus(badWordId, request);
        return ResponseEntity.ok(BaseResponse.of(null, "금칙어 상태 변경 완료"));
    }


}
