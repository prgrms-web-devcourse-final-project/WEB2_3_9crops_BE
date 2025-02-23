package io.crops.warmletter.domain.eventpost.entity;

import io.crops.warmletter.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_comments")
public class EventComment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private long eventPostId;

    @Column(nullable = false)
    private long writerId;

    @Column(nullable = false)
    private String content;

    private boolean isActive;

    @Builder
    public EventComment(long eventPostId, long writerId, String content) {
        this.eventPostId = eventPostId;
        this.writerId = writerId;
        this.content = content;
        this.isActive = true;
    }

    public void softDelete(){
        this.isActive = false;
    }
}
