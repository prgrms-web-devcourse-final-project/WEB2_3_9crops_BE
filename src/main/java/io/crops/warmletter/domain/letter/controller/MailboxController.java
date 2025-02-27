package io.crops.warmletter.domain.letter.controller;

import io.crops.warmletter.domain.letter.dto.response.MailboxDetailResponse;
import io.crops.warmletter.domain.letter.dto.response.MailboxResponse;
import io.crops.warmletter.domain.letter.service.MailboxService;
import io.crops.warmletter.global.response.BaseResponse;
import io.crops.warmletter.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/mailbox")
@RequiredArgsConstructor
public class MailboxController {

    private final MailboxService mailBoxService;

    /**
     * 내 편지함 목록 조회
     */
    @GetMapping
    public BaseResponse<List<MailboxResponse>> getMailbox() {
        List<MailboxResponse> mailbox = mailBoxService.getMailbox();
        return BaseResponse.of(mailbox, "편지함 조회 완료");
    }


    /**
     * 편지함 상세 조회
     */
    @GetMapping("/{matchingId}/detail")
    public ResponseEntity<BaseResponse<PageResponse<MailboxDetailResponse>>> detailMailbox(
            @PathVariable Long matchingId,
            @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Pageable adjustedPageable = PageRequest.of(
                pageable.getPageNumber() > 0 ? pageable.getPageNumber() - 1 : 0,
                pageable.getPageSize(),
                pageable.getSort()
        );
        Page<MailboxDetailResponse> mailboxPage = mailBoxService.detailMailbox(matchingId, adjustedPageable);
        PageResponse<MailboxDetailResponse> pageResponse = new PageResponse<>(mailboxPage);
        BaseResponse<PageResponse<MailboxDetailResponse>> response = BaseResponse.of(pageResponse, "편지함 상세 조회 성공");
        return ResponseEntity.ok(response);
    }


    /**
     * 편지 마무리
     */
    @PostMapping("/{matchingId}/disconnect")
    public ResponseEntity<BaseResponse<Void>> disconnectMatching(@PathVariable Long matchingId) {
        mailBoxService.disconnectMatching(matchingId);
        return ResponseEntity.ok(BaseResponse.of(null,"매칭 차단 완료"));
    }
}
