package io.crops.warmletter.domain.share.entity;

import io.crops.warmletter.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShareProposalLetter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long proposalId;

    @Column(nullable = false)
    private long letterId;

    private LocalDateTime createdAt;

    @Builder
    public ShareProposalLetter(Long proposalId, long letterId) {
        this.proposalId = proposalId;
        this.letterId = letterId;
    }

}
