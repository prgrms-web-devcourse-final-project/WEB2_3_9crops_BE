package io.crops.warmletter.domain.share.dto.response;

import lombok.*;
import java.util.List;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ShareLetterDetailResponse {

    private Long sharePostId;
    private String writerZipCode;
    private String sharePostContent;
    private List<ShareLetterPostResponse> letters;


}

