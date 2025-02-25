package io.crops.warmletter.domain.letter.controller;

import io.crops.warmletter.domain.letter.dto.request.ApproveLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.CheckLastMatchResponse;
import io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse;
import io.crops.warmletter.domain.letter.dto.response.TemporaryMatchingResponse;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.service.RandomLetterService;
import io.crops.warmletter.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(("/api/random-letters/"))
@RequiredArgsConstructor
public class RandomLetterController {

    private final RandomLetterService randomLetterService;

    /**
     *  랜덤 편지 리스트 확인 - 카테고리로 조회 시
     */
    @GetMapping("/{category}")
    public ResponseEntity<BaseResponse<List<RandomLetterResponse>>> findRandomLetters(@PathVariable Category category) {
        List<RandomLetterResponse> randomLetters = randomLetterService.findRandomLetters(category);
        BaseResponse<List<RandomLetterResponse>> response= BaseResponse.of(randomLetters, "랜덤편지 조회 완료");
        return ResponseEntity.ok(response);
    }

    /**
     * member 최종 매칭 시간 1시간이내일 경우 확인
     */
    @PostMapping("/valid")
    public ResponseEntity<BaseResponse<CheckLastMatchResponse>>checkLastMatched(){
        CheckLastMatchResponse checkLastMatchResponse = randomLetterService.checkLastMatched();
        BaseResponse<CheckLastMatchResponse> response = BaseResponse.of(checkLastMatchResponse, "새 편지 매칭까지 남은 시간");
        return ResponseEntity.ok(response);
    }

    /**
     * 임시테이블에 내 데이터가 있는지 확인 (있으면 이미 선택됨 페이지, 없으면 랜덤 편지 리스트 확인)
     */
    @PostMapping("/valid-table")
    public ResponseEntity<BaseResponse<TemporaryMatchingResponse>> checkTemporaryMatchedTable(){
        TemporaryMatchingResponse temporaryMatchingResponse = randomLetterService.checkTemporaryMatchedTable();
        BaseResponse<TemporaryMatchingResponse> response = BaseResponse.of(temporaryMatchingResponse, "임시 테이블 데이터 확인 완료");
        return ResponseEntity.ok(response);
    }

    /**
     * 매칭 취소 하기 - 임시테이블 제거
     */
    @DeleteMapping("/matching/cancel")
    public ResponseEntity<BaseResponse<Void>> matchingCancel(){
        randomLetterService.matchingCancel();
        BaseResponse<Void> response = BaseResponse.of(null, "랜덤 편지 매칭이 취소되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 랜덤 편지 승인하기
     */
    @PostMapping("/approve")
    public ResponseEntity<BaseResponse<Void>> approveLetter(@RequestBody ApproveLetterRequest request) {
        randomLetterService.approveLetter(request);
        BaseResponse<Void> response = BaseResponse.of(null, "랜덤 편지 승인 완료");
        return ResponseEntity.ok(response);
    }


//    /**
//     * 랜덤편지 매칭
//     */
//    @PostMapping("/api/random-letters/matching")
//    public void letterMatching(@RequestBody RandomMatchingRequest request) {
//        randomLetterService.letterMatching(request);
//    }


}
