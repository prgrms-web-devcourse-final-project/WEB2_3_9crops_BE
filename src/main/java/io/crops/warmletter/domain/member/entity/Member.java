package io.crops.warmletter.domain.member.entity;

import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.domain.member.enums.TemperaturePolicy;
import io.crops.warmletter.domain.member.exception.InvalidTemperatureException;
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

    private static final float MAX_TEMPERATURE = 100.0f;
    private static final float MIN_TEMPERATURE = 0.0f;

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

    private boolean isActive;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
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
        this.isActive = true;
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }

    public void updateZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public void inactive() {
        this.isActive = false;
    }

    public void applyTemperaturePolicy(TemperaturePolicy policy) {
        float change = policy.getValue();

        if (change > 0) {
            increaseTemperature(change);
        } else if (change < 0) {
            decreaseTemperature(Math.abs(change));
        }
    }

    public void increaseTemperature(float temperature) {
        if (temperature <= 0) {
            throw new InvalidTemperatureException();
        }

        float newTemperature = this.temperature + temperature;

        if (newTemperature > MAX_TEMPERATURE) {
            this.temperature = MAX_TEMPERATURE;
        } else {
            this.temperature = newTemperature;
        }
    }

    public void decreaseTemperature(float temperature) {
        if (temperature <= 0) {
            throw new InvalidTemperatureException();
        }

        float newTemperature = this.temperature - temperature;

        if (newTemperature < MIN_TEMPERATURE) {
            this.temperature = MIN_TEMPERATURE;
        } else {
            this.temperature = newTemperature;
        }
    }
}
