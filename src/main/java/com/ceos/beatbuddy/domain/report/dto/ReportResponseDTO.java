package com.ceos.beatbuddy.domain.report.dto;

import com.ceos.beatbuddy.domain.report.entity.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponseDTO {
    private Long id;
    private ReportTargetType targetType;
    private Long targetId;
    private String reason;
    private String reporterNickname;
    private LocalDateTime createdAt;
}