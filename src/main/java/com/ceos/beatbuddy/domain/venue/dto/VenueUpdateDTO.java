package com.ceos.beatbuddy.domain.venue.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VenueUpdateDTO {
    @JsonUnwrapped
    private VenueRequestDTO venueRequestDTO;
    private List<String> deleteImageUrls;
}
