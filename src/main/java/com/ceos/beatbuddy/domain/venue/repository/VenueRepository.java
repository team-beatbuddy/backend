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

    @Modifying
    @Query("UPDATE Venue v SET v.latitude = :lat, v.longitude = :lng WHERE v.id = :venueId")
    void updateLatLng(@Param("venueId") Long venueId, @Param("lat") double lat, @Param("lng") double lng);

    @Query(
            value = """
            SELECT *,
                   ST_Distance_Sphere(point(:lng, :lat), point(v.longitude, v.latitude)) AS distance
            FROM venue v
            WHERE v.latitude IS NOT NULL AND v.longitude IS NOT NULL
            ORDER BY distance
            LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<Venue> findNearbyVenuesWithPagination(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query("SELECT v FROM Venue v WHERE v.id IN :ids")
    List<Venue> findByIdIn(@Param("ids") List<Long> ids);
}

