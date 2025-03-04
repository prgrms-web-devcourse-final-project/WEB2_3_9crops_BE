package io.crops.warmletter.domain.eventpost.entity;

import io.crops.warmletter.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_posts")
public class EventPost extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean isUsed;

    @Column(nullable = false)
    private boolean isActive;

    @Builder
    public EventPost(String title) {
        this.title = title;
        this.isUsed = false;    // 기본 값 비활성화
        this.isActive = true;
    }

    public void softDelete(){
        this.isUsed = false;
    }

    public void isUsedChange(boolean isUsed){
        this.isUsed = isUsed;
    }
}
