package com.ceos.beatbuddy.domain.report.dto;

import com.ceos.beatbuddy.domain.report.entity.Report;
import com.ceos.beatbuddy.domain.report.entity.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO {
    private String targetType;
    private Long targetId;
    private String reason;

    public static Report toEntity(ReportRequestDTO reportRequestDTO, String title, String content) {
        return Report.builder()
                .targetType(ReportTargetType.from(reportRequestDTO.getTargetType()))
                .targetId(reportRequestDTO.getTargetId())
                .reason(reportRequestDTO.getReason())
                .title(title)
                .content(content)
                .build();
    }
}
