package io.crops.warmletter.domain.auth.facade;

import io.crops.warmletter.domain.auth.service.AuthService;
import io.crops.warmletter.global.oauth.entity.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthService authService;

    public UserPrincipal getCurrentUser() {
        return authService.getCurrentUser();
    }

    public Long getCurrentUserId() {
        return authService.getCurrentUserId();
    }

    public String getZipCode() {
        return authService.getZipCode();
    }
}
