package io.crops.warmletter.domain.share.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SharePostLikeRequest {

    private Long sharePostId;
    private Long memberId;

}
