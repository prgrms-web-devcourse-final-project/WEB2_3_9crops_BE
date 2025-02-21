package io.crops.warmletter.domain.share.entity;

import io.crops.warmletter.domain.share.enums.ProposalStatus;
import io.crops.warmletter.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ShareProposal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long requesterId;

    @Column(nullable = false)
    private Long recipientId;

    @Enumerated(EnumType.STRING)
    private ProposalStatus status;

    @Column(nullable = false)
    private String message;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public ShareProposal(Long requesterId, Long recipientId, ProposalStatus status, String message) {
        this.requesterId = requesterId;
        this.recipientId = recipientId;
        this.status = ProposalStatus.PENDING;
        this.message = message;
    }

    public void updateStatus(ProposalStatus status) {
        this.status = status;
    }

}
