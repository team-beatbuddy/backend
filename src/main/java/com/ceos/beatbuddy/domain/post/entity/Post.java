package com.ceos.beatbuddy.domain.post.entity;

import com.ceos.beatbuddy.domain.comment.entity.Comment;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.PostLike;
import com.ceos.beatbuddy.domain.scrapandlike.entity.PostScrap;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import com.ceos.beatbuddy.global.util.StringListConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "Post")  // 추가
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
@SuperBuilder
@NoArgsConstructor(access = PROTECTED)
public abstract class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String title;

    @Column(nullable = false,length = 1000)
    private String content;
    private boolean anonymous;
    private int likes;
    private int views;
    private int scraps;

    private int comments;

    @Setter
    @Convert(converter = StringListConverter.class)
    @Column(length = 3000)
    private List<String> imageUrls;

    @Setter
    @Convert(converter = StringListConverter.class)
    @Column(length = 4000)
    private List<String> thumbnailUrls;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    @Setter(PROTECTED)
    private Member member;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostScrap> postScraps = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();


    protected Post(String title, String content, Boolean anonymous,
                   List<String> imageUrls, Member member) {
        this.title = title;
        this.content = content;
        this.anonymous = anonymous;
        this.imageUrls = imageUrls;
        this.member = member;
        this.likes = 0;
        this.views = 0;
        this.scraps = 0;
        this.comments = 0;
    }

    public void increaseComments() {
        comments++;
    }

    public void decreaseComments() {
        comments--;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void setAnonymous(Boolean anonymous) {
        if (anonymous != null) {
            this.anonymous = anonymous;
        }
    }
}
