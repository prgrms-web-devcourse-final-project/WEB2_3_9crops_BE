 package io.crops.warmletter.domain.share.controller;

import io.crops.warmletter.domain.share.dto.request.SharePostLikeRequest;
import io.crops.warmletter.domain.share.service.SharePostLikeService;
import io.crops.warmletter.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

 @RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SharePostLikeController {

    private final SharePostLikeService sharePostLikeService;

     @PostMapping("/share-posts/{sharePostId}/likes")
     public ResponseEntity<BaseResponse<Void>> toggleLike(@PathVariable(name = "sharePostId") Long sharePostId,
                                                          @RequestBody SharePostLikeRequest request) {
         sharePostLikeService.toggleLike(sharePostId, request.getMemberId());
         return ResponseEntity.ok()
                 .body(new BaseResponse<>(null, "좋아요 요청 성공"));
     }
}
