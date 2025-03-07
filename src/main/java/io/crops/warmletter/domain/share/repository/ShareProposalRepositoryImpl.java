package io.crops.warmletter.domain.share.repository;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.crops.warmletter.domain.letter.entity.QLetter;
import io.crops.warmletter.domain.member.entity.QMember;
import io.crops.warmletter.domain.share.dto.response.ShareInboxResponse;
import io.crops.warmletter.domain.share.dto.response.ShareLetterPostResponse;
import io.crops.warmletter.domain.share.dto.response.ShareProposalDetailResponse;
import io.crops.warmletter.domain.share.dto.response.ShareProposalResponse;
import io.crops.warmletter.domain.share.entity.QShareProposal;
import io.crops.warmletter.domain.share.entity.QShareProposalLetter;
import io.crops.warmletter.domain.share.enums.ProposalStatus;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ShareProposalRepositoryImpl implements ShareProposalRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QMember MEMBER = new QMember("member");
    private static final QShareProposal SHARE_PROPOSAL = new QShareProposal("shareProposal");
    private static final QMember REQUESTER_MEMBER = new QMember("requesterMember");
    private static final QMember RECIPIENT_MEMBER = new QMember("recipientMember");

    @Override
    public ShareProposalResponse findShareProposalWithZipCode(Long shareProposalId) {
        return queryFactory
                .select(Projections.constructor(ShareProposalResponse.class,
                        SHARE_PROPOSAL.id,
                        MEMBER.zipCode))
                .from(SHARE_PROPOSAL)
                .join(MEMBER).on(SHARE_PROPOSAL.requesterId.eq(MEMBER.id))
                .where(SHARE_PROPOSAL.id.eq(shareProposalId))
                .fetchOne();
    }

    @Override
    public List<ShareInboxResponse> getAllByRecipientIdOrderByCreatedAtDesc(Long receiverId) {
        return queryFactory
                .select(Projections.constructor(ShareInboxResponse.class,
                        SHARE_PROPOSAL.id.as("shareProposalId"),
                        REQUESTER_MEMBER.zipCode.as("requesterZipCode"),
                        RECIPIENT_MEMBER.zipCode.as("recipientZipCode"),
                        SHARE_PROPOSAL.message,
                        SHARE_PROPOSAL.status))
                .from(SHARE_PROPOSAL)
                .join(REQUESTER_MEMBER).on(SHARE_PROPOSAL.requesterId.eq(REQUESTER_MEMBER.id))
                .join(RECIPIENT_MEMBER).on(SHARE_PROPOSAL.recipientId.eq(RECIPIENT_MEMBER.id))
                .where(SHARE_PROPOSAL.recipientId.eq(receiverId)
                        .and(SHARE_PROPOSAL.status.eq(ProposalStatus.PENDING))
                )
                .orderBy(SHARE_PROPOSAL.createdAt.desc())
                .fetch();
    }

    @Override
    public ShareProposalDetailResponse findShareProposalDetailById(Long shareProposalId) {

        QShareProposalLetter shareProposalLetter = new QShareProposalLetter("shareProposalLetter");
        QLetter letter = new QLetter("letter");
        QMember letterWriterMember = new QMember("letterWriterMember");
        QMember letterReceiverMember = new QMember("letterReceiverMember");

        List<Tuple> results = queryFactory
                .select(
                        SHARE_PROPOSAL.id,
                        REQUESTER_MEMBER.zipCode,
                        RECIPIENT_MEMBER.zipCode,
                        SHARE_PROPOSAL.message,
                        SHARE_PROPOSAL.status,
                        letter.id,
                        letter.content,
                        letterWriterMember.zipCode,
                        letterReceiverMember.zipCode,
                        letter.createdAt
                )
                .from(SHARE_PROPOSAL)
                .join(REQUESTER_MEMBER).on(SHARE_PROPOSAL.requesterId.eq(REQUESTER_MEMBER.id))
                .join(RECIPIENT_MEMBER).on(SHARE_PROPOSAL.recipientId.eq(RECIPIENT_MEMBER.id))
                .join(shareProposalLetter).on(SHARE_PROPOSAL.id.eq(shareProposalLetter.proposalId))
                .leftJoin(letter).on(shareProposalLetter.letterId.eq(letter.id))
                .leftJoin(letterWriterMember).on(letter.writerId.eq(letterWriterMember.id))
                .leftJoin(letterReceiverMember).on(letter.receiverId.eq(letterReceiverMember.id))
                .where(SHARE_PROPOSAL.id.eq(shareProposalId))
                .fetch();

        if (results.isEmpty()) {
            return null;
        }
        Tuple firstRow = results.get(0);
        List<ShareLetterPostResponse> letters = results.stream()
                .map(tuple -> ShareLetterPostResponse.builder()
                        .id(tuple.get(letter.id))
                        .content(tuple.get(letter.content))
                        .writerZipCode(tuple.get(letterWriterMember.zipCode))
                        .receiverZipCode(tuple.get(letterReceiverMember.zipCode))
                        .createdAt(tuple.get(letter.createdAt))
                        .build())
                .collect(Collectors.toList());

        return ShareProposalDetailResponse.builder()
                .shareProposalId(firstRow.get(SHARE_PROPOSAL.id))
                .requesterZipCode(firstRow.get(REQUESTER_MEMBER.zipCode))
                .recipientZipCode(firstRow.get(RECIPIENT_MEMBER.zipCode))
                .message(firstRow.get(SHARE_PROPOSAL.message))
                .status(firstRow.get(SHARE_PROPOSAL.status))
                .letters(letters)
                .build();
    }
}
