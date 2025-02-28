package io.crops.warmletter.global.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PageableConverter {

    public static Pageable convertToPageable(Pageable pageable) {
        return PageRequest.of(
                pageable.getPageNumber() > 0 ? pageable.getPageNumber() - 1 : 0,
                pageable.getPageSize(),
                pageable.getSort()
        );
    }
}
