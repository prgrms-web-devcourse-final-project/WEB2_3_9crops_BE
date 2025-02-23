package io.crops.warmletter.domain.share.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class SharePostDetailResponse {
    private Long sharePostId;
    private String zipCode;
    private String sharePostContent;
    private List<ShareLetterPostResponse> letters;



}