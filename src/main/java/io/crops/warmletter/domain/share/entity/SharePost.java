package io.crops.warmletter.domain.share.entity;

import io.crops.warmletter.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        indexes = {
                @Index(name = "idx_sharepost_active_created", columnList = "isActive,createdAt")
        }
)
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

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

}
