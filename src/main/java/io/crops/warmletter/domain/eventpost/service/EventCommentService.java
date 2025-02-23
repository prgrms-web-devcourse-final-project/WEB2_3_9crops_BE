package io.crops.warmletter.domain.eventpost.service;

import io.crops.warmletter.domain.eventpost.dto.request.CreateEventCommentRequest;
import io.crops.warmletter.domain.eventpost.dto.response.EventCommentResponse;
import io.crops.warmletter.domain.eventpost.entity.EventComment;
import io.crops.warmletter.domain.eventpost.entity.EventPost;
import io.crops.warmletter.domain.eventpost.exception.EventPostNotFoundException;
import io.crops.warmletter.domain.eventpost.repository.EventCommentRepository;
import io.crops.warmletter.domain.eventpost.repository.EventPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EventCommentService {
    public final EventCommentRepository eventCommentRepository;
    public final EventPostRepository eventPostRepository;

    public EventCommentResponse createEventComment(CreateEventCommentRequest createEventCommentRequest, long eventPostId) {
        if(!eventPostRepository.existsById(eventPostId)) {
            throw new EventPostNotFoundException();
        }

        EventComment eventComment = EventComment.builder()
                .eventPostId(eventPostId)
                .writerId(1L)   // TODO: 실제 인증 정보를 사용하도록 변경
                .content(createEventCommentRequest.getContent())
                .build();

        EventComment saveEventComment = eventCommentRepository.save(eventComment);

        return EventCommentResponse.builder()
                .commentId(saveEventComment.getId())
                .content(saveEventComment.getContent())
                .build();
    }
}
