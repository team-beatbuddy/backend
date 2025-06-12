package com.ceos.beatbuddy.domain.magazine.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrap.entity.MagazineScrap;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Magazine extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    private Member member;


    private String title;
    private String content;
    private int likes;
    private int views;
    private int scraps;
    private boolean isVisible = true; // 띄워줄 매거진만

    @ElementCollection
    private List<String> imageUrls;

    private String thumbImage;

    public void setThumbImage(String imageUrl) {
        this.thumbImage = imageUrl;
    }
}
