package io.crops.warmletter.domain.letter.dto.request;

import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import lombok.Getter;

/**
 * {
 * 	"title": "저 요즘 슬퍼요.." ,
 * 	"message": "블라블라",
 * 	"category": "위로"(or null),
 * 	"paperType": "", - 편지지 종류 뭐뭐있지?
 * 	"font": "" - 글꼴 종류 뭐뭐있지?
 * }
 *
 */
@Getter
public class LettersCreateDTO {

    public String title;
    public String content;
    private Category category;
    private PaperType paperType;
    private FontType font;
}
