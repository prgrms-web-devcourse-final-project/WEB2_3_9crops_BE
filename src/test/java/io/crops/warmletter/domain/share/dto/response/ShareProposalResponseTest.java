package io.crops.warmletter.domain.share.dto.response;

import io.crops.warmletter.domain.share.enums.ProposalStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShareProposalResponseTest {

    @Test
    @DisplayName("ID와 ZipCode로 ShareProposalResponse 객체 생성 성공")
    void constructor_WithIdAndZipCode_Success() {
        // Given
        Long id = 1L;
        String zipCode = "12345";

        // When
        ShareProposalResponse response = new ShareProposalResponse(id, zipCode);

        // Then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(id, response.getShareProposalId()),
                () -> assertEquals(zipCode, response.getZipCode()),
                () -> assertEquals(ProposalStatus.PENDING, response.getStatus())
        );
    }

    @Test
    @DisplayName("ID와 ZipCode로 ShareProposalResponse 객체 생성 - null 값 처리")
    void constructor_WithNullValues() {
        // Given
        Long id = null;
        String zipCode = null;

        // When
        ShareProposalResponse response = new ShareProposalResponse(id, zipCode);

        // Then
        assertAll(
                () -> assertNotNull(response),
                () -> assertNull(response.getShareProposalId()),
                () -> assertNull(response.getZipCode()),
                () -> assertEquals(ProposalStatus.PENDING, response.getStatus())
        );
    }
}