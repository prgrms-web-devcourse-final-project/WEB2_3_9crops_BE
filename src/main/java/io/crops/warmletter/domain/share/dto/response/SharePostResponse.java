package io.crops.warmletter.domain.share.dto.response;
import io.crops.warmletter.domain.share.entity.SharePost;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
public class SharePostResponse {

    private Long shareProposalId;
    private String title;
    private String content;
    private LocalDateTime createdAt;

    public SharePostResponse(SharePost sharePost) {
        this.shareProposalId = sharePost.getShareProposalId();
        this.title = sharePost.getTitle();
        this.content = sharePost.getContent();
        this.createdAt = sharePost.getCreatedAt();
    }

}
