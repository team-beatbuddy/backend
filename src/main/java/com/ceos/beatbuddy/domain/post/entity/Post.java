package com.ceos.beatbuddy.domain.post.entity;

import static lombok.AccessLevel.PROTECTED;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.PostScrap;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Setter
    private List<String> imageUrls;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    @Setter
    private Member member;

    /**
     * Post 엔티티의 주요 필드를 초기화하는 protected 생성자입니다.
     *
     * @param title     게시글의 제목
     * @param content   게시글의 내용
     * @param anonymous 익명 여부
     * @param imageUrls 게시글에 첨부된 이미지 URL 목록
     * @param member    게시글 작성자
     */
    protected Post(String title, String content, Boolean anonymous,
                   List<String> imageUrls, Member member) {
        this.title = title;
        this.content = content;
        this.anonymous = anonymous;
        this.imageUrls = imageUrls;
        this.member = member;
        this.likes = 0;
        this.views = 0;
        this.scraps = null;
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
