package com.ceos.beatbuddy.domain.event.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "event")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDocument {
    @Id
    private Long id;
    @Field(type = FieldType.Text)
    private String title;
    @Field(type = FieldType.Text)
    private String content;
    @Field(type = FieldType.Text)
    private String location;
    @Field(type = FieldType.Text)
    private String entranceNotice;
    @Field(type = FieldType.Text)
    private String notice;
    @Field(type = FieldType.Text)
    private String isFreeEntrance; // "무료입장" 또는 "유료입장"
    @Field(type = FieldType.Text)
    private String venueKoreanName;
    @Field(type = FieldType.Text)
    private String venueEnglishName;
    @Field(type = FieldType.Text)
    private String venueLocation;
    @Field(type = FieldType.Boolean)
    private Boolean isVisible;
    @Field(type = FieldType.Text)
    private String region;
public static EventDocument from(Event event) {
    return EventDocument.builder()
            .id(event.getId())
            .title(event.getTitle())
            .content(event.getContent())
            .location(event.getLocation())
            .entranceNotice(event.getEntranceNotice())
            .notice(event.getNotice())
            .isFreeEntrance(event.isFreeEntrance() ? "무료입장" : "유료입장")
            .venueKoreanName(event.getVenue() != null ? event.getVenue().getKoreanName() : "")
            .venueEnglishName(event.getVenue() != null ? event.getVenue().getEnglishName() : "")
            .venueLocation(event.getVenue() != null ? event.getVenue().getAddress() : "")
            .region(event.getRegion().name())
            .isVisible(event.isVisible())
            .startDate(event.getStartDate())
            .endDate(event.getEndDate())
            .build();
}

    public static EventDocument from(Event event) {
        return EventDocument.builder()
                .id(event.getId())
                .title(event.getTitle())
                .content(event.getContent())
                .location(event.getLocation())
                .entranceNotice(event.getEntranceNotice())
                .notice(event.getNotice())
                .isFreeEntrance(event.isFreeEntrance() ? "무료입장" : "유료입장")
                .venueKoreanName(event.getVenue() != null ? event.getVenue().getKoreanName() : "")
                .venueEnglishName(event.getVenue() != null ? event.getVenue().getEnglishName() : "")
                .venueLocation(event.getVenue() != null ? event.getVenue().getAddress() : "")
                .region(event.getRegion().name())
                .isVisible(event.isVisible())
                .build();
    }
}
