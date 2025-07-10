package com.ceos.beatbuddy.domain.venue.entity;

import com.ceos.beatbuddy.domain.event.entity.Event;
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
    private String address;
    private String instaId;
    private String instaUrl;
    private String phoneNum;

    private String entranceFee; // 입장료
    private String notice;

    @ElementCollection
    private Map<String,String> operationHours;

    private String logoUrl;
    @ElementCollection
    private List<String> backgroundUrl;

    @Builder.Default
    private Long heartbeatNum = 0L;

    public void addHeartbeatNum() {
        if(this.heartbeatNum==null) {
            this.heartbeatNum = 1L;
        }
        this.heartbeatNum += 1;
    }

    public void deleteHeartbeatNum() {
        if(this.heartbeatNum > 0) {
            this.heartbeatNum -= 1;
        }
    }

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
        this.entranceFee = venueUpdateDTO.getVenueRequestDTO().getEntranceFee();
        this.notice = venueUpdateDTO.getVenueRequestDTO().getNotice();
    }

    public static Venue of(VenueRequestDTO request, String  logoUrl, List<String> backgroundUrl){
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
                .notice(request.getNotice())
                .entranceFee(request.getEntranceFee())
                .build();
    }
}
