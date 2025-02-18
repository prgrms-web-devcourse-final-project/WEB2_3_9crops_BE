package io.crops.warmletter.domain.moderation.controller;


import io.crops.warmletter.domain.moderation.dto.request.ModerationRequest;
import io.crops.warmletter.domain.moderation.service.ModerationService;
import io.crops.warmletter.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/moderations")
@RequiredArgsConstructor
public class ModerationController {


    private final ModerationService moderationService;

    @PostMapping
    public ResponseEntity<BaseResponse<String>> createModerationWord(@RequestBody ModerationRequest request) {
        moderationService.saveModerationWord(request);
        return ResponseEntity.ok(BaseResponse.of(request.getWord(), "성공"));
    }

}
