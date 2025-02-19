package io.crops.warmletter.global.oauth.entity;

import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.enums.Role;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
public class UserPrincipal implements OAuth2User {
    private Long id;
    private String email;
    private Role role;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    @Builder
    public UserPrincipal(Long id, String email, Role role, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.authorities = authorities;
    }

    public static UserPrincipal create(Member member, Map<String, Object> attributes) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(member.getRole().name())
        );

        UserPrincipal userPrincipal = UserPrincipal.builder()
                .id(member.getId())
                .email(member.getEmail())
                .role(member.getRole())
                .authorities(authorities)
                .build();

        userPrincipal.setAttributes(attributes);

        return userPrincipal;
    }

    @SuppressWarnings("lombok")
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }
}
