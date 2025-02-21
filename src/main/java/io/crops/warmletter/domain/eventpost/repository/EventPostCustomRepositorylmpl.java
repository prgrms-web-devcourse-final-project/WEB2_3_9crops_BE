package io.crops.warmletter.domain.eventpost.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostCommentsResponse;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostDetailResponse;
import io.crops.warmletter.domain.eventpost.entity.EventPost;
import org.springframework.stereotype.Repository;

import java.util.List;

import static io.crops.warmletter.domain.eventpost.entity.QEventPost.eventPost;
import static io.crops.warmletter.domain.eventpost.entity.QEventComment.eventComment;
import static io.crops.warmletter.domain.member.entity.QMember.member;

@Repository
public class EventPostCustomRepositorylmpl implements EventPostCustomRepository {
    private final JPAQueryFactory queryFactory;

    public EventPostCustomRepositorylmpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public EventPostDetailResponse findEventPostDetailById(long eventPostId) {
        // 이벤트 게시글 정보 조회
        EventPost post = queryFactory
                .selectFrom(eventPost)
                .where(eventPost.id.eq(eventPostId))
                .fetchOne();
        if (post == null) {
            return null;
        }
        // 댓글 리스트 조회
        List<EventPostCommentsResponse> comments = queryFactory
                .select(Projections.constructor(EventPostCommentsResponse.class,
                        eventComment.id,
                        member.zipCode,
                        eventComment.content))
                .from(eventComment)
                .join(member).on(eventComment.writerId.eq(member.id))
                .where(eventComment.eventPostId.eq(eventPostId))
                .fetch();

        return new EventPostDetailResponse(post.getId(),post.getTitle(),comments);
    }
}
