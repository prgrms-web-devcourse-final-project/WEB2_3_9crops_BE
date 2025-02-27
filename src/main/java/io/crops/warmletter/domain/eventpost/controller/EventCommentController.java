package io.crops.warmletter.domain.eventpost.controller;

import io.crops.warmletter.domain.eventpost.dto.request.CreateEventCommentRequest;
import io.crops.warmletter.domain.eventpost.dto.response.EventCommentResponse;
import io.crops.warmletter.domain.eventpost.service.EventCommentService;
import io.crops.warmletter.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "EventComment", description = "이벤트(롤링페이퍼) 게시판 댓글 관련 API")
public class EventCommentController {
    private final EventCommentService eventCommentService;

    @Operation(summary = "댓글 생성", description = "특정 이벤트 게시판 글에 댓글을 작성합니다.")
    @PostMapping("/event-posts/{eventPostId}/comments")
    public ResponseEntity<BaseResponse<EventCommentResponse>> createEventComment(
            @RequestBody @Valid CreateEventCommentRequest createEventCommentRequest,
            @Parameter(description = "이벤트 게시글 ID", required = true) @PathVariable Long eventPostId) {
        return ResponseEntity.ok(BaseResponse.of(eventCommentService.createEventComment(createEventCommentRequest, eventPostId), "댓글 생성 성공"));
    }

    @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다.")
    @DeleteMapping("/event-posts/comments/{commentId}")
    public ResponseEntity<BaseResponse<Map<String, Long>>> deleteEventComment(
            @Parameter(description = "삭제할 댓글 ID", required = true) @PathVariable Long commentId) {
        return ResponseEntity.ok(BaseResponse.of(eventCommentService.deleteEventComment(commentId), "댓글 삭제 성공"));
    }
}