package io.crops.warmletter.domain.share.service;

import io.crops.warmletter.domain.share.dto.request.ShareProposalRequest;
import io.crops.warmletter.domain.share.dto.request.UpdateProposalStatusRequest;
import io.crops.warmletter.domain.share.dto.response.ShareProposalResponse;
import io.crops.warmletter.domain.share.dto.response.ShareProposalStatusResponse;
import io.crops.warmletter.domain.share.entity.SharePost;
import io.crops.warmletter.domain.share.entity.ShareProposal;
import io.crops.warmletter.domain.share.entity.ShareProposalLetter;
import io.crops.warmletter.domain.share.enums.ProposalStatus;
import io.crops.warmletter.domain.share.repository.SharePostRepository;
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
    private final SharePostRepository sharePostRepository;

    @Transactional
    public ShareProposalResponse requestShareProposal(ShareProposalRequest request) {

        if (request.getRequesterId() == null || request.getRecipientId() == null ||
                request.getLetters() == null || request.getLetters().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        ShareProposal shareProposal = shareProposalRepository.save(request.toEntity());

        List<ShareProposalLetter> letters = request.getLetters().stream()
                .map(letterId -> new ShareProposalLetter(shareProposal.getId(), letterId))
                .collect(Collectors.toList());
        shareProposalLetterRepository.saveAll(letters);

        ShareProposalResponse response = shareProposalRepository.findShareProposalWithZipCode(shareProposal.getId());
        if (response == null) {
            throw new BusinessException(ErrorCode.SHARE_POST_NOT_FOUND);
        }

        return response;

    }

    @Transactional
    public ShareProposalStatusResponse approveShareProposal(Long shareProposalId) {
        ShareProposal shareProposal = shareProposalRepository.findById(shareProposalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHARE_PROPOSAL_NOTFOUND));

        shareProposal.updateStatus(ProposalStatus.APPROVED);
        SharePost sharePost = SharePost.builder()
                .shareProposalId(shareProposal.getId())
                .content(shareProposal.getMessage())
                .isActive(true)
                .build();
        sharePostRepository.save(sharePost);

        return ShareProposalStatusResponse.builder()
                .shareProposalId(shareProposal.getId())
                .status(shareProposal.getStatus())
                .sharePostId(sharePost.getId())
                .build();
    }
}
