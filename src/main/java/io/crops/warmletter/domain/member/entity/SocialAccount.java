package io.crops.warmletter.domain.member.entity;

import io.crops.warmletter.domain.member.enums.SocialProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "social_accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialProvider provider;

    @Column(nullable = false)
    private String socialId;  // 소셜 서비스의 고유 id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public SocialAccount(SocialProvider provider, String socialId) {
        this.provider = provider;
        this.socialId = socialId;
    }

    // 연관관계 편의 메서드
    @SuppressWarnings("lombok")
    public void setMember(Member member) {
        this.member = member;
    }
}
