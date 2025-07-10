package com.ceos.beatbuddy.domain.venue.dto;

import com.ceos.beatbuddy.domain.venue.entity.VenueDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class VenueSearchResponseDTO {
    private Long venueId;
    private String koreanName;
    private String englishName;
    private String address;

    public static VenueSearchResponseDTO toDTO(VenueDocument venue) {
        return VenueSearchResponseDTO.builder()
                .venueId(venue.getId())
                .koreanName(venue.getKoreanName())
                .englishName(venue.getEnglishName())
                .address(venue.getAddress())
                .build();
    }
}
