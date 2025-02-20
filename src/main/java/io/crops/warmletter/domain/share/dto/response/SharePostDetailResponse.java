package io.crops.warmletter.domain.share.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SharePostDetailResponse {
    private Long sharePostId;
    private String zipCode;
    private String sharePostContent;
    private List<ShareLetterPostResponse> letters;

    @Builder
    public SharePostDetailResponse(Long id, String zipCode, String sharePostContent, List<ShareLetterPostResponse> letters) {
        this.sharePostId = id;
        this.zipCode = zipCode;
        this.sharePostContent = sharePostContent;
        this.letters = letters != null ? letters : new ArrayList<>();
    }

}