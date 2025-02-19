package io.crops.warmletter.domain.letter.dto.request;

import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateLetterRequest {

    //받는사람 아이디 -> null이면 첫편지
    private Long receiverId;

    //상위 편지 id -> null이면 첫편지
    private Long parentLetterId; //상위 편지 id

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    private Category category;

    private PaperType paperType;

    private FontType font;

    @Builder
    public CreateLetterRequest(Long receiverId, Long parentLetterId, String title, String content, Category category, PaperType paperType, FontType font) {
        this.receiverId = receiverId;
        this.parentLetterId = parentLetterId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.paperType = paperType;
        this.font = font;
    }
}
