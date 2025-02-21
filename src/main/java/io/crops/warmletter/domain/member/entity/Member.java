package io.crops.warmletter.domain.member.entity;

import io.crops.warmletter.domain.letter.enums.Category;
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
    private String socialUniqueId;

    @Column(nullable = false)
    private String email;

    @Column(unique = true)
    private String zipCode;

    private String password;

    @Column(nullable = false)
    private float temperature;

    @Enumerated(EnumType.STRING)
    private Category preferredLetterCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private LocalDateTime lastMatchedAt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    @Builder
    public Member(String socialUniqueId, String email, String zipCode, String password, float temperature, Category preferredLetterCategory, Role role, LocalDateTime lastMatchedAt) {
        this.socialUniqueId = socialUniqueId;
        this.email = email;
        this.zipCode = zipCode;
        this.password = password;
        this.temperature = temperature;
        this.preferredLetterCategory = preferredLetterCategory;
        this.role = role;
        this.lastMatchedAt = lastMatchedAt;
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }
}
