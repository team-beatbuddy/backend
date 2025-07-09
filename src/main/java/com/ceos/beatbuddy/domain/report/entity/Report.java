package com.ceos.beatbuddy.domain.report.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reporterId", nullable = false)
    private Member reporter;

    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType;

    private Long targetId;

    private String title;
    @Lob
    private String content;

    private String reason;

    public void setReporter(Member reporter) {
        this.reporter = reporter;
    }
}
