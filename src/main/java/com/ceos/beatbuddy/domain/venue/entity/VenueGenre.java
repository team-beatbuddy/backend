package com.ceos.beatbuddy.domain.venue.entity;

import com.ceos.beatbuddy.domain.vector.entity.Vector;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VenueGenre extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "venueId")
    private Venue venue;

    @Transient
    private Vector genreVector;

    @Lob
    private String genreVectorString;

    public void updateGenreVector(Vector vector) {
        this.genreVector = vector;
        this.genreVectorString = vector.getElements().toString();
    }

    public Vector getGenreVector() {
        if (genreVector == null && genreVectorString != null) {
            List<Double> elements = Stream.of(genreVectorString.replace("[", "").replace("]", "").split(",")).map(String::trim).map(Double::parseDouble).collect(Collectors.toList());
            genreVector = new Vector(elements);
        }
        return genreVector;
    }

}
