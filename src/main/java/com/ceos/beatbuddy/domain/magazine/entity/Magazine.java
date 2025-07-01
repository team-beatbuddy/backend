package com.ceos.beatbuddy.domain.magazine.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.MagazineLike;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
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

    @OneToMany(mappedBy = "magazine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MagazineLike> likeList = new ArrayList<>();

    private boolean isVisible = true; // 띄워줄 매거진만
    private boolean isPinned = false; // 고정된 매거진인지 여부

    @ElementCollection
    private List<String> imageUrls;

    private String thumbImage;

    // 이후 이벤트 글 연동..........

    public void setThumbImage(String imageUrl) {
        this.thumbImage = imageUrl;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }


    public void increaseView() {
        views++;
    }

}
