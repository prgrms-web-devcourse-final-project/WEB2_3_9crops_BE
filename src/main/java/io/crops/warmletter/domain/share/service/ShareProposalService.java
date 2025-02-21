package io.crops.warmletter.domain.share.service;

import io.crops.warmletter.domain.share.dto.request.ShareProposalRequest;
import io.crops.warmletter.domain.share.dto.response.ShareProposalResponse;
import io.crops.warmletter.domain.share.entity.ShareProposal;
import io.crops.warmletter.domain.share.entity.ShareProposalLetter;
import io.crops.warmletter.domain.share.repository.ShareProposalLetterRepository;
import io.crops.warmletter.domain.share.repository.ShareProposalRepository;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShareProposalService {

    private final ShareProposalRepository shareProposalRepository;
    private final ShareProposalLetterRepository shareProposalLetterRepository;

    @Transactional
    public ShareProposalResponse requestShareProposal(ShareProposalRequest request) {
        // 1. ShareProposal 저장
        if (request.getRequesterId() == null || request.getRecipientId() == null ||
                request.getLetters() == null || request.getLetters().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        ShareProposal shareProposal = shareProposalRepository.save(request.toEntity());


        // 2. ShareProposalLetter 생성 및 저장
        List<ShareProposalLetter> letters = request.getLetters().stream()
                .map(letterId -> new ShareProposalLetter(shareProposal.getId(), letterId))
                .collect(Collectors.toList());
        shareProposalLetterRepository.saveAll(letters);

        // 3. Response 조회 및 반환
        ShareProposalResponse response = shareProposalRepository.findShareProposalWithZipCode(shareProposal.getId());
        if (response == null) {
            throw new BusinessException(ErrorCode.SHARE_POST_NOT_FOUND);
        }
        return response;

    }
}
