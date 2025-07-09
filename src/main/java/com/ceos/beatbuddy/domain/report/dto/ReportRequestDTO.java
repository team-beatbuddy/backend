package com.ceos.beatbuddy.domain.report.dto;

import com.ceos.beatbuddy.domain.report.entity.Report;
import com.ceos.beatbuddy.domain.report.entity.ReportTargetType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO {
    @NotNull(message = "신고 대상 타입은 필수입니다.")
    private String targetType;
    @NotNull(message = "신고 대상 ID는 필수입니다.")
    private Long targetId;
    @NotNull(message = "신고 사유는 필수입니다.")
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
