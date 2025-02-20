package io.crops.warmletter.domain.eventpost.service;

import io.crops.warmletter.domain.eventpost.dto.request.CreateEventPostRequest;
import io.crops.warmletter.domain.eventpost.dto.response.CreateEventPostResponse;
import io.crops.warmletter.domain.eventpost.entity.EventPost;
import io.crops.warmletter.domain.eventpost.repository.EventPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventPostService {
    private final EventPostRepository eventPostRepository;

    public CreateEventPostResponse createEventPost(CreateEventPostRequest createEventPostRequest) {
        EventPost eventPost = EventPost.builder()
                .title(createEventPostRequest.getTitle())
                .content(createEventPostRequest.getContent())
                .build();
        EventPost saveEventPost = eventPostRepository.save(eventPost);

        return CreateEventPostResponse.builder()
                .eventPostId(saveEventPost.getId())
                .title(saveEventPost.getTitle())
                .content(saveEventPost.getContent())
                .build();
    }
}
