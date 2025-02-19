package io.crops.warmletter.domain.eventpost.entity;

import io.crops.warmletter.global.entity.BaseEntity;
import io.crops.warmletter.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "event_posts")
public class EventPost extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;

    private String content;

    @Builder
    public EventPost(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
