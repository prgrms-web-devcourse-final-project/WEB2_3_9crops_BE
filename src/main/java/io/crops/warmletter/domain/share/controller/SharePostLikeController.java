 package io.crops.warmletter.domain.share.controller;

import io.crops.warmletter.domain.share.dto.request.SharePostLikeRequest;
import io.crops.warmletter.domain.share.service.SharePostLikeService;
import io.crops.warmletter.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SharePostLikeController {

    private final SharePostLikeService sharePostLikeService;

    @PostMapping("/share-post-like")
    public ResponseEntity<BaseResponse<Void>> addLike(@RequestBody SharePostLikeRequest request) {
        sharePostLikeService.addLike(request);
        return ResponseEntity.ok()
                .body(new BaseResponse<>(null, "좋아요 요청 성공"));
    }

}
