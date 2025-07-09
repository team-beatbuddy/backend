package com.ceos.beatbuddy.domain.admin.dto;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.report.entity.Report;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportSummaryDTO {
    private Long reportId;

    private String targetType;
    private Long targetId;

    private String targetTitle;
    private String targetContent; // 예: 게시글 제목, 댓글 내용 등

    private String reason;
    private Long reporterId;
    private String reporterNickname;

    private LocalDateTime reportedAt;

    public static ReportSummaryDTO toDTO(Report report) {
        return ReportSummaryDTO.builder()
                .reportId(report.getId())
                .targetType(report.getTargetType().name())
                .targetId(report.getTargetId())
                .targetTitle(report.getTitle())
                .targetContent(report.getContent())
                .reason(report.getReason())
                .reporterId(report.getReporter().getId())
                .reporterNickname(report.getReporter().getNickname())
                .reportedAt(report.getCreatedAt())
                .build();
    }
}