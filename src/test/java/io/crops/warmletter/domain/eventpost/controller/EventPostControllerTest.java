package io.crops.warmletter.domain.eventpost.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crops.warmletter.domain.eventpost.dto.request.CreateEventPostRequest;
import io.crops.warmletter.domain.eventpost.repository.EventPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
class EventPostControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventPostRepository eventPostRepository;

    @BeforeEach
    void setUp() {
        eventPostRepository.deleteAll();
    }

    @Test
    @DisplayName("게시판 생성 성공")
    void create_event_post_success () throws Exception {
        //given
        CreateEventPostRequest createEventPostRequest = CreateEventPostRequest.builder()
                .title("제목")
                .content("")
                .build();

        //when
        mockMvc.perform(post("/api/admin/event-posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEventPostRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("제목"))
                .andExpect(jsonPath("$.data.content").value(""))
                .andExpect(jsonPath("$.message").value("게시판 생성 완료"));
        //then
    }

    @Test
    @DisplayName("게시판 생성 실패(NotExistTitle)")
    void create_event_post_not_exist_title () throws Exception {
        //given
        CreateEventPostRequest createEventPostRequest = CreateEventPostRequest.builder()
                .title("")
                .content("내용")
                .build();
        //when
        mockMvc.perform(post("/api/admin/event-posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEventPostRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목을 입력해주세요."));
        //then
    }
}