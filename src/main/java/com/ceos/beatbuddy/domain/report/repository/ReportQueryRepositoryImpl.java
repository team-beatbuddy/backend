package com.ceos.beatbuddy.domain.report.repository;

import com.ceos.beatbuddy.domain.member.entity.QMember;
import com.ceos.beatbuddy.domain.report.entity.QReport;
import com.ceos.beatbuddy.domain.report.entity.Report;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReportQueryRepositoryImpl implements ReportQueryRepository{
    private final JPAQueryFactory queryFactory;


    public List<Report> getAllReports() {
        QReport report = QReport.report;
        QMember reporter = QMember.member;

        return queryFactory
                .select(report)
                .from(report)
                .join(report.reporter, reporter).fetchJoin()
                .orderBy(report.createdAt.desc())
                .fetch();
    }
}
