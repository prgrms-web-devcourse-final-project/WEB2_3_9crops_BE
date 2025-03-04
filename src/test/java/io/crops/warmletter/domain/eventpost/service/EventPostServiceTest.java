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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class EventPostServiceTest {
    @Mock
    private EventPostRepository eventPostRepository;

    @Mock
    private EventCommentRepository eventCommentRepository;

    @InjectMocks
    private EventPostService eventPostService;

    @Test
    @DisplayName("게시판 전체 조회 성공")
    void get_eventPosts_success() throws Exception {
        //given
        EventPostsResponse eventPostsResponse1 = EventPostsResponse.builder().eventPostId(1L).title("제목").build();
        EventPostsResponse eventPostsResponse2 = EventPostsResponse.builder().eventPostId(2L).title("제목").build();
        ReflectionTestUtils.setField(eventPostsResponse2, "isUsed", true);

        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<EventPostsResponse> eventPosts = List.of(eventPostsResponse1, eventPostsResponse2);
        Page<EventPostsResponse> eventPostsPage = new PageImpl<>(eventPosts, pageable, eventPosts.size());

        when(eventPostRepository.findByIsActiveIsTrue(any(Pageable.class))).thenReturn(eventPostsPage);

        //when
        Page<EventPostsResponse> eventPostsResponse = eventPostService.getEventPosts(pageable);

        //then
        assertNotNull(eventPostsResponse);
        assertEquals(eventPostsResponse1.getEventPostId(), eventPostsResponse.getContent().get(0).getEventPostId());
        assertEquals(eventPostsResponse1.getTitle(), eventPostsResponse.getContent().get(0).getTitle());
        assertEquals(eventPostsResponse1.isUsed(), eventPostsResponse.getContent().get(0).isUsed());
        assertEquals(eventPostsResponse2.getEventPostId(), eventPostsResponse.getContent().get(1).getEventPostId());
        assertEquals(eventPostsResponse2.getTitle(), eventPostsResponse.getContent().get(1).getTitle());
        assertEquals(eventPostsResponse2.isUsed(), eventPostsResponse.getContent().get(1).isUsed());
    }



    @Test
    @DisplayName("게시판 생성 성공")
    void create_eventPost_success(){
        //given
        CreateEventPostRequest createEventPostRequest = CreateEventPostRequest.builder()
                .title("제목")
                .build();

        EventPost eventPost = EventPost.builder()
                .title(createEventPostRequest.getTitle())
                .build();

        when(eventPostRepository.save(any(EventPost.class))).thenReturn(eventPost);

        //when
        EventPostResponse eventPostResponse = eventPostService.createEventPost(createEventPostRequest);

        //then
        assertEquals("제목", eventPostResponse.getTitle());

        verify(eventPostRepository).save(any(EventPost.class));
    }

    @Test
    @DisplayName("게시판 삭제 성공")
    void delete_eventPost_success(){
        //given
        Long eventPostId = 1L;

        EventPost eventPost = EventPost.builder().title("제목").build();
        ReflectionTestUtils.setField(eventPost, "id", eventPostId);

        when(eventPostRepository.findById(any(Long.class))).thenReturn(Optional.of(eventPost));

        //when
        Map<String,Long> deleteEventPostResponse = eventPostService.deleteEventPost(eventPostId);

        //then
        assertEquals(1, deleteEventPostResponse.get("eventPostId"));
        assertFalse(eventPost.isUsed());

    }


    @Test
    @DisplayName("게시판 삭제 실패 - 존재하지 않는 게시판")
    void delete_eventPost_notFound() {
        // given
        when(eventPostRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(EventPostNotFoundException.class, () -> eventPostService.deleteEventPost(999L));

        // then
        assertEquals(ErrorCode.EVENT_POST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용중인 게시판 조회 성공")
    void get_usedEventPost_success(){
        //given
        Long eventPostId = 1L;

        EventPost eventPost = EventPost.builder().title("제목").build();
        ReflectionTestUtils.setField(eventPost, "id", eventPostId);

        when(eventPostRepository.findByIsUsed(true)).thenReturn(Optional.of(eventPost));

        //when
        EventPostResponse eventPostResponse = eventPostService.getUsedEventPost();

        //then
        assertNotNull(eventPostResponse);
        assertEquals(1, eventPostResponse.getEventPostId());
        assertEquals("제목", eventPostResponse.getTitle());
    }

    @Test
    @DisplayName("사용중인 게시판 조회 실패 - 조건이 일치하는 게시판 없음")
    void get_usedEventPost_isReadFalse(){
        //given
        when(eventPostRepository.findByIsUsed(true)).thenReturn(Optional.empty());
        //when
        EventPostResponse eventPostResponse = eventPostService.getUsedEventPost();

        //then
        assertNull(eventPostResponse);

        verify(eventPostRepository).findByIsUsed(true);
    }

    @Test
    @DisplayName("게시판 조회(개별) 성공")
    void get_eventPost_success(){
        // given
        Long eventPostId = 1L;
        EventPost eventPost = EventPost.builder().title("제목").build();
        ReflectionTestUtils.setField(eventPost, "id", eventPostId);


        EventCommentsResponse comment1 = EventCommentsResponse.builder().commentId(1L).zipCode("11111").content("내용1").build();
        EventCommentsResponse comment2 = EventCommentsResponse.builder().commentId(2L).zipCode("22222").content("내용2").build();

        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<EventCommentsResponse> comments = List.of(comment2, comment1);


        Page<EventCommentsResponse> eventCommentsPage = new PageImpl<>(comments, pageable, comments.size());
        PageResponse<EventCommentsResponse> eventCommentsResponse = new PageResponse<>(eventCommentsPage);
        when(eventPostRepository.findByIdAndIsActiveIsTrue(any(Long.class))).thenReturn(Optional.of(eventPost));
        when(eventCommentRepository.findByEventPostIdWithZipCode(any(Long.class),any(Pageable.class))).thenReturn(eventCommentsPage);

        // when
        EventPostDetailResponse eventPostDetailResponse = eventPostService.getEventPostDetail(1L,pageable);

        // then
        assertEquals("제목", eventPostDetailResponse.getTitle());
        assertNotNull(eventPostDetailResponse.getEventPostComments());
        assertEquals(comment2.getCommentId(), eventPostDetailResponse.getEventPostComments().getContent().get(0).getCommentId());
        assertEquals(comment2.getZipCode(), eventPostDetailResponse.getEventPostComments().getContent().get(0).getZipCode());
        assertEquals(comment2.getContent(), eventPostDetailResponse.getEventPostComments().getContent().get(0).getContent());
        assertEquals(1, eventPostDetailResponse.getEventPostComments().getCurrentPage());
        assertEquals(1,eventPostDetailResponse.getEventPostComments().getSize());
        assertEquals(2,eventPostDetailResponse.getEventPostComments().getTotalElements());
        assertEquals(2,eventPostDetailResponse.getEventPostComments().getTotalPages());
    }

    @Test
    @DisplayName("게시판 조회(개별) 실패 - 일치하는 eventPostId 없음")
    void get_eventPost_notFound(){
        //given
        when(eventPostRepository.findByIdAndIsActiveIsTrue(any(Long.class))).thenThrow(new EventPostNotFoundException());
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"));

        //when
        BusinessException exception = assertThrows(EventPostNotFoundException.class, ()-> eventPostService.getEventPostDetail(999L,pageable));

        //then
        assertEquals(ErrorCode.EVENT_POST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시판 사용여부 변경 성공 - 사용중에서 미사용")
    void update_eventPostIsUsedToFalse_success(){
        //given
        Long eventPostId = 1L;

        EventPost eventPost = EventPost.builder().title("제목").build();
        ReflectionTestUtils.setField(eventPost, "id", eventPostId);
        ReflectionTestUtils.setField(eventPost, "isUsed", true);

        when(eventPostRepository.findById(any(Long.class))).thenReturn(Optional.of(eventPost));

        //when
        EventPostStatusResponse eventPostStatusResponse = eventPostService.updateEventPostIsUsed(eventPostId);

        //then
        assertEquals(1, eventPostStatusResponse.getEventPostId());
        assertFalse(eventPostStatusResponse.isUsed());
    }

    @Test
    @DisplayName("게시판 사용여부 변경 성공 - 미사용에서 사용중")
    void update_eventPostIsUsedToTrue_success(){
        //given
        Long eventPostId = 1L;

        EventPost eventPost = EventPost.builder().title("제목").build();
        ReflectionTestUtils.setField(eventPost, "id", eventPostId);

        when(eventPostRepository.findById(any(Long.class))).thenReturn(Optional.of(eventPost));
        when(eventPostRepository.existsByIsUsedTrue()).thenReturn(false);

        //when
        EventPostStatusResponse eventPostStatusResponse = eventPostService.updateEventPostIsUsed(eventPostId);

        //then
        assertEquals(1, eventPostStatusResponse.getEventPostId());
        assertTrue(eventPostStatusResponse.isUsed());
    }

    @Test
    @DisplayName("게시판 사용여부 변경 실패 - 이미 사용중인 게시판이 있는 경우")
    void update_eventPostIsUsedToFalse_AlreadyInUsed(){
        //given
        Long eventPostId = 1L;

        EventPost eventPost = EventPost.builder().title("제목").build();
        ReflectionTestUtils.setField(eventPost, "id", eventPostId);

        when(eventPostRepository.findById(any(Long.class))).thenReturn(Optional.of(eventPost));
        when(eventPostRepository.existsByIsUsedTrue()).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, ()-> eventPostService.updateEventPostIsUsed(eventPostId));

        //then
        assertEquals(ErrorCode.EVENT_POST_IN_USE, exception.getErrorCode());
    }
}
