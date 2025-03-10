package io.crops.warmletter.domain.share.dto.response;
import io.crops.warmletter.domain.share.entity.SharePost;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SharePostDeleteResponse {

    private SharePost sharePost;
    private Long requesterId;

}
