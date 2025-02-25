package io.crops.warmletter.domain.letter.controller;

import io.crops.warmletter.domain.letter.dto.response.CheckLastMatchResponse;
import io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse;
import io.crops.warmletter.domain.letter.dto.response.TemporaryMatchingResponse;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.service.LetterMatchingService;
import io.crops.warmletter.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LetterMatchingController {

    private final LetterMatchingService randomLetterService;

    /**
     *  랜덤 편지 리스트 확인 - 카테고리로 조회 시
     */
    @GetMapping("/api/random/{category}")
    public ResponseEntity<BaseResponse<List<RandomLetterResponse>>> findRandomLetters(@PathVariable Category category) {
        List<RandomLetterResponse> randomLetters = randomLetterService.findRandomLetters(category);
        BaseResponse<List<RandomLetterResponse>> response= BaseResponse.of(randomLetters, "랜덤편지 조회 완료");
        return ResponseEntity.ok(response);
    }

    /**
     * member 최종 매칭 시간 1시간이내일 경우 확인
     */
    @PostMapping("/api/random-letters/valid")
    public ResponseEntity<BaseResponse<CheckLastMatchResponse>>checkLastMatched(){
        CheckLastMatchResponse checkLastMatchResponse = randomLetterService.checkLastMatched();
        BaseResponse<CheckLastMatchResponse> response = BaseResponse.of(checkLastMatchResponse, "새 편지 매칭까지 남은 시간");
        return ResponseEntity.ok(response);
    }

    /**
     * 임시테이블에 내 데이터가 있는지 확인 (있으면 이미 선택됨 페이지, 없으면 랜덤 편지 리스트 확인)
     */
    @PostMapping("/api/random-letters/valid-table")
    public ResponseEntity<BaseResponse<TemporaryMatchingResponse>> checkTemporaryMatchedTable(){
        TemporaryMatchingResponse temporaryMatchingResponse = randomLetterService.checkTemporaryMatchedTable();
        BaseResponse<TemporaryMatchingResponse> response = BaseResponse.of(temporaryMatchingResponse, "임시 테이블 데이터 확인 완료");
        return ResponseEntity.ok(response);
    }

    /**
     * 매칭 취소 하기 - 임시테이블 제거
     */
    @DeleteMapping("/api/random-letters/matching/cancel")
    public ResponseEntity<BaseResponse<String>> matchingCancel(){
        randomLetterService.matchingCancel();
        BaseResponse<String> response = BaseResponse.of("매칭 취소 성공", "랜덤 편지 매칭이 취소되었습니다.");
        return ResponseEntity.ok(response);
    }


//    /**
//     * 랜덤편지 승인
//     */
//    @PostMapping("/api/random-letters/matching")
//    public void letterMatching(@RequestBody RandomMatchingRequest request) {
//        randomLetterService.letterMatching(request);
//    }


}
