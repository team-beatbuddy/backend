package com.ceos.beatbuddy.domain.venue.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.VenueReviewLike;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class VenueReview extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id; // 리뷰 ID

    @Column(nullable = false, length = 400)
    private String content; // 리뷰 내용

    private int likes;

    @ElementCollection
    private List<String> imageUrls = new ArrayList<>(); // 리뷰 이미지 URL 목록
    
    @ElementCollection
    private List<String> thumbnailUrls = new ArrayList<>(); // 썸네일 이미지 URL 목록

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venueId")
    private Venue venue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    private Member member; // 리뷰 작성자 ID

    @OneToMany(mappedBy = "venueReview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VenueReviewLike> likesList = new ArrayList<>();

    public void setVenue(Venue venue) {
        this.venue = venue;
    }


    public void setMember(Member member) {
        this.member = member;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
    
    public void setThumbnailUrls(List<String> thumbnailUrls) {
        this.thumbnailUrls = thumbnailUrls;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
