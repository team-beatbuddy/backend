package com.ceos.beatbuddy.domain.venue.application;

import com.ceos.beatbuddy.domain.vector.entity.Vector;
import com.ceos.beatbuddy.domain.venue.dto.VenueVectorResponseDTO;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.domain.venue.entity.VenueMood;
import com.ceos.beatbuddy.domain.venue.exception.VenueErrorCode;
import com.ceos.beatbuddy.domain.venue.repository.VenueMoodRepository;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VenueMoodService {
    private final VenueInfoService venueInfoService;
    private final VenueMoodRepository venueMoodRepository;
    private final VenueSearchService venueSearchService;

    /**
     * Adds a new mood vector for the specified venue and returns the resulting vector details.
     *
     * Validates the existence of the venue, converts the provided mood map into a vector, associates it with the venue, and saves the new mood vector entity.
     *
     * @param venueId the ID of the venue to associate with the mood vector
     * @param moods a map representing mood names and their corresponding values
     * @return a response DTO containing the saved vector string, venue ID, vector ID, and venue names and region
     */
    @Transactional
    public VenueVectorResponseDTO addMoodVector(Long venueId, Map<String, Double> moods) {
        Venue venue = venueInfoService.validateAndGetVenue(venueId);

        Vector preferenceVector = Vector.fromMoods(moods);

        VenueMood venueMood = VenueMood.builder()
                .venue(venue).moodVectorString(preferenceVector.toString())
                .build();

        venueMoodRepository.save(venueMood);

        venueSearchService.save(
                venue,
                null,
                venueMood
        );
        return VenueVectorResponseDTO.builder()
                .vectorString(venueMood.getMoodVectorString())
                .venueId(venue.getId())
                .vectorId(venueMood.getId())
                .englishName(venue.getEnglishName())
                .koreanName(venue.getKoreanName())
                .region(venue.getRegion())
                .build();
    }

    /**
     * Updates the mood vector associated with a venue.
     *
     * Validates the existence of the venue by its ID, retrieves the current mood vector, updates it with the provided moods, and returns a response containing the updated vector and venue details.
     *
     * @param venueId the ID of the venue whose mood vector is to be updated
     * @param moods a map representing the new mood values
     * @return a response DTO containing the updated mood vector and venue information
     * @throws CustomException if the venue does not have an existing mood vector
     */
    @Transactional
    public VenueVectorResponseDTO updateMoodVector(Long venueId, Map<String, Double> moods) {
        Venue venue = venueInfoService.validateAndGetVenue(venueId);
        VenueMood venueMood = venueMoodRepository.findByVenue(venue).orElseThrow(()->new CustomException(VenueErrorCode.INVALID_VENUE_INFO));

        venueMood.updateMoodVector(Vector.fromMoods(moods));

        venueMoodRepository.save(venueMood);

        venueSearchService.save(
                venue,
                null,
                venueMood
        );
        return VenueVectorResponseDTO.builder()
                .vectorString(venueMood.getMoodVectorString())
                .venueId(venue.getId())
                .vectorId(venueMood.getId())
                .englishName(venue.getEnglishName())
                .koreanName(venue.getKoreanName())
                .region(venue.getRegion())
                .build();
    }
}
