package com.ceos.beatbuddy.domain.venue.dto;

import com.ceos.beatbuddy.domain.venue.entity.Venue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class VenueInfoResponseDTO {
    private Venue venue;
    private Boolean isHeartbeat;
    private Boolean isCoupon;
    private List<String> tagList;
    
    // 언어별 응답을 위한 정적 메서드
    public static VenueInfoResponseDTO forLocale(Venue venue, Boolean isHeartbeat, Boolean isCoupon, List<String> tagList, String locale) {
        // 영어 요청의 경우 영어 필드로 대체
        if ("en".equals(locale)) {
            Venue translatedVenue = createTranslatedVenue(venue);
            return VenueInfoResponseDTO.builder()
                    .venue(translatedVenue)
                    .isHeartbeat(isHeartbeat)
                    .isCoupon(isCoupon)
                    .tagList(tagList)
                    .build();
        }
        
        // 기본(한국어) 응답
        return VenueInfoResponseDTO.builder()
                .venue(venue)
                .isHeartbeat(isHeartbeat)
                .isCoupon(isCoupon)
                .tagList(tagList)
                .build();
    }
    
    private static Venue createTranslatedVenue(Venue original) {
        return Venue.builder()
                .id(original.getId())
                .englishName(original.getEnglishName())
                .koreanName(original.getKoreanName())
                .region(original.getRegion())
                .isSmokingAllowed(original.isSmokingAllowed())
                .description(original.getDescriptionEng() != null ? original.getDescriptionEng() : original.getDescription())
                .address(original.getAddressEng() != null ? original.getAddressEng() : original.getAddress())
                .instaId(original.getInstaId())
                .instaUrl(original.getInstaUrl())
                .phoneNum(original.getPhoneNum())
                .entranceFee(original.getEntranceFee())
                .entranceNotice(original.getEntranceNoticeEng() != null ? original.getEntranceNoticeEng() : original.getEntranceNotice())
                .notice(original.getNoticeEng() != null ? original.getNoticeEng() : original.getNotice())
                .isFreeEntrance(original.isFreeEntrance())
                .latitude(original.getLatitude())
                .longitude(original.getLongitude())
                .operationHours(original.getOperationHours())
                .logoUrl(original.getLogoUrl())
                .backgroundUrl(original.getBackgroundUrl())
                .heartbeatNum(original.getHeartbeatNum())
                .build();
    }
}
