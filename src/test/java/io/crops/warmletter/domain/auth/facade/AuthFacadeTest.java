package io.crops.warmletter.domain.auth.facade;

import io.crops.warmletter.domain.auth.service.AuthService;
import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.global.oauth.entity.UserPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthFacadeTest {

    @InjectMocks
    private AuthFacade authFacade;

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletResponse response;

    @DisplayName("authService getCurrentUser 호출 성공")
    @Test
    void getCurrentUser_ShouldReturnUserFromService() {
        // given
        UserPrincipal expectedUser = UserPrincipal.builder()
                .id(1L)
                .email("test@test.com")
                .socialUniqueId("12345")
                .role(Role.USER)
                .zipCode("12345")
                .build();
        when(authService.getCurrentUser()).thenReturn(expectedUser);

        // when
        UserPrincipal result = authFacade.getCurrentUser();

        // then
        assertThat(result).isEqualTo(expectedUser);
        verify(authService).getCurrentUser();
    }

    @DisplayName("authService getCurrentUserId 호출 성공")
    @Test
    void getCurrentUserId_ShouldReturnUserIdFromService() {
        // given
        Long expectedId = 1L;
        when(authService.getCurrentUserId()).thenReturn(expectedId);

        // when
        Long result = authFacade.getCurrentUserId();

        // then
        assertThat(result).isEqualTo(expectedId);
        verify(authService).getCurrentUserId();
    }

    @DisplayName("authService getZipCode 호출 성공")
    @Test
    void getZipCode_ShouldReturnZipCodeFromService() {
        // given
        String expectedZipCode = "12345";
        when(authService.getZipCode()).thenReturn(expectedZipCode);

        // when
        String result = authFacade.getZipCode();

        // then
        assertThat(result).isEqualTo(expectedZipCode);
        verify(authService).getZipCode();
    }

    @DisplayName("authService logout 호출 성공")
    @Test
    void logout_Success() {
        //given
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        //when
        authService.logout(accessToken, refreshToken, response);

        //then
        verify(authService).logout(accessToken, refreshToken, response);
    }
}