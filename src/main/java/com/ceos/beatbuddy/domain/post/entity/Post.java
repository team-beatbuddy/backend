package com.ceos.beatbuddy.domain.post.entity;

import static lombok.AccessLevel.PROTECTED;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrap.entity.PostScrap;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "Post")  // 추가
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
@NoArgsConstructor(access = PROTECTED)
public abstract class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private boolean anonymous;
    private int likes;
    private int views;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostScrap> scraps = new ArrayList<>();

    private int comments;

    @ElementCollection
    private List<String> imageUrls;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    private Member member;

    protected Post(String title, String content, Boolean anonymous,
                   List<String> imageUrls, Member member) {
        this.title = title;
        this.content = content;
        this.anonymous = anonymous;
        this.imageUrls = imageUrls;
        this.member = member;
        this.likes = 0;
        this.views = 0;
        this.comments = 0;
    }

    public void increaseView() {
        views++;
    }

    public void increaseLike() {
        likes++;
    }

    public void decreaseLike() {
        likes--;
    }

    public void increaseComments() {
        comments++;
    }

    public void decreaseComments() {
        comments--;
    }

}
