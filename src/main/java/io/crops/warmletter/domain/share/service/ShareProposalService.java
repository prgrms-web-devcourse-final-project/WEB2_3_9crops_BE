package io.crops.warmletter.domain.share.service;

import io.crops.warmletter.domain.share.dto.request.ShareProposalRequest;
import io.crops.warmletter.domain.share.dto.response.ShareProposalResponse;
import io.crops.warmletter.domain.share.dto.response.ShareProposalStatusResponse;
import io.crops.warmletter.domain.share.entity.SharePost;
import io.crops.warmletter.domain.share.entity.ShareProposal;
import io.crops.warmletter.domain.share.entity.ShareProposalLetter;
import io.crops.warmletter.domain.share.enums.ProposalStatus;
import io.crops.warmletter.domain.share.repository.*;
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

    @Transactional
    public ShareProposalStatusResponse rejectShareProposal(Long shareProposalId) {
        ShareProposal shareProposal = shareProposalRepository.findById(shareProposalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHARE_PROPOSAL_NOTFOUND));

        shareProposal.updateStatus(ProposalStatus.REJECTED);
        // 추가적으로 거절한 사람에게 거절 이유에 대해 쓰도록 한다던지, 부가적인 기능이 필요해보임. 프론트와 얘기.
        return ShareProposalStatusResponse.builder()
                .shareProposalId(shareProposal.getId())
                .status(shareProposal.getStatus())
                .build();

    }


}
