package io.crops.warmletter.domain.letter.controller.docs;

import io.crops.warmletter.domain.letter.dto.request.ApproveLetterRequest;
import io.crops.warmletter.domain.letter.dto.request.CreateLetterRequest;
import io.crops.warmletter.domain.letter.dto.response.CheckLastMatchResponse;
import io.crops.warmletter.domain.letter.dto.response.LetterResponse;
import io.crops.warmletter.domain.letter.dto.response.RandomLetterResponse;
import io.crops.warmletter.domain.letter.dto.response.TemporaryMatchingResponse;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "랜덤 편지 기능 API", description = "랜덤 편지 리스트 조회, 편지 승인, 매칭 완료 등 랜덤 편지 관련 기능의 API를 제공합니다.")
public interface RandomLetterControllerDocs {

    @Operation(
            summary = "랜덤 편지 리스트 조회",
            description = "URL 경로 변수로 지정된 카테고리에 해당하는 랜덤 편지 리스트를 조회합니다. 예를 들어, '/api/random-letters/CONSULT'와 같이 요청합니다."
    )
    @GetMapping("/{category}")
    ResponseEntity<BaseResponse<List<RandomLetterResponse>>> findRandomLetters(@PathVariable Category category);

    @Operation(
            summary = "최종 매칭 시간 확인",
            description = "현재 멤버의 최종 매칭 시간이 1시간 이내인지를 확인하여 새 편지 매칭까지 남은 시간을 반환합니다."
    )
    @PostMapping("/valid")
    ResponseEntity<BaseResponse<CheckLastMatchResponse>> checkLastMatched();

    @Operation(
            summary = "임시 테이블 데이터 확인",
            description = "임시 매칭 테이블에 현재 사용자의 데이터가 존재하는지 확인합니다. 데이터가 있으면 이미 선택된 상태로 간주합니다."
    )
    @PostMapping("/valid-table")
    ResponseEntity<BaseResponse<TemporaryMatchingResponse>> checkTemporaryMatchedTable();

    @Operation(
            summary = "매칭 취소",
            description = "현재 사용자의 임시 매칭 데이터를 삭제하여 매칭을 취소합니다."
    )
    @DeleteMapping("/matching/cancel")
    ResponseEntity<BaseResponse<Void>> matchingCancel();

    @Operation(
            summary = "랜덤 편지 승인",
            description = "사용자가 랜덤 편지를 승인하면 해당 편지에 대한 승인을 처리합니다."
    )
    @PostMapping("/approve")
    ResponseEntity<BaseResponse<Void>> approveLetter(@RequestBody ApproveLetterRequest request);

    @Operation(
            summary = "최종 랜덤 편지 매칭 완료",
            description = "임시 매칭 데이터를 최종 매칭 테이블로 옮기고 임시 데이터를 삭제한 후, 편지를 생성합니다. 생성된 편지의 정보를 반환합니다."
    )
    @PostMapping("/matching")
    ResponseEntity<BaseResponse<LetterResponse>> completeLetterMatching(@RequestBody CreateLetterRequest request);
}