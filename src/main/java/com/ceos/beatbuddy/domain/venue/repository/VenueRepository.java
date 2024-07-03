package com.ceos.beatbuddy.domain.venue.repository;


import com.ceos.beatbuddy.domain.venue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;


public interface VenueRepository extends JpaRepository<Venue, Long> {
    Long findByKoreanName(String koreanName);

}

