package io.crops.warmletter.domain.letter.controller;

import io.crops.warmletter.domain.letter.dto.response.MailboxResponse;
import io.crops.warmletter.domain.letter.service.MailboxService;
import io.crops.warmletter.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping("/{matchingId}/disconnect")
    public ResponseEntity<BaseResponse<Void>> disconnectMatching(@PathVariable Long matchingId) {
        mailBoxService.disconnectMatching(matchingId);
        return ResponseEntity.ok(BaseResponse.of(null,"매칭 차단 완료"));
    }
}
