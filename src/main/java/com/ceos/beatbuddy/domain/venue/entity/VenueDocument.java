package com.ceos.beatbuddy.domain.venue.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueDocument {
    private Long id;
    private String englishName;
    private String koreanName;
    private String address;

    public static VenueDocument from(Venue venue) {
        return VenueDocument.builder()
                .id(venue.getId())
                .englishName(venue.getEnglishName())
                .koreanName(venue.getKoreanName())
                .address(venue.getAddress())
                .build();
    }
}