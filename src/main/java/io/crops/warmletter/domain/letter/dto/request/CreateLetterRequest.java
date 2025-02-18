package io.crops.warmletter.domain.letter.dto.request;

import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

/**
 * {
 * 	"title": "저 요즘 슬퍼요.." ,
 * 	"message": "블라블라",
 * 	"category": "위로"(or null),
 * 	"paperType": "", - 편지지 종류 뭐뭐있지?
 * 	"font": "" - 글꼴 종류 뭐뭐있지?
 * }
 */
@Getter
public class CreateLetterRequest {

    //받는사람 아이디 -> null이면 첫편지
    public Long receiverId;

    //상위 편지 id
    public Long parentLetterId; //상위 편지 id

    @NotBlank(message = "제목을 입력해주세요.")
    public String title;

    @NotBlank(message = "내용을 입력해주세요.")
    public String content;

    public Category category; //null이면 주고받는 답장 편지

    @NotBlank(message = "편지지 타입을 입력해주세요.")
    public PaperType paperType;

    @NotBlank(message = "글꼴을 입력해주세요.")
    public FontType font;

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
