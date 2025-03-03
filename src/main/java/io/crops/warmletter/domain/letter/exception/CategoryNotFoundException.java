package io.crops.warmletter.domain.letter.exception;
import io.crops.warmletter.global.error.exception.BusinessException;
import static io.crops.warmletter.global.error.common.ErrorCode.CATEGORY_NOT_FOUND;

public class CategoryNotFoundException extends BusinessException {
    public CategoryNotFoundException() {
        super(CATEGORY_NOT_FOUND);
    }
}
