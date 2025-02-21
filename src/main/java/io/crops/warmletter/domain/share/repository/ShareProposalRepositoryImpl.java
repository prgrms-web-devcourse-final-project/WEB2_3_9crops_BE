package io.crops.warmletter.domain.share.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.crops.warmletter.domain.share.dto.response.ShareProposalResponse;
import lombok.RequiredArgsConstructor;

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

}