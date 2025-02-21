package io.crops.warmletter.domain.eventpost.service;

import io.crops.warmletter.domain.eventpost.dto.request.CreateEventPostRequest;
import io.crops.warmletter.domain.eventpost.dto.response.CreateEventPostResponse;
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
public class EventPostService {
    private final EventPostRepository eventPostRepository;

    public CreateEventPostResponse createEventPost(CreateEventPostRequest createEventPostRequest) {
        EventPost eventPost = EventPost.builder()
                .title(createEventPostRequest.getTitle())
                .build();
        EventPost saveEventPost = eventPostRepository.save(eventPost);

        return CreateEventPostResponse.builder()
                .eventPostId(saveEventPost.getId())
                .title(saveEventPost.getTitle())
                .build();
    }

    @Transactional
    public Map<String, Long> deleteEventPost(long eventPostId) {
        EventPost eventPost = eventPostRepository.findById(eventPostId).orElseThrow(EventPostNotFoundException::new);
        eventPost.softDelete();
        return Map.of("eventPostId", eventPost.getId());
    }
}
