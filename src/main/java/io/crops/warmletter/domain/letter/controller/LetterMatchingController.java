package io.crops.warmletter.domain.letter.controller;

import io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse;
import io.crops.warmletter.domain.letter.service.LetterMatchingService;
import io.crops.warmletter.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LetterMatchingController {

    private final LetterMatchingService randomLetterService;

    /**
     *  랜덤 편지 리스트 확인 - 카테고리로 조회 시
     */
    @GetMapping("/api/random/{category}")
    public ResponseEntity<BaseResponse<List<RandomLetterResponse>>> findRandomLetters(@PathVariable String category) {
        List<RandomLetterResponse> randomLetters = randomLetterService.findRandomLetters(category);
        BaseResponse<List<RandomLetterResponse>> response= BaseResponse.of(randomLetters, "랜덤편지 조회 완료");
        return ResponseEntity.ok(response);
    }

}
