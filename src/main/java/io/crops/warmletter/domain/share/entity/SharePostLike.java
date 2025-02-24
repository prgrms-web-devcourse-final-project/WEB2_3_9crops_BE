package io.crops.warmletter.domain.share.entity;

import io.crops.warmletter.global.entity.BaseEntity;
import io.crops.warmletter.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SharePostLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sharePostId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private boolean isLiked;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
