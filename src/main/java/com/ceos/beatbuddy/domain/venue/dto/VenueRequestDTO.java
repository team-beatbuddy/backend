package com.ceos.beatbuddy.domain.venue.dto;

import com.ceos.beatbuddy.domain.member.constant.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class VenueRequestDTO {
    private boolean smokingAllowed;
    private String englishName;
    private String koreanName;

    private Region region;
    private Map<String,String> weeklyOperationHours;
    private String description;
    private String address;
    private String instaId;
    private String instaUrl;
    private String phoneNum;

    @Schema(description = "입장료", example = "15000")
    private int entranceFee; // 입장료
    @Schema(description = "입장료 공지", example = "입장료는 현장에서 결제해주세요.")
    private String entranceNotice; // 입장료 공지
    @Schema(description = "장소 공지사항", example = "장소 관련 공지사항입니다.")
    private String notice;
    @Schema(description = "무료 입장 여부", example = "false")
    private boolean isFreeEntrance; // 무료 입장 여부
}
