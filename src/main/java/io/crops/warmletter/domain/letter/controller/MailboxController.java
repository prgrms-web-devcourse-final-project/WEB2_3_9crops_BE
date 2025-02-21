package io.crops.warmletter.domain.letter.controller;

import io.crops.warmletter.domain.letter.dto.response.MailboxResponse;
import io.crops.warmletter.domain.letter.service.MailBoxService;
import io.crops.warmletter.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MailboxController {

    private final MailBoxService mailBoxService;

    /**
     * 내 편지함 목록 조회
     */
    @GetMapping("/api/mailbox")
    public BaseResponse<Object> getMailbox() {
        List<MailboxResponse> mailbox = mailBoxService.getMailbox();
        return BaseResponse.of(mailbox, "편지함 조회 완료");
    }
}
