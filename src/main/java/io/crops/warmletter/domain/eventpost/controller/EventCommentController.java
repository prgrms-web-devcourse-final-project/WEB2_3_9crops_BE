package io.crops.warmletter.domain.eventpost.controller;

import io.crops.warmletter.domain.eventpost.dto.request.CreateEventCommentRequest;
import io.crops.warmletter.domain.eventpost.dto.response.EventCommentResponse;
import io.crops.warmletter.domain.eventpost.service.EventCommentService;
import io.crops.warmletter.global.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EventCommentController {
    private final EventCommentService eventCommentService;

    // 이벤트 게시판 댓글 생성
    @PostMapping("/event-posts/{eventPostId}/comments")
    public ResponseEntity<BaseResponse<EventCommentResponse>> createEventComment(@RequestBody @Valid CreateEventCommentRequest createEventCommentRequest, @PathVariable Long eventPostId){
        return ResponseEntity.ok(BaseResponse.of(eventCommentService.createEventComment(createEventCommentRequest,eventPostId),"댓글 생성 성공"));
    }
}
