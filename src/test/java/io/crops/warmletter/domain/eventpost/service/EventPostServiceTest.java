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

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

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
                .content("내용")
                .build();

        EventPost eventPost = EventPost.builder()
                .title(createEventPostRequest.getTitle())
                .content(createEventPostRequest.getContent())
                .build();

        //when
        CreateEventPostResponse createEventPostResponse = eventPostService.createEventPost(createEventPostRequest);

        //then
        assertNotNull(createEventPostResponse);
        assertEquals("제목",createEventPostResponse.getTitle());
        assertEquals("내용",createEventPostResponse.getContent());
    }
}