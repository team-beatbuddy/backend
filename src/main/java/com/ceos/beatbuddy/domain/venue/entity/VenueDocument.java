package com.ceos.beatbuddy.domain.venue.entity;

import com.ceos.beatbuddy.domain.vector.entity.Vector;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "venue")
public class VenueDocument implements java.io.Serializable {
    @Id
    private Long id;

    @Field(type = FieldType.Text)
    private String englishName;

    @Field(type = FieldType.Text)
    private String koreanName;

    @Field(type = FieldType.Text)
    private String address;

    private List<String> genre;
    private List<String> mood;
    private boolean smoking;
    private String region;

    public static VenueDocument from(Venue venue, VenueGenre venueGenre, VenueMood venueMood) {
        List<String> genres = venueGenre != null && venueGenre.getGenreVector() != null
                ? Vector.getTrueGenreElements(venueGenre.getGenreVector())
                : Collections.emptyList();

        List<String> moods = venueMood != null && venueMood.getMoodVector() != null
                ? Vector.getTrueMoodElements(venueMood.getMoodVector())
                : Collections.emptyList();

        return VenueDocument.builder()
                .id(venue.getId())
                .englishName(venue.getEnglishName())
                .koreanName(venue.getKoreanName())
                .address(venue.getAddress())
                .genre(genres) // ✅ 리스트로 변환된 장르
                .mood(moods)   // ✅ 리스트로 변환된 무드
                .smoking(venue.isSmokingAllowed())
                .region(venue.getRegion() != null ? venue.getRegion().getKorText() : null)
                .build();
    }
}