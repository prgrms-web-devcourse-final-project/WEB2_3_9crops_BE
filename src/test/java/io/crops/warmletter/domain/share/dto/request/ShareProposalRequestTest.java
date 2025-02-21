package io.crops.warmletter.domain.share.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShareProposalRequestTest {

    @Test
    @DisplayName("모든 필드를 포함한 생성자로 객체 생성")
    void allArgsConstructor() {
        // given
        List<Long> letters = List.of(1L, 2L);
        Long requesterId = 1L;
        Long recipientId = 2L;
        String message = "공유 요청";

        // when
        ShareProposalRequest request = new ShareProposalRequest(letters, requesterId, recipientId, message);

        // then
        assertAll(
                () -> assertEquals(letters, request.getLetters()),
                () -> assertEquals(requesterId, request.getRequesterId()),
                () -> assertEquals(recipientId, request.getRecipientId()),
                () -> assertEquals(message, request.getMessage())
        );
    }
}