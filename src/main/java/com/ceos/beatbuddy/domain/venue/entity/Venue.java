package com.ceos.beatbuddy.domain.venue.entity;

import com.ceos.beatbuddy.domain.member.constant.Region;
import com.ceos.beatbuddy.domain.venue.dto.VenueRequestDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueUpdateDTO;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Map;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Venue extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String englishName;
    private String koreanName;

    @Enumerated
    private Region region;
    private boolean isSmokingAllowed;
    private String description;
    @Column(nullable = false)
    private String address;
    private String instaId;
    private String instaUrl;
    private String phoneNum;

    private int entranceFee; // 입장료
    private String entranceNotice; // 입장료 공지
    private String notice;
    private boolean isFreeEntrance; // 무료 입장 여부

    private double latitude;

    private double longitude;

    @ElementCollection
    private Map<String,String> operationHours;

    private String logoUrl;
    @ElementCollection
    private List<String> backgroundUrl;

    @Builder.Default
    private Long heartbeatNum = 0L;

    public void updateLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public void updateBackgroundUrl(List<String> backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }

    public void update(VenueUpdateDTO venueUpdateDTO) {
        this.isSmokingAllowed = venueUpdateDTO.getVenueRequestDTO().isSmokingAllowed();
        this.englishName = venueUpdateDTO.getVenueRequestDTO().getEnglishName();
        this.koreanName = venueUpdateDTO.getVenueRequestDTO().getKoreanName();
        this.region = venueUpdateDTO.getVenueRequestDTO().getRegion();
        this.description = venueUpdateDTO.getVenueRequestDTO().getDescription();
        this.address = venueUpdateDTO.getVenueRequestDTO().getAddress();
        this.instaId = venueUpdateDTO.getVenueRequestDTO().getInstaId();
        this.instaUrl = venueUpdateDTO.getVenueRequestDTO().getInstaUrl();
        this.phoneNum = venueUpdateDTO.getVenueRequestDTO().getPhoneNum();
        this.operationHours = venueUpdateDTO.getVenueRequestDTO().getWeeklyOperationHours();
        this.entranceNotice = venueUpdateDTO.getVenueRequestDTO().getEntranceNotice();
        this.notice = venueUpdateDTO.getVenueRequestDTO().getNotice();

        this.isFreeEntrance = venueUpdateDTO.getVenueRequestDTO().isFreeEntrance();
        if (this.isFreeEntrance) {
            this.entranceFee = 0; // 무료일 경우 금액 무시
        } else {
            this.entranceFee = venueUpdateDTO.getVenueRequestDTO().getEntranceFee(); // 유료일 경우만 금액 반영
        }
    }

    public static Venue of(VenueRequestDTO request, String  logoUrl, List<String> backgroundUrl){
        int entranceFee = request.isFreeEntrance() ? 0 : request.getEntranceFee();

        return Venue.builder()
                .isSmokingAllowed(request.isSmokingAllowed())
                .englishName(request.getEnglishName())
                .koreanName(request.getKoreanName())
                .region(request.getRegion())
                .address(request.getAddress())
                .description(request.getDescription())
                .phoneNum(request.getPhoneNum())
                .instaId(request.getInstaId())
                .instaUrl(request.getInstaUrl())
                .operationHours(request.getWeeklyOperationHours())
                .logoUrl(logoUrl)
                .backgroundUrl(backgroundUrl)
                .entranceFee(entranceFee)
                .entranceNotice(request.getEntranceNotice())
                .notice(request.getNotice())
                .isFreeEntrance(request.isFreeEntrance())
                .build();
    }
}
