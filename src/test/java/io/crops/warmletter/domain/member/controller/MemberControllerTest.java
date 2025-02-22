package io.crops.warmletter.domain.member.controller;

import io.crops.warmletter.domain.member.dto.response.MeResponse;
import io.crops.warmletter.domain.member.dto.response.ZipCodeResponse;
import io.crops.warmletter.domain.member.enums.SocialProvider;
import io.crops.warmletter.domain.member.exception.DuplicateZipCodeException;
import io.crops.warmletter.domain.member.exception.MemberNotFoundException;
import io.crops.warmletter.domain.member.service.MemberService;
import io.crops.warmletter.global.config.CorsConfig;
import io.crops.warmletter.global.config.TestSecurityConfig;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private CorsConfig corsConfig;

    @DisplayName("우편번호 생성 API 호출 성공")
    @Test
    void createZipCode_Success() throws Exception {
        // given
        String zipCode = "ABC12";
        String expectedToken = "new.access.token";
        ZipCodeResponse expectedResponse = new ZipCodeResponse(zipCode);

        when(memberService.createZipCode(any(HttpServletResponse.class)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/members/zipCode"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.zipCode").value(zipCode))
                .andExpect(jsonPath("$.message").value("우편번호 생성 완료"));

        verify(memberService).createZipCode(any(HttpServletResponse.class));
    }

    @DisplayName("우편번호 생성 실패 - 해당 사용자가 존재하지 않는 경우")
    @Test
    void createZipCode_Fail_MemberNotFound() throws Exception {
        // given
        when(memberService.createZipCode(any(HttpServletResponse.class)))
                .thenThrow(new MemberNotFoundException());

        // when & then
        mockMvc.perform(post("/api/members/zipCode"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEM-001"))
                .andExpect(jsonPath("$.message").value("유저를 찾을 수 없습니다."));
    }

    @DisplayName("우편번호 생성 실패 - 이미 우편번호가 존재하는 경우")
    @Test
    void createZipCode_Fail_DuplicateZipCode() throws Exception {
        // given
        when(memberService.createZipCode(any(HttpServletResponse.class)))
                .thenThrow(new DuplicateZipCodeException());

        // when & then
        mockMvc.perform(post("/api/members/zipCode"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEM-002"))
                .andExpect(jsonPath("$.message").value("우편번호가 이미 존재합니다."));
    }

    @DisplayName("마이페이지 조회 API 호출 성공")
    @Test
    void getMe_Success() throws Exception {
        //given
        String zipCode = "1A2AC";
        float temperature = 36.5f;
        SocialProvider social = SocialProvider.GOOGLE;
        String email = "test@test.com";
        MeResponse response = new MeResponse(zipCode, temperature, social, email);

        when(memberService.getMe()).thenReturn(response);

        //when & then
        mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.zipCode").value(zipCode))
                .andExpect(jsonPath("$.data.temperature").value(temperature))
                .andExpect(jsonPath("$.data.social").value(SocialProvider.GOOGLE.name()))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.message").value("마이페이지 조회 완료"));
    }

    @DisplayName("마이페이지 조회 API 호출 실패 - 존재하지 않은 사용자")
    @Test
    void getMe_Fail_MemberNotFound() throws Exception {
        //given
        when(memberService.getMe())
                .thenThrow(new MemberNotFoundException());

        //when & then
        mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEM-001"))
                .andExpect(jsonPath("$.message").value("유저를 찾을 수 없습니다."));
    }
}