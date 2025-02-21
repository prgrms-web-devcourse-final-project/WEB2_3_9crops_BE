package io.crops.warmletter.domain.eventpost.service;

import io.crops.warmletter.domain.eventpost.dto.request.CreateEventPostRequest;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostDetailResponse;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostResponse;
import io.crops.warmletter.domain.eventpost.entity.EventPost;
import io.crops.warmletter.domain.eventpost.exception.EventPostNotFoundException;
import io.crops.warmletter.domain.eventpost.repository.EventPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventPostService {
    private final EventPostRepository eventPostRepository;

    public EventPostResponse createEventPost(CreateEventPostRequest createEventPostRequest) {
        EventPost eventPost = EventPost.builder()
                .title(createEventPostRequest.getTitle())
                .build();
        EventPost saveEventPost = eventPostRepository.save(eventPost);

        return EventPostResponse.builder()
                .eventPostId(saveEventPost.getId())
                .title(saveEventPost.getTitle())
                .build();
    }

    public Map<String, Long> deleteEventPost(long eventPostId) {
        EventPost eventPost = eventPostRepository.findById(eventPostId).orElseThrow(EventPostNotFoundException::new);
        eventPost.softDelete();
        return Map.of("eventPostId", eventPost.getId());
    }

    @Transactional(readOnly = true)
    public EventPostResponse getUsedEventPost() {
        EventPost eventPost = eventPostRepository.findFirstByIsUsed(true);
        return EventPostResponse.builder()
                .eventPostId(eventPost.getId())
                .title(eventPost.getTitle())
                .build();
    }

    public EventPostDetailResponse getEventPostDetail(long eventPostId) {
        return eventPostRepository.findEventPostDetailById(eventPostId);
    }
}
