package com.ceos.beatbuddy.domain.venue.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

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

    public static VenueDocument from(Venue venue) {
        return VenueDocument.builder()
                .id(venue.getId())
                .englishName(venue.getEnglishName())
                .koreanName(venue.getKoreanName())
                .address(venue.getAddress())
                .build();
    }
}