package io.crops.warmletter.domain.eventpost.service;

import io.crops.warmletter.domain.eventpost.dto.request.CreateEventPostRequest;
import io.crops.warmletter.domain.eventpost.dto.response.*;
import io.crops.warmletter.domain.eventpost.entity.EventPost;
import io.crops.warmletter.domain.eventpost.exception.EventPostNotFoundException;
import io.crops.warmletter.domain.eventpost.repository.EventCommentRepository;
import io.crops.warmletter.domain.eventpost.repository.EventPostRepository;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;
import io.crops.warmletter.global.response.PageResponse;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class EventPostService {
    private final EventPostRepository eventPostRepository;
    private final EventCommentRepository eventCommentRepository;

    @Transactional(readOnly = true)
    public Page<EventPostsResponse> getEventPosts(Pageable eventPostPageable) {
        return eventPostRepository.findByIsActiveIsTrue(eventPostPageable);
    }

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

    public Map<String, Long> deleteEventPost(Long eventPostId) {
        EventPost eventPost = eventPostRepository.findByIdAndIsActiveIsTrue(eventPostId).orElseThrow(EventPostNotFoundException::new);
        eventPost.softDelete();
        return Map.of("eventPostId", eventPost.getId());
    }

    @Transactional(readOnly = true)
    public EventPostResponse getUsedEventPost() {
        EventPost eventPost = eventPostRepository.findByIsUsed(true).orElse(null);
        if(eventPost==null) {
            return null;
        }
        return EventPostResponse.builder()
                .eventPostId(eventPost.getId())
                .title(eventPost.getTitle())
                .build();
    }

    @Transactional(readOnly = true)
    public EventPostDetailResponse getEventPostDetail(Long eventPostId, Pageable eventCommentspageable) {
        EventPost eventPost = eventPostRepository.findByIdAndIsActiveIsTrue(eventPostId)
                .orElseThrow(EventPostNotFoundException::new);
        PageResponse<EventCommentsResponse> eventCommentsPageResponse = new PageResponse<>(
                eventCommentRepository.findByEventPostIdWithZipCode(eventPostId, eventCommentspageable));
        return EventPostDetailResponse.builder()
                .eventPostId(eventPost.getId())
                .title(eventPost.getTitle())
                .eventPostComments(eventCommentsPageResponse)
                .build();
    }

    public EventPostStatusResponse updateEventPostIsUsed(Long eventPostId) {
        // true -> fasle (문제 없음)
        // false -> true(true 값을 가진 eventPostId가 있으면 예외처리)
        try {
            EventPost eventPost = eventPostRepository.findById(eventPostId).orElseThrow(EventPostNotFoundException::new);

            if (eventPost.isUsed()) {
                eventPost.isUsedChange(false);
            } else {
                // Used가 true로 변경될 경우, 이미 true인 값이 있는지 확인
                boolean isAlreadyInUse = eventPostRepository.existsByIsUsedTrue();
                if (isAlreadyInUse) {
                    throw new BusinessException(ErrorCode.EVENT_POST_IN_USE);
                }
                eventPost.isUsedChange(true);
            }
            return EventPostStatusResponse.builder()
                    .eventPostId(eventPost.getId())
                    .isUsed(eventPost.isUsed())
                    .build();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.EVENT_POST_IN_USE);
        } catch (PersistenceException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
