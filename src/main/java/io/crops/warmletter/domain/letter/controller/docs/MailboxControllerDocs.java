package io.crops.warmletter.domain.letter.controller.docs;

import io.crops.warmletter.domain.letter.dto.response.MailboxDetailResponse;
import io.crops.warmletter.domain.letter.dto.response.MailboxResponse;
import io.crops.warmletter.global.response.BaseResponse;
import io.crops.warmletter.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "내 편지함 기능 API", description = "내 편지함 목록 조회, 편지함 상세 조회, 편지 마무리(매칭 차단) 기능을 제공합니다.")
public interface MailboxControllerDocs {

    @Operation(summary = "내 편지함 목록 조회", description = "로그인한 사용자의 전체 편지함 목록을 조회합니다.")
    @GetMapping("/api/mailbox")
    BaseResponse<List<MailboxResponse>> getMailbox();

    @Operation(
            summary = "편지함 상세 조회",
            description = "지정된 매칭 ID에 해당하는 편지함의 상세 정보를 페이징 처리하여 조회합니다. ")
    @GetMapping("/api/mailbox/{matchingId}/detail")
    ResponseEntity<BaseResponse<PageResponse<MailboxDetailResponse>>> detailMailbox(@PathVariable Long matchingId, Pageable pageable);

    @Operation(
            summary = "편지 마무리 (매칭 차단)",
            description = "지정된 매칭 ID에 해당하는 편지 매칭을 차단합니다. " +
                    "매칭 차단 후, 편지함 목록에서 해당 매칭은 더 이상 조회되지 않습니다."
    )
    @PostMapping("/api/mailbox/{matchingId}/disconnect")
    ResponseEntity<BaseResponse<Void>> disconnectMatching(@PathVariable Long matchingId);
}