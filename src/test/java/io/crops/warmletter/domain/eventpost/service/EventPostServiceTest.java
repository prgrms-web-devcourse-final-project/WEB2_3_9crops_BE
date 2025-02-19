package io.crops.warmletter.domain.eventpost.service;

import io.crops.warmletter.domain.eventpost.dto.request.CreateEventPostRequest;
import io.crops.warmletter.domain.eventpost.dto.response.CreateEventPostResponse;
import io.crops.warmletter.domain.eventpost.repository.EventPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
class EventPostServiceTest {
    @Autowired
    private EventPostService eventPostService;

    @Autowired
    private EventPostRepository eventPostRepository;

    @BeforeEach
    void setUp() {
        eventPostRepository.deleteAll();
    }
    
    @Test
    @DisplayName("게시판 작성 성공")
    void createEventPost_success(){
        //given
        CreateEventPostRequest createEventPostRequest = CreateEventPostRequest.builder()
                .title("제목")
                .content("내용")
                .build();

        //when
        CreateEventPostResponse createEventPostResponse = eventPostService.createEventPost(createEventPostRequest);

        //then
        assertNotNull(createEventPostResponse);
        assertEquals("제목",createEventPostResponse.getTitle());
        assertEquals("내용",createEventPostResponse.getContent());
    }
}