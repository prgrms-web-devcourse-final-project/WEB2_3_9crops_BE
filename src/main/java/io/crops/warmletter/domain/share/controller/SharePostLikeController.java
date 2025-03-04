package io.crops.warmletter.domain.share.controller;

import io.crops.warmletter.domain.share.dto.request.SharePostLikeRequest;
import io.crops.warmletter.domain.share.dto.response.SharePostLikeResponse;
import io.crops.warmletter.domain.share.service.SharePostLikeService;
import io.crops.warmletter.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "SharePostLike", description = "공유 게시글 좋아요 관련 API")
public class SharePostLikeController {

    private final SharePostLikeService sharePostLikeService;

    @Operation(summary = "공유 게시글 좋아요 토글", description = "공유 게시글에 대한 좋아요를 추가하거나 취소합니다.")
    @PostMapping("/share-posts/{sharePostId}/likes")
    public ResponseEntity<BaseResponse<Void>> toggleLike(@PathVariable(name = "sharePostId") Long sharePostId) {
        sharePostLikeService.toggleLike(sharePostId);
        return ResponseEntity.ok()
                .body(new BaseResponse<>(null, "좋아요 요청 성공"));
    }


    @Operation(summary = "공유 게시글 좋아요 정보 조회", description = "공유 게시글의 좋아요 수와 현재 사용자의 좋아요 상태를 조회합니다.")
    @GetMapping("/share-posts/{sharePostId}/likes")
    public ResponseEntity<SharePostLikeResponse> getLikeCountAndStatus(@PathVariable(name = "sharePostId") Long sharePostId) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(sharePostLikeService.getLikeCountAndStatus(sharePostId));
    }

}
