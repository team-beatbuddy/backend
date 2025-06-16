package com.ceos.beatbuddy.domain.vector.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.Map;

@Getter
public class MoodRequestDTO {
    @NotNull
    @Schema(description = "\"CLUB\", \"PUB\", \"ROOFTOP\", \"DEEP\", \"COMMERCIAL\", \"CHILL\", \"EXOTIC\", \"HUNTING\", \"BAR&CAFE\" 의 값들을 각각 기입해주세요. " +
            "이들의 기입 순서는 상관없습니다. 내부적으로 자동 매핑됩니다.",
            example = "{\"CLUB\": 1.0, \"ROOFTOP\": 1.0, \"CHILL\": 1.0, \"BAR&CAFE\": 1.0}")
    private Map<String, Double> moodPreferences;
}
