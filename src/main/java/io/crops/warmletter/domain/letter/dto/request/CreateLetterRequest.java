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

    @NotBlank(message = "제목을 입력해주세요.")
    public String title;

    @NotBlank(message = "내용을 입력해주세요.")
    public String content;

    @NotBlank(message = "카테고리를 입력해주세요.")
    private Category category;

    @NotBlank(message = "카테고리를 입력해주세요.")
    private PaperType paperType;

    @NotBlank(message = "카테고리를 입력해주세요.")
    private FontType font;

    @Builder
    public CreateLetterRequest(String title, String content, Category category, PaperType paperType, FontType font) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.paperType = paperType;
        this.font = font;
    }
}
