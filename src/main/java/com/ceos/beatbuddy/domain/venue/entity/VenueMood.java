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
@Table(name = "VenueMood",
        uniqueConstraints = @UniqueConstraint(columnNames = "venueId"))
public class VenueMood extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venueId", unique = true, nullable = false)
    private Venue venue;

    @Transient
    private Vector moodVector;

    @Lob
    private String moodVectorString;

    public void updateMoodVector(Vector vector) {
        this.moodVector = vector;
        this.moodVectorString = vector.getElements().toString();
    }

    public Vector getMoodVector() {
        if (moodVector == null && moodVectorString != null) {
            List<Double> elements = Stream.of(moodVectorString.replace("[", "").replace("]", "").split(",")).map(String::trim).map(Double::parseDouble).collect(Collectors.toList());
            moodVector = new Vector(elements);
        }
        return moodVector;
    }

}
