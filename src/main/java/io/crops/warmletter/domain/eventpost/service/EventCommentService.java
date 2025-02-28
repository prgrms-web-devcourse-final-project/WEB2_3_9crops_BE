package io.crops.warmletter.domain.eventpost.service;

import io.crops.warmletter.domain.auth.exception.UnauthorizedException;
import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.eventpost.dto.request.CreateEventCommentRequest;
import io.crops.warmletter.domain.eventpost.dto.response.EventCommentResponse;
import io.crops.warmletter.domain.eventpost.entity.EventComment;
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

    public EventCommentResponse createEventComment(CreateEventCommentRequest createEventCommentRequest, Long eventPostId) {
        if(!eventPostRepository.existsById(eventPostId)) {
            throw new EventPostNotFoundException();
        }

        Long writerId = authFacade.getCurrentUserId();

        EventComment eventComment = EventComment.builder()
                .eventPostId(eventPostId)
                .writerId(writerId)
                .content(createEventCommentRequest.getContent())
                .build();

        EventComment saveEventComment = eventCommentRepository.save(eventComment);

        return EventCommentResponse.builder()
                .commentId(saveEventComment.getId())
                .content(saveEventComment.getContent())
                .build();
    }

    public Map<String,Long> deleteEventComment(Long eventCommentId) {
        Long writerId = authFacade.getCurrentUserId();
        EventComment eventComment = eventCommentRepository.findByIdAndWriterId(eventCommentId,writerId).orElseThrow(EventCommentNotFoundException::new);
        if(!eventComment.isActive()){
            throw new EventCommentNotFoundException();
        }
        eventComment.softDelete();
        return Map.of("commentId",eventComment.getId());
    }
}
