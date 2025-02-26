package io.crops.warmletter.domain.letter.entity;


import io.crops.warmletter.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "letter_matchings")
public class LetterMatching extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long letterId;

    private Long firstMemberId;

    private Long secondMemberId;

    private boolean isActive;

    private LocalDateTime matchedAt;


    @Builder
    public LetterMatching(Long letterId, Long firstMemberId, Long secondMemberId) {
        this.letterId = letterId;
        this.firstMemberId = firstMemberId;
        this.secondMemberId = secondMemberId;
        this.isActive = true; //매칭 테이블 생성 시 바로 활성
        this.matchedAt = LocalDateTime.now(); //매칭된 시간
    }

    public void inactive() {
        this.isActive = false;
    }
}
