package io.crops.warmletter.global.oauth.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class OAuth2UserWithDeletedFlag implements OAuth2User {

    private final Map<String, Object> attributes;
    private final String email;

    public OAuth2UserWithDeletedFlag(Map<String, Object> attributes, String email) {
        this.attributes = attributes;
        this.email = email;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한 없음
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return email;
    }

    // 탈퇴 회원 여부 확인용 메서드
    public boolean isDeletedMember() {
        return true;
    }
}
