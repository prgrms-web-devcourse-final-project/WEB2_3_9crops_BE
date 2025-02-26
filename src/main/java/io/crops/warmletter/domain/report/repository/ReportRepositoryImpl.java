package io.crops.warmletter.domain.report.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.crops.warmletter.domain.eventpost.entity.QEventComment;
import io.crops.warmletter.domain.letter.entity.QLetter;
import io.crops.warmletter.domain.member.entity.QMember;
import io.crops.warmletter.domain.report.dto.response.ReportsResponse;
import io.crops.warmletter.domain.report.entity.QReport;
import io.crops.warmletter.domain.report.enums.ReportStatus;
import io.crops.warmletter.domain.report.enums.ReportType;
import io.crops.warmletter.domain.share.entity.QSharePost;
import io.crops.warmletter.domain.share.entity.QShareProposal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ReportsResponse> findAllWithFilters(String reportType, String status, Pageable pageable) {
        QReport report = QReport.report;
        QLetter letter = QLetter.letter;
        QSharePost sharePost = QSharePost.sharePost;
        QEventComment eventComment = QEventComment.eventComment;
        QShareProposal shareProposal = QShareProposal.shareProposal;
        QMember reporter = new QMember("reporter");
        QMember target = new QMember("target");

        JPAQuery<ReportsResponse> query = queryFactory
                .select(Projections.constructor(
                        ReportsResponse.class,
                        report.id,
                        report.memberId,  // reporterId 추가
                        reporter.email,
                        Expressions.cases()
                                .when(report.letterId.isNotNull()).then(letter.writerId)
                                .when(report.sharePostId.isNotNull()).then(shareProposal.requesterId)
                                .when(report.eventCommentId.isNotNull()).then(eventComment.writerId)
                                .otherwise((Long) null),  // targetId 추가
                        target.email,
                        report.reportType.stringValue(),
                        report.reasonType.stringValue(),
                        report.reason,
                        report.reportStatus.stringValue(),
                        report.reportStartedAt,
                        report.letterId,
                        report.sharePostId,
                        report.eventCommentId,
                        Projections.constructor(
                                ReportsResponse.ContentDetail.class,
                                Expressions.cases()
                                        .when(report.letterId.isNotNull()).then(letter.title)
                                        .otherwise((String) null),
                                Expressions.cases()
                                        .when(report.letterId.isNotNull()).then(letter.content)
                                        .when(report.sharePostId.isNotNull()).then(sharePost.content)
                                        .when(report.eventCommentId.isNotNull()).then(eventComment.content)
                                        .otherwise((String) null)
                        )
                ))
                .from(report)
                .leftJoin(reporter).on(reporter.id.eq(report.memberId)) // 신고자
                .leftJoin(letter).on(report.letterId.eq(letter.id)) // 편지
                .leftJoin(sharePost).on(report.sharePostId.eq(sharePost.id)) // 공유게시글
                .leftJoin(shareProposal).on(shareProposal.id.eq(sharePost.id)) // 공유요청
                .leftJoin(eventComment).on(report.eventCommentId.eq(eventComment.id)) // 이벤트 댓글
                .leftJoin(target).on(
                        report.letterId.isNotNull().and(target.id.eq(letter.writerId))
                                .or(report.sharePostId.isNotNull().and(target.id.eq(shareProposal.requesterId)))
                                .or(report.eventCommentId.isNotNull().and(target.id.eq(eventComment.writerId)))
                );


        if (reportType != null) {
            try {
                ReportType validReportType = ReportType.valueOf(reportType.toUpperCase()); // ✅ String → Enum 변환
                query.where(report.reportType.eq(validReportType));  // ✅ Enum으로 비교
            } catch (IllegalArgumentException ignored) {
                // 변환 실패하면 필터링 안 함 (전체 조회)
            }
        }
        if (status != null) {
            try {
                ReportStatus validStatus = ReportStatus.valueOf(status.toUpperCase()); // ✅ String → Enum 변환
                query.where(report.reportStatus.eq(validStatus));  // ✅ Enum으로 비교
            } catch (IllegalArgumentException ignored) {
                // 변환 실패하면 필터링 안 함 (전체 조회)
            }
        }

        List<ReportsResponse> reports = query
                .orderBy(report.reportStartedAt.desc()) // 최신 신고순 정렬 추가
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        JPAQuery<Long> countQuery = queryFactory
                .select(report.count())
                .from(report);

        return PageableExecutionUtils.getPage(reports, pageable, countQuery::fetchOne);
    }
}
