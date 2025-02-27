package io.crops.warmletter.domain.letter.dto.request;

import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateLetterRequest {

//    private Long writerId; // todo 테스트시에만 작성자 추가, 사용자 id 가져올때는 제거 해야 함.

    //받는사람 아이디 -> null이면 첫편지
    private Long receiverId;

    //상위 편지 id -> null이면 첫편지
    private Long parentLetterId; //상위 편지 id -> 프론트는 편지 아이디를 여기에 넣어주면 됨.

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    private Category category;

    private PaperType paperType;

    private FontType fontType;

    private Long matchingId;

    @Builder
    public CreateLetterRequest(Long receiverId, Long parentLetterId, String title,
                               String content, Category category,
                               PaperType paperType, FontType font, Long matchingId) {
        this.receiverId = receiverId;
        this.parentLetterId = parentLetterId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.paperType = paperType;
        this.fontType = font;
        this.matchingId = matchingId;
    }

    public void updateMatchingId(Long matchingId) {
        this.matchingId = matchingId;
    }
}
