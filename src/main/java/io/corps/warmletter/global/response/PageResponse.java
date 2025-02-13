package io.corps.warmletter.global.response;

import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class PageResponse<T> {
    private final List<T> content;
    private final int currentPage;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.currentPage = page.getNumber() + 1; // 0부터 시작하므로 1을 더해줌
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
    }
}
