package com.ceos.beatbuddy.domain.magazine.dto;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Builder
@Getter
public class MagazineDetailDTO {
    private Long magazineId;
    private String title;
    private String content;
    private Long writerId;
    private List<String> imageUrls;
    private String thumbImage; // 썸네일 이미지 URL
    private int views;
    private int likes;
    private boolean visible; // 매거진이 보이는지 여부
    private boolean pinned; // 매거진이 고정되어 있는지 여부
    private int orderInHome; // 홈에서의 순서
    private LocalDateTime createdAt;
    private boolean liked; // 좋아요 여부
    private boolean sponsored; // 스폰서 매거진인지 여부
    private boolean picked; // 픽된 매거진인지 여부
    private EventSimpleDTO eventSimpleDTO; // 이벤트 ID (optional, if applicable)
    private List<VenueSimpleDTO> venueSimpleDTOS; // 장소 ID 리스트 (optional, if applicable)

    @JsonProperty("isAuthor")
    private Boolean isAuthor;

    public Boolean getIsAuthor() {
        return isAuthor;
    }

    public static MagazineDetailDTO toDTO(Magazine magazine, boolean isLiked, boolean isAuthor) {
        return MagazineDetailDTO.builder()
                .magazineId(magazine.getId())
                .title(magazine.getTitle())
                .content(magazine.getContent())
                .thumbImage(magazine.getThumbImage() != null ? magazine.getThumbImage() : "")
                .imageUrls(magazine.getImageUrls() != null ? magazine.getImageUrls() : Collections.emptyList())
                .writerId(magazine.getMember().getId())
                .likes(magazine.getLikes())
                .views(magazine.getViews())
                .pinned(magazine.isPinned())
                .orderInHome(magazine.getOrderInHome())
                .visible(magazine.isVisible())
                .createdAt(magazine.getCreatedAt())
                .liked(isLiked)
                .picked(magazine.isPicked())
                .sponsored(magazine.isSponsored())
                .eventSimpleDTO(magazine.getEvent() != null ? EventSimpleDTO.toDTO(magazine) : null)
                .venueSimpleDTOS(magazine.getVenues() != null ?
                        magazine.getVenues().stream()
                                .map(VenueSimpleDTO::toDTO)
                                .toList() : Collections.emptyList())
                .isAuthor(isAuthor)
                .build();
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class VenueSimpleDTO {
        private Long venueId;
        private String koreanName;
        private String englishName;

        public static VenueSimpleDTO toDTO(Venue venue) {
            return VenueSimpleDTO.builder()
                    .venueId(venue.getId())
                    .koreanName(venue.getKoreanName())
                    .englishName(venue.getEnglishName())
                    .build();
        }
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class EventSimpleDTO {
        private Long eventId;
        private String title;

        public static EventSimpleDTO toDTO(Magazine magazine) {
            if (magazine.getEvent() == null) {
                return null; // 이벤트가 없는 경우 null 반환
            }
            return new EventSimpleDTO(magazine.getEvent().getId(), magazine.getEvent().getTitle());
        }
    }
}
