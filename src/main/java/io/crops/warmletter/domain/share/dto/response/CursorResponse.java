package io.crops.warmletter.domain.share.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class CursorResponse<T> {
    private List<T> data;
    private Long nextCursor;
    private boolean hasNext;

    public CursorResponse(List<T> data, Long nextCursor, boolean hasNext) {
        this.data = data;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }
}

