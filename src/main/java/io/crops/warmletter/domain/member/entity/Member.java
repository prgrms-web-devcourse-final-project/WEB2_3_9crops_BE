package io.crops.warmletter.domain.member.entity;

import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String zipCode;

    private String password;

    @Column(nullable = false)
    private float temperature;

    // @Enumerated(EnumType.STRING)
    // private Category preferredLetterCategory

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private LocalDateTime lastMatchedAt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    @Builder
    public Member(String email, String zipCode, String password, float temperature, Role role, LocalDateTime lastMatchedAt) {
        this.email = email;
        this.zipCode = zipCode;
        this.password = password;
        this.temperature = temperature;
        this.role = role;
        this.lastMatchedAt = lastMatchedAt;
    }
}
