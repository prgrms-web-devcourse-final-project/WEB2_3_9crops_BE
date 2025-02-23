package io.crops.warmletter.domain.eventpost.service;

import io.crops.warmletter.domain.eventpost.dto.request.CreateEventPostRequest;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostDetailResponse;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostResponse;
import io.crops.warmletter.domain.eventpost.entity.EventPost;
import io.crops.warmletter.domain.eventpost.exception.EventPostNotFoundException;
import io.crops.warmletter.domain.eventpost.exception.UsedEventPostNotFoundException;
import io.crops.warmletter.domain.eventpost.repository.EventCommentRepository;
import io.crops.warmletter.domain.eventpost.repository.EventPostRepository;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;
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
    @DisplayName("게시판 작성 성공")
    void createEventPost_success(){
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
    void deleteEventPost_success(){
        //given
        long eventPostId = 1L;

        EventPost eventPost = EventPost.builder().title("제목").build();
        ReflectionTestUtils.setField(eventPost, "id", eventPostId);

        when(eventPostRepository.findById(any(Long.class))).thenReturn(Optional.of(eventPost));

        //when
        Map<String,Long> deleteEventPostResponse = eventPostService.deleteEventPost(eventPostId);

        //then
        then(eventPostRepository).should(times(1)).findById(any(Long.class));

        assertEquals(1, deleteEventPostResponse.get("eventPostId"));
        assertFalse(eventPost.getIsUsed());

    }


    @Test
    @DisplayName("게시판 삭제 실패 - 존재하지 않는 게시판")
    void deleteEventPost_fail_not_found() {
        // given
        when(eventPostRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // when & then
        assertThrows(EventPostNotFoundException.class, () -> eventPostService.deleteEventPost(999));

        // then
        then(eventPostRepository).should(times(1)).findById(any(Long.class));
    }

    @Test
    @DisplayName("사용중인 게시판 조회 성공")
    void getUsedEventPost_success(){
        //given
        long eventPostId = 1L;

        EventPost eventPost = EventPost.builder().title("제목").build();
        ReflectionTestUtils.setField(eventPost, "id", eventPostId);

        when(eventPostRepository.findFirstByIsUsed(true)).thenReturn(eventPost);

        //when
        EventPostResponse eventPostResponse = eventPostService.getUsedEventPost();

        //then
        assertNotNull(eventPostResponse);
        assertEquals(1, eventPostResponse.getEventPostId());
        assertEquals("제목", eventPostResponse.getTitle());
    }

    @Test
    @DisplayName("사용중인 게시판 조회 실패 - 조건이 일치하는 게시판 없음")
    void getUsedEventPost_not_found(){
        //given
        when(eventPostRepository.findFirstByIsUsed(true)).thenReturn(null);
        //when
        BusinessException exception = assertThrows(UsedEventPostNotFoundException.class, ()-> eventPostService.getUsedEventPost());

        //then
        assertEquals(ErrorCode.USED_EVENT_POST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시판 조회(개별) 성공")
    void getEventPost_success(){
        // given
        EventPost eventPost = EventPost.builder().title("제목").build();
        ReflectionTestUtils.setField(eventPost, "id", 1);

        List<Object[]> result = new ArrayList<>();
        Object[] object1 = {1L, "11111", "내용1"};
        Object[] object2 = {2L, "22222", "내용2"};
        result.add(object1);
        result.add(object2);

        when(eventPostRepository.findById(any(Long.class))).thenReturn(Optional.of(eventPost));
        when(eventCommentRepository.findEventCommentsWithZipCode(any(Long.class))).thenReturn(result);

        // when
        EventPostDetailResponse eventPostDetailResponse = eventPostService.getEventPostDetail(1);

        // then
        assertEquals("제목", eventPostDetailResponse.getTitle());
        assertNotNull(eventPostDetailResponse.getEventPostComments());
        assertEquals(2, eventPostDetailResponse.getEventPostComments().size());
        assertEquals(1L, eventPostDetailResponse.getEventPostComments().get(0).getCommentId());
        assertEquals("11111", eventPostDetailResponse.getEventPostComments().get(0).getZipCode());
        assertEquals("내용1", eventPostDetailResponse.getEventPostComments().get(0).getContent());
        assertEquals(2L, eventPostDetailResponse.getEventPostComments().get(1).getCommentId());
        assertEquals("22222", eventPostDetailResponse.getEventPostComments().get(1).getZipCode());
        assertEquals("내용2", eventPostDetailResponse.getEventPostComments().get(1).getContent());
    }

    @Test
    @DisplayName("게시판 조회(개별) 실패 - 일치하는 eventPostId 없음")
    void getEventPost_not_found(){
        //given
        when(eventPostRepository.findById(any(Long.class))).thenThrow(new EventPostNotFoundException());

        //when
        BusinessException exception = assertThrows(EventPostNotFoundException.class, ()-> eventPostService.getEventPostDetail(999));

        //then
        then(eventPostRepository).should(times(1)).findById(any(Long.class));

        assertEquals(ErrorCode.EVENT_POST_NOT_FOUND, exception.getErrorCode());
    }
}