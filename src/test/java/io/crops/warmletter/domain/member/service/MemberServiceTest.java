package io.crops.warmletter.domain.member.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.letter.enums.LetterEvaluation;
import io.crops.warmletter.domain.member.dto.response.MeResponse;
import io.crops.warmletter.domain.member.dto.response.ZipCodeResponse;
import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.entity.SocialAccount;
import io.crops.warmletter.domain.member.entity.Temperature;
import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.domain.member.enums.SocialProvider;
import io.crops.warmletter.domain.member.enums.TemperaturePolicy;
import io.crops.warmletter.domain.member.exception.DeletedMemberException;
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

    @DisplayName("마이페이지 조회 오류 - 해당 사용자 존재하지 않음")
    @Test
    void getMe_WithInvalidMemberId_ShouldThrowException() throws Exception {
        // given
        Long invalidMemberId = 999L;
        when(authFacade.getCurrentUserId()).thenReturn(invalidMemberId);
        when(memberRepository.findMeById(invalidMemberId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(MemberNotFoundException.class,
                () -> memberService.getMe());

        verify(authFacade).getCurrentUserId();
        verify(memberRepository).findMeById(invalidMemberId);
    }

    @DisplayName("마이페이지 조회 성공")
    @Test
    void getMe_Success() throws Exception {
        //given
        Long memberId = 1L;
        Member member = Member.builder()
                .socialUniqueId("GOOGLE_12345")
                .zipCode("1A2AC")
                .email("test@test.com")
                .role(Role.USER)
                .build();

        SocialAccount socialAccount = SocialAccount.builder()
                .socialId("12345")
                .provider(SocialProvider.GOOGLE)
                .build();

        socialAccount.setMember(member);

        // Reflection으로 id 설정
        Field idField = Member.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(member, memberId);

        when(authFacade.getCurrentUserId()).thenReturn(memberId);
        when(memberRepository.findMeById(memberId))
                .thenReturn(Optional.of(new MeResponse(
                        member.getZipCode(),
                        member.getTemperatureValue(),
                        socialAccount.getProvider(),
                        member.getEmail()
                )));
        //when
        MeResponse meResponse = memberService.getMe();

        //then
        assertThat(member.getZipCode()).isEqualTo(meResponse.getZipCode());
        assertThat(member.getTemperatureValue()).isEqualTo(meResponse.getTemperature());
        assertThat(socialAccount.getProvider()).isEqualTo(meResponse.getSocial());
        assertThat(member.getEmail()).isEqualTo(meResponse.getEmail());

        verify(authFacade).getCurrentUserId();
        verify(memberRepository).findMeById(memberId);
    }

    @DisplayName("회원 탈퇴 오류 - 해당 사용자 존재하지 않음")
    @Test
    void deleteMe_WithInvalidMemberId_ShouldThrowException() throws Exception {
        // given
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        Long invalidMemberId = 999L;
        when(authFacade.getCurrentUserId()).thenReturn(invalidMemberId);
        when(memberRepository.findById(invalidMemberId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(MemberNotFoundException.class,
                () -> memberService.deleteMe(accessToken, refreshToken, response));

        verify(authFacade).getCurrentUserId();
        verify(memberRepository).findById(invalidMemberId);
    }

    @DisplayName("회원 탈퇴 오류 - 이미 탈퇴한 사용자")
    @Test
    void deleteMe_WithDeletedMemberId_ShouldThrowException() throws Exception {
        // given
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        Long memberId = 1L;
        Member member = Member.builder()
                .socialUniqueId("GOOGLE_12345")
                .zipCode("1A2AC")
                .email("test@test.com")
                .role(Role.USER)
                .build();

        // Reflection으로 id 설정
        Field idField = Member.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(member, memberId);
        Field isActiveField = Member.class.getDeclaredField("isActive");
        isActiveField.setAccessible(true);
        isActiveField.set(member, false);

        when(authFacade.getCurrentUserId()).thenReturn(memberId);
        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(member));

        // when & then
        assertThrows(DeletedMemberException.class,
                () -> memberService.deleteMe(accessToken, refreshToken, response));

        verify(authFacade).getCurrentUserId();
        verify(memberRepository).findById(memberId);
    }

    @DisplayName("회원 탈퇴 성공")
    @Test
    void deleteMe_Success() throws Exception {
        //given
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        Long memberId = 1L;
        Member member = Member.builder()
                .socialUniqueId("GOOGLE_12345")
                .zipCode("1A2AC")
                .email("test@test.com")
                .role(Role.USER)
                .build();

        // Reflection으로 id 설정
        Field idField = Member.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(member, memberId);

        when(authFacade.getCurrentUserId()).thenReturn(memberId);
        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(member));

        //when
        memberService.deleteMe(accessToken, refreshToken, response);

        //then
        assertThat(member.isActive()).isFalse();

        verify(authFacade).getCurrentUserId();
        verify(memberRepository).findById(memberId);
        verify(authFacade).logout(accessToken, refreshToken, response);
    }
    
    @DisplayName("평가에 따른 온도 적용 실패 - 존재하지 않은 회원")
    @Test
    void applyEvaluationTemperature_WithInvalidMemberId_ShouldThrowException() {
        //given
        Long invalidMemberId = 999L;
        LetterEvaluation evaluation = LetterEvaluation.GOOD;
        when(memberRepository.findById(invalidMemberId)).thenReturn(Optional.empty());

        //when & then
        assertThrows(MemberNotFoundException.class,
                () -> memberService.applyEvaluationTemperature(invalidMemberId, evaluation));

        verify(memberRepository).findById(invalidMemberId);
    }

    @DisplayName("평가에 따른 온도 적용 성공 - 좋은 편지 평가를 받았을 경우")
    @Test
    void applyEvaluationTemperature_Success_GoodLetterEvaluation() throws Exception {
        //given
        Long memberId = 1L;
        LetterEvaluation evaluation = LetterEvaluation.GOOD;
        Member member = Member.builder()
                .socialUniqueId("GOOGLE_12345")
                .zipCode("1AA2C")
                .role(Role.USER)
                .build();

        // Reflection으로 id 설정
        Field idField = Member.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(member, memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        //when
        memberService.applyEvaluationTemperature(memberId, evaluation);

        //then
        float expectedTemperature = 36.5f + TemperaturePolicy.GOOD_EVALUATION.getValue();
        assertEquals(expectedTemperature, member.getTemperatureValue());
        verify(memberRepository).findById(memberId);
    }

    @DisplayName("평가에 따른 온도 적용 성공 - 나쁜 편지 평가를 받았을 경우")
    @Test
    void applyEvaluationTemperature_Success_BadLetterEvaluation() throws Exception {
        //given
        Long memberId = 1L;
        LetterEvaluation evaluation = LetterEvaluation.BAD;
        Member member = Member.builder()
                .socialUniqueId("GOOGLE_12345")
                .zipCode("1AA2C")
                .role(Role.USER)
                .build();

        // Reflection으로 id 설정
        Field idField = Member.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(member, memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        //when
        memberService.applyEvaluationTemperature(memberId, evaluation);

        //then
        float expectedTemperature = 36.5f + TemperaturePolicy.BAD_EVALUATION.getValue();
        assertEquals(expectedTemperature, member.getTemperatureValue());
        verify(memberRepository).findById(memberId);
    }

    @DisplayName("온도 경계값 테스트 - 최대값을 초과하는 경우 최대값으로 제한되어야 한다")
    @Test
    void applyEvaluationTemperature_MaxBoundary() throws Exception {
        //given
        Long memberId = 1L;
        LetterEvaluation evaluation = LetterEvaluation.GOOD;
        Member member = Member.builder()
                .socialUniqueId("GOOGLE_12345")
                .zipCode("1AA2C")
                .role(Role.USER)
                .build();

        // Reflection으로 id 설정
        Field idField = Member.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(member, memberId);
        // Reflection으로 temperature 필드에 새 객체 설정
        Field temperatureField = Member.class.getDeclaredField("temperature");
        temperatureField.setAccessible(true);
        temperatureField.set(member, new Temperature(99.5f));

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        //when
        memberService.applyEvaluationTemperature(memberId, evaluation);

        //then
        assertEquals(100.0f, member.getTemperatureValue());  // 최대값으로 제한
        verify(memberRepository).findById(memberId);
    }

    @DisplayName("온도 경계값 테스트 - 최소값 미만으로 감소하는 경우 최소값으로 제한되어야 한다")
    @Test
    void applyEvaluationTemperature_MinBoundary() throws Exception {
        //given
        Long memberId = 1L;
        LetterEvaluation evaluation = LetterEvaluation.BAD;
        Member member = Member.builder()
                .socialUniqueId("GOOGLE_12345")
                .zipCode("1AA2C")
                .role(Role.USER)
                .build();

        // Reflection으로 id 설정
        Field idField = Member.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(member, memberId);
        // Reflection으로 temperature 필드에 새 객체 설정
        Field temperatureField = Member.class.getDeclaredField("temperature");
        temperatureField.setAccessible(true);
        temperatureField.set(member, new Temperature(0.5f));

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        //when
        memberService.applyEvaluationTemperature(memberId, evaluation);

        //then
        assertEquals(0.0f, member.getTemperatureValue());  // 최소값으로 제한
        verify(memberRepository).findById(memberId);
    }
}