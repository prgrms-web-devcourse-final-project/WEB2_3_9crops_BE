package io.crops.warmletter.domain.eventpost.entity;

import io.crops.warmletter.global.entity.BaseEntity;
import io.crops.warmletter.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_posts")
public class EventPost extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;

    private String content;

    private Boolean isUsed;

    @Builder
    public EventPost(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void softDelete(){
        this.isUsed = false;
    }
}
