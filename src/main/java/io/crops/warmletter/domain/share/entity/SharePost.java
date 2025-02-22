package io.crops.warmletter.domain.share.entity;

import io.crops.warmletter.global.entity.BaseEntity;
import io.crops.warmletter.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SharePost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long shareProposalId;

    private boolean isActive;

    @Column(nullable = false)
    private String content;

    @Builder
    public SharePost(Long shareProposalId, String content,boolean isActive) {
        this.shareProposalId = shareProposalId;
        this.content = content;
        this.isActive = isActive;
    }

    // 비즈니스 로직 메서드
    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

}
