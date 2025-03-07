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
import static io.crops.warmletter.domain.member.entity.QMember.member;
import static io.crops.warmletter.domain.share.entity.QShareProposal.shareProposal;

@RequiredArgsConstructor
public class ShareProposalRepositoryImpl implements ShareProposalRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QMember requesterMember = new QMember("requesterMember");
    private final QMember recipientMember = new QMember("recipientMember");

    @Override
    public ShareProposalResponse findShareProposalWithZipCode(Long shareProposalId) {
        return queryFactory
                .select(Projections.constructor(ShareProposalResponse.class,
                        shareProposal.id,
                        member.zipCode))
                .from(shareProposal)
                .join(member).on(shareProposal.requesterId.eq(member.id))
                .where(shareProposal.id.eq(shareProposalId))
                .fetchOne();
    }

    @Override
    public List<ShareInboxResponse> getAllByRecipientIdOrderByCreatedAtDesc(Long receiverId) {

        return queryFactory
                .select(Projections.constructor(ShareInboxResponse.class,
                        shareProposal.id.as("shareProposalId"),
                        requesterMember.zipCode.as("requesterZipCode"),
                        recipientMember.zipCode.as("recipientZipCode"),
                        shareProposal.message,
                        shareProposal.status))
                .from(shareProposal)
                .join(requesterMember).on(shareProposal.requesterId.eq(requesterMember.id))
                .join(recipientMember).on(shareProposal.recipientId.eq(recipientMember.id))
                .where(shareProposal.recipientId.eq(receiverId)
                        .and(shareProposal.status.eq(ProposalStatus.PENDING))
                )
                .orderBy(shareProposal.createdAt.desc())
                .fetch();
    }

    @Override
    public ShareProposalDetailResponse findShareProposalDetailById(Long shareProposalId) {
        QShareProposal shareProposal = QShareProposal.shareProposal;
        QShareProposalLetter shareProposalLetter = QShareProposalLetter.shareProposalLetter;
        QLetter letter = QLetter.letter;
        QMember letterWriterMember = new QMember("letterWriterMember");
        QMember letterReceiverMember = new QMember("letterReceiverMember");

        List<Tuple> results = queryFactory
                .select(
                        shareProposal.id,
                        requesterMember.zipCode,
                        recipientMember.zipCode,
                        shareProposal.message,
                        shareProposal.status,
                        letter.id,
                        letter.content,
                        letterWriterMember.zipCode,
                        letterReceiverMember.zipCode,
                        letter.createdAt
                )
                .from(shareProposal)
                .join(requesterMember).on(shareProposal.requesterId.eq(requesterMember.id))
                .join(recipientMember).on(shareProposal.recipientId.eq(recipientMember.id))
                .leftJoin(shareProposalLetter).on(shareProposal.id.eq(shareProposalLetter.proposalId))
                .leftJoin(letter).on(shareProposalLetter.letterId.eq(letter.id))
                .leftJoin(letterWriterMember).on(letter.writerId.eq(letterWriterMember.id))
                .leftJoin(letterReceiverMember).on(letter.receiverId.eq(letterReceiverMember.id))
                .where(shareProposal.id.eq(shareProposalId))
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
                .shareProposalId(firstRow.get(shareProposal.id))
                .requesterZipCode(firstRow.get(requesterMember.zipCode))
                .recipientZipCode(firstRow.get(recipientMember.zipCode))
                .message(firstRow.get(shareProposal.message))
                .status(firstRow.get(shareProposal.status))
                .letters(letters)
                .build();
    }
}
