package io.crops.warmletter.domain.share.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.share.dto.request.ShareProposalRequest;
import io.crops.warmletter.domain.share.dto.response.ShareProposalResponse;
import io.crops.warmletter.domain.share.dto.response.ShareProposalStatusResponse;
import io.crops.warmletter.domain.share.entity.SharePost;
import io.crops.warmletter.domain.share.entity.ShareProposal;
import io.crops.warmletter.domain.share.entity.ShareProposalLetter;
import io.crops.warmletter.domain.share.enums.ProposalStatus;
import io.crops.warmletter.domain.share.exception.ShareInvalidInputValue;
import io.crops.warmletter.domain.share.exception.ShareProposalNotFoundException;
import io.crops.warmletter.domain.share.repository.*;
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
    private final AuthFacade authFacade;

    @Transactional
    public ShareProposalResponse requestShareProposal(ShareProposalRequest request) {

        Long currentUserId = authFacade.getCurrentUserId();

        if (!currentUserId.equals(request.getRequesterId())) {
            throw new ShareInvalidInputValue();
        }

        ShareProposal shareProposal = shareProposalRepository.save(request.toEntity());

        List<ShareProposalLetter> letters = request.getLetters().stream()
                .map(letterId -> new ShareProposalLetter(shareProposal.getId(), letterId))
                .collect(Collectors.toList());
        shareProposalLetterRepository.saveAll(letters);

        ShareProposalResponse response = shareProposalRepository.findShareProposalWithZipCode(shareProposal.getId());
        if (response == null) {
            throw new ShareProposalNotFoundException();
        }
        return response;
    }

    @Transactional
    public ShareProposalStatusResponse approveShareProposal(Long shareProposalId) {

        Long memberId = authFacade.getCurrentUserId();

        ShareProposal shareProposal = shareProposalRepository.findById(shareProposalId)
                .orElseThrow(() -> new ShareProposalNotFoundException());

        if (!memberId.equals(shareProposal.getRecipientId())) {
            throw new ShareProposalNotFoundException();
        }


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

        Long memberId = authFacade.getCurrentUserId();

        ShareProposal shareProposal = shareProposalRepository.findById(shareProposalId)
                .orElseThrow(() -> new ShareProposalNotFoundException());

        if (!memberId.equals(shareProposal.getRecipientId())) {
            throw new ShareProposalNotFoundException();
        }

        shareProposal.updateStatus(ProposalStatus.REJECTED);
        return ShareProposalStatusResponse.builder()
                .shareProposalId(shareProposal.getId())
                .status(shareProposal.getStatus())
                .build();
    }
}
