package io.crops.warmletter.domain.eventpost.repository;

import io.crops.warmletter.domain.eventpost.dto.response.EventPostDetailResponse;

public interface EventPostCustomRepository {
    EventPostDetailResponse findEventPostDetailById(long eventPostId);
}
