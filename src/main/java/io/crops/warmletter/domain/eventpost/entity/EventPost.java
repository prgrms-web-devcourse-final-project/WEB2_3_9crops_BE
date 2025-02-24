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
    private long id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true)
    private Boolean isUsed;

    @Builder
    public EventPost(String title) {
        this.title = title;
        this.isUsed = false;    // 기본 값 비활성화
    }

    public void softDelete(){
        this.isUsed = false;
    }
}
