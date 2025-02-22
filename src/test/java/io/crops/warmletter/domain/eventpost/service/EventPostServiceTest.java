package io.crops.warmletter.domain.eventpost.service;

import io.crops.warmletter.domain.eventpost.dto.request.CreateEventPostRequest;
import io.crops.warmletter.domain.eventpost.dto.response.CreateEventPostResponse;
import io.crops.warmletter.domain.eventpost.entity.EventPost;
import io.crops.warmletter.domain.eventpost.repository.EventPostRepository;
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

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class EventPostServiceTest {
    @Mock
    private EventPostRepository eventPostRepository;

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
        CreateEventPostResponse createEventPostResponse = eventPostService.createEventPost(createEventPostRequest);

        //then
        then(eventPostRepository).should(times(1)).save(any(EventPost.class));

        assertEquals("제목", createEventPostResponse.getTitle());
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
    }
}