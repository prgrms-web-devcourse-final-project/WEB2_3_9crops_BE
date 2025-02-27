package io.crops.warmletter.domain.share.controller;

import io.crops.warmletter.domain.share.dto.response.SharePostLikeResponse;
import io.crops.warmletter.domain.share.service.SharePostLikeService;
import io.crops.warmletter.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SharePostLikeController {

    private final SharePostLikeService sharePostLikeService;

    @PostMapping("/share-posts/{sharePostId}/likes")
    public ResponseEntity<BaseResponse<Void>> toggleLike(@PathVariable(name = "sharePostId") Long sharePostId) {
        sharePostLikeService.toggleLike(sharePostId);
        return ResponseEntity.ok()
                .body(new BaseResponse<>(null, "좋아요 요청 성공"));
    }



    @GetMapping("/share-posts/{sharePostId}/likes")
    public ResponseEntity<SharePostLikeResponse> getLikeCountAndStatus(@PathVariable(name = "sharePostId") Long sharePostId) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(sharePostLikeService.getLikeCountAndStatus(sharePostId));
    }

}
