package io.crops.warmletter.domain.share.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SharePostLikeResponse {

    private Long likeCount;
    private boolean isLiked;
}