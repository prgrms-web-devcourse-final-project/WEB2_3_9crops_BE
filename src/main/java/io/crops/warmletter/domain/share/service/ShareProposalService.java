package io.crops.warmletter.domain.share.service;

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
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.facade.NotificationFacade;
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

    private final NotificationFacade notificationFacade;

    @Transactional
    public ShareProposalResponse requestShareProposal(ShareProposalRequest request) {
        if (request.getRequesterId() == null || request.getRecipientId() == null ||
                request.getLetters() == null || request.getLetters().isEmpty()) {
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
        // 알림 전송 TODO : 배포 후 테스트 예정
        notificationFacade.sendNotification(response.getZipCode(), request.getRecipientId(), AlarmType.SHARE, response.getShareProposalId().toString());
        return response;

    }

    @Transactional
    public ShareProposalStatusResponse approveShareProposal(Long shareProposalId) {
        ShareProposal shareProposal = shareProposalRepository.findById(shareProposalId)
                .orElseThrow(() -> new ShareProposalNotFoundException());

        shareProposal.updateStatus(ProposalStatus.APPROVED);

        SharePost sharePost = SharePost.builder()
                .shareProposalId(shareProposal.getId())
                .content(shareProposal.getMessage())
                .isActive(true)
                .build();
        sharePost = sharePostRepository.save(sharePost);
        // 알림 전송(양쪽다) / 인가 값이 없어서 일단 우편번호는 임시값으로 대체 TODO : 배포 후 테스트 예정
        notificationFacade.sendNotification("승인요청자", shareProposal.getRequesterId(), AlarmType.POSTED, sharePost.getId().toString());
        notificationFacade.sendNotification("승인수락자", shareProposal.getRecipientId(), AlarmType.POSTED, sharePost.getId().toString());
        return ShareProposalStatusResponse.builder()
                .shareProposalId(shareProposal.getId())
                .status(shareProposal.getStatus())
                .sharePostId(sharePost.getId())
                .build();
    }

    @Transactional
    public ShareProposalStatusResponse rejectShareProposal(Long shareProposalId) {
        ShareProposal shareProposal = shareProposalRepository.findById(shareProposalId)
                .orElseThrow(() -> new ShareProposalNotFoundException());

        shareProposal.updateStatus(ProposalStatus.REJECTED);
        return ShareProposalStatusResponse.builder()
                .shareProposalId(shareProposal.getId())
                .status(shareProposal.getStatus())
                .build();

    }


}
