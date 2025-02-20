package io.crops.warmletter.domain.share.dto.response;
import io.crops.warmletter.domain.share.entity.SharePost;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SharePostResponse {

    private Long shareProposalId;
    private String content;
    private boolean isActive;
    private LocalDateTime createdAt;
    // writerId와 receiverId가 필요하다.
    // 주말 수정


    public SharePostResponse(SharePost sharePost) {
        this.shareProposalId = sharePost.getShareProposalId();
        this.content = sharePost.getContent();
        this.isActive = sharePost.isActive();
        this.createdAt = sharePost.getCreatedAt();
    }

}
