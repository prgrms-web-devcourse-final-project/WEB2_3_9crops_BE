package io.crops.warmletter.domain.eventpost.controller;

import io.crops.warmletter.domain.eventpost.dto.request.CreateEventCommentRequest;
import io.crops.warmletter.domain.eventpost.dto.response.EventCommentResponse;
import io.crops.warmletter.domain.eventpost.service.EventCommentService;
import io.crops.warmletter.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "이벤트 게시글 댓글", description = "이벤트 게시글에 대한 댓글 관리 API")
public class EventCommentController {
    private final EventCommentService eventCommentService;

    @PostMapping("/event-posts/{eventPostId}/comments")
    @Operation(summary = "이벤트 게시글 댓글 생성", description = "특정 이벤트 게시글에 로그인한 사용자의 새로운 댓글을 생성합니다.")
    public ResponseEntity<BaseResponse<EventCommentResponse>> createEventComment(@RequestBody @Valid CreateEventCommentRequest createEventCommentRequest, @PathVariable Long eventPostId){
        return ResponseEntity.ok(BaseResponse.of(eventCommentService.createEventComment(createEventCommentRequest,eventPostId),"댓글 생성 성공"));
    }

    @DeleteMapping("/event-posts/comments/{commentId}")
    @Operation(summary = "이벤트 게시글 댓글 삭제", description = "특정 댓글 ID를 통해 이벤트 게시글에서 로그인한 사용자의 댓글을 삭제합니다.")
    public ResponseEntity<BaseResponse<Map<String,Long>>> deleteEventComment(@PathVariable Long commentId){
        return ResponseEntity.ok(BaseResponse.of(eventCommentService.deleteEventComment(commentId),"댓글 삭제 성공"));
    }
}
