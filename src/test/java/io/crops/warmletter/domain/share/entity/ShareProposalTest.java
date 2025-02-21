package io.crops.warmletter.domain.share.entity;

import io.crops.warmletter.domain.share.enums.ProposalStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShareProposalTest {

    @Test
    @DisplayName("ShareProposal 생성 - 성공")
    void CreateShareProposal() {
        // given
        Long requesterId = 1L;
        Long recipientId = 2L;
        String message = "Test Message";

        // when
        ShareProposal shareProposal = ShareProposal.builder()
                .requesterId(requesterId)
                .recipientId(recipientId)
                .message(message)
                .build();

        // then
        assertAll(
                () -> assertEquals(requesterId, shareProposal.getRequesterId()),
                () -> assertEquals(recipientId, shareProposal.getRecipientId()),
                () -> assertEquals(message, shareProposal.getMessage()),
                () -> assertEquals(ProposalStatus.PENDING, shareProposal.getStatus())
        );
    }

    @Test
    @DisplayName("ShareProposal 상태 업데이트 - 성공")
    void updateShareProposalStatus() {
        // given
        ShareProposal shareProposal = ShareProposal.builder()
                .requesterId(1L)
                .recipientId(2L)
                .message("Test Message")
                .build();

        // when
        shareProposal.updateStatus(ProposalStatus.APPROVED);

        // then
        assertEquals(ProposalStatus.APPROVED, shareProposal.getStatus());
    }
}