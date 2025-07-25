package com.ceos.beatbuddy.domain.venue.repository;


import com.ceos.beatbuddy.domain.member.constant.Region;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface VenueRepository extends JpaRepository<Venue, Long> {
    @Query("SELECT v.id FROM Venue v WHERE v.koreanName = :koreanName")
    Long findVenueIdByKoreanName(@Param("koreanName") String koreanName);

    @Query("SELECT v.id FROM Venue v")
    List<Long> findAllIds();

    @Query("SELECT v FROM Venue v ORDER BY v.heartbeatNum DESC LIMIT 10")
    List<Venue> sortByHeartbeatCount();

    @Query("SELECT v FROM Venue v WHERE v.id = :venueId")
    Long deleteByVenueId(@Param("venueId")Long venueId);

    @Query("SELECT v FROM Venue v WHERE v.region IN :regions")
    List<Venue> findByVenueRegion(@Param("regions") List<Region> regions);

    @Modifying
    @Query("UPDATE Venue v SET v.heartbeatNum = v.heartbeatNum + 1 WHERE v.id = :venueId")
    void incrementHeartbeatCount(@Param("venueId") Long venueId);

    @Modifying
    @Query("UPDATE Venue v SET v.heartbeatNum = v.heartbeatNum - 1 WHERE v.id = :venueId")
    void decrementHeartbeatCount(@Param("venueId") Long venueId);

}

