package io.crops.warmletter.domain.share.service;
import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.share.dto.request.ShareProposalRequest;
import io.crops.warmletter.domain.share.dto.response.ShareInboxResponse;
import io.crops.warmletter.domain.share.dto.response.ShareProposalDetailResponse;
import io.crops.warmletter.domain.share.dto.response.ShareProposalResponse;
import io.crops.warmletter.domain.share.dto.response.ShareProposalStatusResponse;
import io.crops.warmletter.domain.share.entity.SharePost;
import io.crops.warmletter.domain.share.entity.ShareProposal;
import io.crops.warmletter.domain.share.entity.ShareProposalLetter;
import io.crops.warmletter.domain.share.enums.ProposalStatus;
import io.crops.warmletter.domain.share.exception.ShareAccessException;
import io.crops.warmletter.domain.share.exception.ShareProposalNotFoundException;
import io.crops.warmletter.domain.share.repository.*;
import io.crops.warmletter.domain.timeline.dto.request.NotificationRequest;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.facade.NotificationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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

    private final ApplicationEventPublisher notificationPublisher;

    @Transactional
    public ShareProposalResponse requestShareProposal(ShareProposalRequest request) {

        Long requesterId = authFacade.getCurrentUserId();

        ShareProposal shareProposal = shareProposalRepository.save(request.toEntity(requesterId));

        List<ShareProposalLetter> letters = request.getLetterIds().stream()
                .map(letterId -> new ShareProposalLetter(shareProposal.getId(), letterId))
                .collect(Collectors.toList());
        shareProposalLetterRepository.saveAll(letters);

        ShareProposalResponse response = shareProposalRepository.findShareProposalWithZipCode(shareProposal.getId());
        if (response == null) {
            throw new ShareProposalNotFoundException();
        }
        // 알림 전송
        notificationPublisher.publishEvent(NotificationRequest.builder()
                .senderZipCode(response.getZipCode())
                .receiverId(request.getRecipientId())
                .alarmType(AlarmType.SHARE)
                .data(response.getShareProposalId().toString())
                .build());
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
        sharePost = sharePostRepository.save(sharePost);
        // 알림 전송(양쪽 다)
        String requestZipCode = shareProposalRepository.findZipCodeByRequesterId(shareProposal.getRequesterId());
        String recipientZipCode = shareProposalRepository.findZipCodeByRecipientId(shareProposal.getRecipientId());
        notificationPublisher.publishEvent(NotificationRequest.builder()
                .senderZipCode(recipientZipCode)
                .receiverId(shareProposal.getRequesterId())
                .alarmType(AlarmType.POSTED)
                .data(sharePost.getId().toString())
                .build());
        notificationPublisher.publishEvent(NotificationRequest.builder()
                .senderZipCode(requestZipCode)
                .receiverId(shareProposal.getRecipientId())
                .alarmType(AlarmType.POSTED)
                .data(sharePost.getId().toString())
                .build());
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

    public List<ShareInboxResponse> getReceivedShareProposals() {
        Long memberId = authFacade.getCurrentUserId();

        return shareProposalRepository.getAllByRecipientIdOrderByCreatedAtDesc(memberId);
    }

    public ShareProposalDetailResponse getShareProposalDetail(Long shareProposalId) {
        Long memberId = authFacade.getCurrentUserId();

        ShareProposal shareProposal = shareProposalRepository.findById(shareProposalId)
                .orElseThrow(() -> new ShareProposalNotFoundException());

        if (!shareProposal.getRecipientId().equals(memberId)) {
            throw new ShareAccessException();
        }

        return shareProposalRepository.findShareProposalDetailById(shareProposalId);
    }
}
