package io.crops.warmletter.domain.share.repository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.crops.warmletter.domain.member.entity.QMember;
import io.crops.warmletter.domain.share.dto.response.ShareInboxResponse;
import io.crops.warmletter.domain.share.dto.response.ShareProposalResponse;
import lombok.RequiredArgsConstructor;
import java.util.List;
import static io.crops.warmletter.domain.member.entity.QMember.member;
import static io.crops.warmletter.domain.share.entity.QShareProposal.shareProposal;

@RequiredArgsConstructor
public class ShareProposalRepositoryImpl implements ShareProposalRepositoryCustom {

    private final JPAQueryFactory queryFactory;

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
        QMember requesterMember = new QMember("requesterMember");
        QMember recipientMember = new QMember("recipientMember");

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
                .where(shareProposal.recipientId.eq(receiverId))
                .orderBy(shareProposal.createdAt.desc())
                .fetch();
    }
}
