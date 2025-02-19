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

    private boolean isActive = false;

    @Column(nullable = false)
    private String title;

    private String content;

    private String createdBy;

    private String updatedBy;

    @Builder
    public SharePost(Long shareProposalId, String title, String content) {
        this.shareProposalId = shareProposalId;
        this.title = title;
        this.content = content;
    }

}
