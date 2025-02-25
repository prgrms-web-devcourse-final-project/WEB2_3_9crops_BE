package io.crops.warmletter.domain.eventpost.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.eventpost.dto.request.CreateEventCommentRequest;
import io.crops.warmletter.domain.eventpost.dto.response.EventCommentResponse;
import io.crops.warmletter.domain.eventpost.entity.EventComment;
import io.crops.warmletter.domain.eventpost.entity.EventPost;
import io.crops.warmletter.domain.eventpost.exception.EventCommentNotFoundException;
import io.crops.warmletter.domain.eventpost.exception.EventPostNotFoundException;
import io.crops.warmletter.domain.eventpost.repository.EventCommentRepository;
import io.crops.warmletter.domain.eventpost.repository.EventPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class EventCommentService {
    private final AuthFacade authFacade;
    private final EventCommentRepository eventCommentRepository;
    private final EventPostRepository eventPostRepository;

    public EventCommentResponse createEventComment(CreateEventCommentRequest createEventCommentRequest, long eventPostId) {
        if(!eventPostRepository.existsById(eventPostId)) {
            throw new EventPostNotFoundException();
        }

        EventComment eventComment = EventComment.builder()
                .eventPostId(eventPostId)
                .writerId(1L)   // TODO: authFacade.getCurrentUserId();
                .content(createEventCommentRequest.getContent())
                .build();

        EventComment saveEventComment = eventCommentRepository.save(eventComment);

        return EventCommentResponse.builder()
                .commentId(saveEventComment.getId())
                .content(saveEventComment.getContent())
                .build();
    }

    public Map<String,Long> deleteEventComment(long eventCommentId) {
        EventComment eventComment = eventCommentRepository.findById(eventCommentId).orElseThrow(EventCommentNotFoundException::new);
        eventComment.softDelete();
        return Map.of("commentId",eventComment.getId());
    }
}
