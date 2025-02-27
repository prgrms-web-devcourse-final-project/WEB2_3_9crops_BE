package io.crops.warmletter.domain.eventpost.controller;

import io.crops.warmletter.domain.eventpost.dto.request.CreateEventCommentRequest;
import io.crops.warmletter.domain.eventpost.dto.request.CreateEventPostRequest;
import io.crops.warmletter.domain.eventpost.dto.response.EventCommentResponse;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostDetailResponse;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostResponse;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostStatusResponse;
import io.crops.warmletter.domain.eventpost.service.EventCommentService;
import io.crops.warmletter.domain.eventpost.service.EventPostService;
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
@Tag(name = "EventPost", description = "이벤트(롤링페이퍼) 게시판 관련 API")
public class EventPostController {
    private final EventPostService eventPostService;

    @Operation(summary = "이벤트 게시판 생성", description = "새로운 이벤트 게시판을 생성합니다.")
    @PostMapping("/admin/event-posts")
    public ResponseEntity<BaseResponse<EventPostResponse>> createEventPost(
            @RequestBody @Valid CreateEventPostRequest createEventPostRequest){
        return ResponseEntity.ok(BaseResponse.of(eventPostService.createEventPost(createEventPostRequest),"게시판 생성 성공"));
    }

    @Operation(summary = "이벤트 게시판 삭제", description = "이벤트 게시판을 삭제합니다. (임시 기능)")
    @DeleteMapping("/admin/event-posts/{eventPostId}")
    public ResponseEntity<BaseResponse<Map<String,Long>>> deleteEventPost(
            @Parameter(description = "삭제할 게시판 ID", required = true) @PathVariable Long eventPostId){
        return ResponseEntity.ok(BaseResponse.of(eventPostService.deleteEventPost(eventPostId),"게시판 삭제 성공"));
    }

    @Operation(summary = "이벤트 게시판 조회(사용 중)", description = "현재 사용 중인 이벤트 게시판을 조회합니다.")
    @GetMapping("/event-posts")
    public ResponseEntity<BaseResponse<EventPostResponse>> getUsedEventPost(){
        return ResponseEntity.ok(BaseResponse.of(eventPostService.getUsedEventPost(),"게시판 조회(사용중) 성공"));
    }

    @Operation(summary = "이벤트 게시판 조회(개별)", description = "특정 이벤트 게시판을 조회합니다.")
    @GetMapping("/event-posts/{eventPostId}")
    public ResponseEntity<BaseResponse<EventPostDetailResponse>> getEventPostDetail(
            @Parameter(description = "조회할 게시판 ID", required = true) @PathVariable Long eventPostId){
        return ResponseEntity.ok(BaseResponse.of(eventPostService.getEventPostDetail(eventPostId),"게시판 조회(개별) 성공"));
    }

    @Operation(summary = "이벤트 게시판 사용여부 변경", description = "특정 이벤트 게시판 사용여부(on/off)을 변경")
    @PatchMapping("/admin/event-posts/{eventPostId}/status")
    public ResponseEntity<BaseResponse<EventPostStatusResponse>> updateEventPostIsUsed(
            @Parameter(description = "사용여부 바꿀 게시판 ID", required = true) @PathVariable Long eventPostId){
        return ResponseEntity.ok(BaseResponse.of(eventPostService.updateEventPostIsUsed(eventPostId),"게시판 사용여부 변경 성공"));
    }

}
