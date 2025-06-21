package com.ceos.beatbuddy.domain.venue.application;

import com.ceos.beatbuddy.domain.vector.entity.Vector;
import com.ceos.beatbuddy.domain.venue.dto.VenueVectorResponseDTO;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.domain.venue.entity.VenueGenre;
import com.ceos.beatbuddy.domain.venue.exception.VenueErrorCode;
import com.ceos.beatbuddy.domain.venue.repository.VenueGenreRepository;
import com.ceos.beatbuddy.domain.venue.repository.VenueRepository;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VenueGenreService {
    private final VenueInfoService venueInfoService;
    private final VenueGenreRepository venueGenreRepository;

    /**
     * Adds a new genre vector for the specified venue and returns the resulting vector information.
     *
     * @param venueId the ID of the venue to associate with the genre vector
     * @param genres a map representing genre preferences, where keys are genre names and values are their corresponding weights
     * @return a response DTO containing the created genre vector and venue details
     */
    @Transactional
    public VenueVectorResponseDTO addGenreVector(Long venueId, Map<String, Double> genres) {
        Venue venue = venueInfoService.validateAndGetVenue(venueId);

        Vector preferenceVector = Vector.fromGenres(genres);

        VenueGenre venueGenre = VenueGenre.builder()
                .venue(venue).genreVectorString(preferenceVector.toString())
                .build();

        venueGenreRepository.save(venueGenre);
        return VenueVectorResponseDTO.builder()
                .vectorString(venueGenre.getGenreVectorString())
                .venueId(venue.getId())
                .vectorId(venueGenre.getId())
                .englishName(venue.getEnglishName())
                .koreanName(venue.getKoreanName())
                .region(venue.getRegion())
                .build();
    }

    /**
     * Updates the genre vector associated with a venue.
     *
     * Validates the existence of the venue by its ID, retrieves the current genre vector, and updates it with the provided genre map. Returns a response DTO containing the updated vector and venue details.
     *
     * @param venueId the ID of the venue whose genre vector is to be updated
     * @param genres a map representing the new genre preferences for the venue
     * @return a DTO containing the updated genre vector and venue information
     * @throws CustomException if the venue or its genre vector does not exist
     */
    @Transactional
    public VenueVectorResponseDTO updateGenreVector(Long venueId, Map<String, Double> genres) {
        Venue venue = venueInfoService.validateAndGetVenue(venueId);
        VenueGenre venueGenre = venueGenreRepository.findByVenue(venue).orElseThrow(()->new CustomException(VenueErrorCode.INVALID_VENUE_INFO));

        venueGenre.updateGenreVector(Vector.fromGenres(genres));

        venueGenreRepository.save(venueGenre);
        return VenueVectorResponseDTO.builder()
                .vectorString(venueGenre.getGenreVectorString())
                .venueId(venue.getId())
                .vectorId(venueGenre.getId())
                .englishName(venue.getEnglishName())
                .koreanName(venue.getKoreanName())
                .region(venue.getRegion())
                .build();
    }
}
