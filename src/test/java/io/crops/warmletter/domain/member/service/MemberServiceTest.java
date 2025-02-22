package io.crops.warmletter.domain.member.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.member.dto.response.ZipCodeResponse;
import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.domain.member.exception.DuplicateZipCodeException;
import io.crops.warmletter.domain.member.exception.MemberNotFoundException;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import io.crops.warmletter.global.jwt.provider.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuthFacade authFacade;

    @Mock
    private HttpServletResponse response;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @DisplayName("회원 이메일 업데이트")
    @Test
    void updateMemberEmail() {
        // given
        String oldEmail = "old@example.com";
        String newEmail = "new@example.com";
        String socialUniqueId = "GOOGLE_12345";

        Member member = Member.builder()
                .email(oldEmail)
                .socialUniqueId(socialUniqueId)
                .role(Role.USER)
                .build();

        // when
        memberService.updateMemberEmail(member, newEmail);

        // then
        assertThat(member.getEmail()).isEqualTo(newEmail);
    }

    @DisplayName("우편 번호 생성 오류 - 해당 사용자 존재하지 않음")
    @Test
    void createZipCode_WithInvalidMemberId_ShouldThrowException() {
        //given
        Long invalidMemberId = 999L;
        when(authFacade.getCurrentUserId()).thenReturn(invalidMemberId);
        when(memberRepository.findById(invalidMemberId)).thenReturn(Optional.empty());

        //when & then
        assertThrows(MemberNotFoundException.class,
                () -> memberService.createZipCode(response));

        verify(authFacade).getCurrentUserId();
        verify(memberRepository).findById(invalidMemberId);
    }

    @DisplayName("우편 번호 생성 오류 - 이미 우편번호가 발급된 사용자")
    @Test
    void createZipCode_ExistZipCode_ShouldThrowException() {
        //given
        Long memberId = 1L;
        Member member = Member.builder()
                            .socialUniqueId("GOOGLE_12345")
                            .zipCode("12345")
                            .email("test@test.com")
                            .temperature(36.5f)
                            .role(Role.USER)
                            .build();
        when(authFacade.getCurrentUserId()).thenReturn(memberId);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        //when & then
        assertThrows(DuplicateZipCodeException.class,
                () -> memberService.createZipCode(response));

        verify(authFacade).getCurrentUserId();
        verify(memberRepository).findById(memberId);
    }

    @DisplayName("우편 번호 발급 성공")
    @Test
    void createZipCode_Success() throws Exception {
        //given
        Long memberId = 1L;
        Member member = Member.builder()
                .socialUniqueId("GOOGLE_12345")
                .email("test@test.com")
                .temperature(36.5f)
                .role(Role.USER)
                .build();

        // Reflection으로 id 설정
        Field idField = Member.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(member, memberId);

        when(authFacade.getCurrentUserId()).thenReturn(memberId);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        // 첫 번째 체크에서 중복되지 않은 것으로 가정
        when(memberRepository.existsByZipCode(any())).thenReturn(false);

        String expectedToken = "new.access.token";
        when(jwtTokenProvider.createAccessToken(
                eq(member.getSocialUniqueId()),
                eq(member.getRole()),
                any(String.class),
                eq(memberId)
        )).thenReturn(expectedToken);

        // when
        ZipCodeResponse zipCodeResponse = memberService.createZipCode(response);

        // then
        assertThat(zipCodeResponse).isNotNull();
        assertThat(zipCodeResponse.getZipCode()).isNotNull();
        assertThat(member.getZipCode()).isEqualTo(zipCodeResponse.getZipCode());

        verify(memberRepository).findById(memberId);
        verify(memberRepository).existsByZipCode(any());
        verify(jwtTokenProvider).createAccessToken(
                eq(member.getSocialUniqueId()),
                eq(member.getRole()),
                eq(member.getZipCode()),
                eq(memberId)
        );
        verify(response).addHeader(eq(HttpHeaders.AUTHORIZATION), eq("Bearer " + expectedToken));
    }
}