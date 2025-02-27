package io.crops.warmletter.domain.timeline.entity;

import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "timelines")
public class Timeline extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private long memberId;

    @Column(nullable = false)
    private String title;
    private String content;

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;

    @Column(nullable = false)
    private Boolean isRead;

    @Builder
    public Timeline(long memberId, String title, String content, AlarmType alarmType) {
        this.memberId = memberId;
        this.title = title;
        this.content = content;
        this.alarmType = alarmType;
        this.isRead = false;
    }

    public void notificationRead(){
        this.isRead = true;
    }
}
