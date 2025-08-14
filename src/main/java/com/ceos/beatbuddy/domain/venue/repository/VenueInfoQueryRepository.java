package com.ceos.beatbuddy.domain.venue.repository;

import com.ceos.beatbuddy.domain.venue.dto.VenueInfoOptimizedData;

public interface VenueInfoQueryRepository {
    VenueInfoOptimizedData findVenueInfoOptimized(Long venueId, Long memberId);
}