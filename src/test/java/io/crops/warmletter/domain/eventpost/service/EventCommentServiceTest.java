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
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.AuthException;
import io.crops.warmletter.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class EventCommentServiceTest {
    @Mock
    private EventCommentRepository eventCommentRepository;

    @Mock
    private EventPostRepository eventPostRepository;

    @Mock
    private AuthFacade authFacade;

    @InjectMocks
    private EventCommentService eventCommentService;

    @Test
    @DisplayName("게시판 댓글 생성 성공")
    void create_eventComment_success(){
        //given
        Long writerId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(writerId);

        CreateEventCommentRequest createEventCommentRequest = CreateEventCommentRequest.builder()
                .content("내용")
                .build();

        EventComment eventComment = EventComment.builder()
                .eventPostId(1L)
                .writerId(writerId)
                .content(createEventCommentRequest.getContent())
                .build();
        ReflectionTestUtils.setField(eventComment, "id", 1L);

        when(eventPostRepository.existsById(1L)).thenReturn(true);
        when(eventCommentRepository.save(any(EventComment.class))).thenReturn(eventComment);

        //when
        EventCommentResponse eventCommentResponse = eventCommentService.createEventComment(createEventCommentRequest,1L);

        //then
        assertEquals(1, eventCommentResponse.getCommentId());
        assertEquals("내용", eventCommentResponse.getContent());

        verify(eventCommentRepository).save(any(EventComment.class));
    }

    @Test
    @DisplayName("게시판 댓글 생성 실패 - 조건이 일치하는 게시판 없음")
    void create_eventComment_eventPostNotFound(){
        //given
        CreateEventCommentRequest createEventCommentRequest = CreateEventCommentRequest.builder()
                .content("내용")
                .build();

        when(eventPostRepository.existsById(1L)).thenReturn(false);

        //when
        BusinessException exception = assertThrows(EventPostNotFoundException.class, ()-> eventCommentService.createEventComment(createEventCommentRequest,1L));

        //then
        assertEquals(ErrorCode.EVENT_POST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시판 댓글 생성 실패 - 인증 정보 없음")
    void create_eventComment_authNotFound(){
        //given
        when(authFacade.getCurrentUserId()).thenReturn(null);

        CreateEventCommentRequest createEventCommentRequest = CreateEventCommentRequest.builder()
                .content("내용")
                .build();

        when(eventPostRepository.existsById(1L)).thenReturn(true);

        //when
        AuthException exception = assertThrows(UnauthorizedException.class, ()-> eventCommentService.createEventComment(createEventCommentRequest,1L));

        //then
        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시판 댓글 삭제 성공")
    void delete_eventComment_success(){
        //given
        Long eventCommentId = 1L;
        Long writerId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(writerId);

        EventComment eventComment = EventComment.builder()
                .eventPostId(eventCommentId)
                .writerId(1L)
                .content("내용")
                .build();
        ReflectionTestUtils.setField(eventComment, "id", eventCommentId);

        when(eventCommentRepository.findByIdAndWriterId(any(Long.class),any(Long.class))).thenReturn(Optional.of(eventComment));

        //when
        Map<String,Long> deleteEventComment = eventCommentService.deleteEventComment(eventCommentId);

        //then
        assertEquals(1, deleteEventComment.get("commentId"));
        assertFalse(eventComment.isActive());
    }

    @Test
    @DisplayName("게시판 댓글 삭제 실패 - 존재하지 않는 게시판")
    void delete_eventComment_notFound() {
        // given
        when(eventCommentRepository.findByIdAndWriterId(any(Long.class),any(Long.class))).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(EventCommentNotFoundException.class, () -> eventCommentService.deleteEventComment(999L));

        // then
        assertEquals(ErrorCode.EVENT_COMMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시판 댓글 삭제 실패 - 이미 삭제한 댓글")
    void update_EventPostIsUsedToFalse_AlreadyInUsed(){
        //given
        Long eventCommentId = 1L;
        Long writerId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(writerId);

        EventComment eventComment = EventComment.builder()
                .eventPostId(1L)
                .writerId(writerId)
                .content("내용")
                .build();
        ReflectionTestUtils.setField(eventComment, "isActive", false);

        when(eventCommentRepository.findByIdAndWriterId(any(Long.class),any(Long.class))).thenReturn(Optional.of(eventComment));

        BusinessException exception = assertThrows(EventCommentNotFoundException.class, ()-> eventCommentService.deleteEventComment(eventCommentId));

        //then
        assertEquals(ErrorCode.EVENT_COMMENT_NOT_FOUND, exception.getErrorCode());
    }
}