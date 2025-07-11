package com.ceos.beatbuddy.domain.venue.repository;


import com.ceos.beatbuddy.domain.member.constant.Region;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.domain.venue.entity.VenueMood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VenueMoodRepository extends JpaRepository<VenueMood, Long> {

    @Query("SELECT vm FROM VenueMood vm JOIN FETCH vm.venue v WHERE v.region IN :regions")
    List<VenueMood> findByVenueRegion(@Param("regions") List<Region> regions);

    @Query("SELECT vm FROM VenueMood vm JOIN vm.venue v WHERE vm.venue = :venue")
    Optional<VenueMood> findByVenue(@Param("venue") Venue venue);
}
