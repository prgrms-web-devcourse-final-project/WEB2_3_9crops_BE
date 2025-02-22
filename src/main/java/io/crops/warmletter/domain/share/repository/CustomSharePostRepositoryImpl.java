package io.crops.warmletter.domain.share.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.crops.warmletter.domain.letter.entity.QLetter;
import io.crops.warmletter.domain.member.entity.QMember;
import io.crops.warmletter.domain.share.dto.response.ShareLetterPostResponse;
import io.crops.warmletter.domain.share.dto.response.SharePostDetailResponse;
import io.crops.warmletter.domain.share.entity.QSharePost;
import io.crops.warmletter.domain.share.entity.QShareProposal;
import io.crops.warmletter.domain.share.entity.QShareProposalLetter;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class CustomSharePostRepositoryImpl implements CustomSharePostRepository {
    private final JPAQueryFactory queryFactory;

    public CustomSharePostRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    private static final QSharePost SHARE_POST = QSharePost.sharePost;
    private static final QShareProposal PROPOSAL = QShareProposal.shareProposal;
    private static final QShareProposalLetter PROPOSAL_LETTER = QShareProposalLetter.shareProposalLetter;
    private static final QLetter LETTER = QLetter.letter;
    private static final QMember WRITER = new QMember("writer");
    private static final QMember RECEIVER = new QMember("receiver");

    @Override
    public Optional<SharePostDetailResponse> findDetailById(Long sharePostId) {
        List<Tuple> results = queryFactory
                .select(
                        SHARE_POST.id,
                        PROPOSAL.message,
                        LETTER.id,
                        LETTER.content,
                        WRITER.zipCode,
                        RECEIVER.zipCode,
                        LETTER.createdAt
                )
                .from(SHARE_POST)
                .leftJoin(PROPOSAL).on(SHARE_POST.shareProposalId.eq(PROPOSAL.id))
                .leftJoin(PROPOSAL_LETTER).on(PROPOSAL.id.eq(PROPOSAL_LETTER.proposalId))
                .leftJoin(LETTER).on(PROPOSAL_LETTER.letterId.eq(LETTER.id))
                .leftJoin(WRITER).on(LETTER.writerId.eq(WRITER.id))
                .leftJoin(RECEIVER).on(LETTER.receiverId.eq(RECEIVER.id))
                .where(SHARE_POST.id.eq(sharePostId)
                        .and(SHARE_POST.isActive.isTrue()))
                .fetch();

        if (results.isEmpty()) {
            return Optional.empty();
        }

        SharePostDetailResponse response = SharePostDetailResponse.builder()
                .sharePostId(results.get(0).get(SHARE_POST.id))
                .zipCode(results.get(0).get(WRITER.zipCode))
                .sharePostContent(results.get(0).get(PROPOSAL.message))
                .letters(results.stream()
                        .map(tuple -> ShareLetterPostResponse.builder()
                                .id(tuple.get(LETTER.id))
                                .content(tuple.get(LETTER.content))
                                .writerZipCode(tuple.get(WRITER.zipCode))
                                .receiverZipCode(tuple.get(RECEIVER.zipCode))
                                .createdAt(tuple.get(LETTER.createdAt))
                                .build())
                        .collect(Collectors.toList()))
                .build();

        return Optional.of(response);
    }
}

