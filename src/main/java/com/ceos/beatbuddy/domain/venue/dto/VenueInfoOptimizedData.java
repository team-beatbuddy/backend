package com.ceos.beatbuddy.domain.venue.dto;

import com.ceos.beatbuddy.domain.venue.entity.Venue;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class VenueInfoOptimizedData {
    private final Venue venue;
    private final boolean isHeartbeat;
    private final String genreVector;
    private final String moodVector;
    private final boolean hasCoupon;
    private final List<String> tagList;
}