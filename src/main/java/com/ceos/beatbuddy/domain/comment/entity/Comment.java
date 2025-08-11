package com.ceos.beatbuddy.domain.comment.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.domain.scrapandlike.entity.CommentLike;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class Comment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private boolean isAnonymous;

    @Column(nullable = true)
    private String anonymousNickname; // 익명 댓글일 때 부여되는 닉네임 (예: "익명 1", "익명 2")

    @ManyToOne
    @Nullable
    @JoinColumn(name = "replyId")
    private Comment reply;

    @ManyToOne
    @JoinColumn(name = "memberId")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "postId")
    private Post post;

    private boolean isDeleted;

    private int likes;

    // 대댓글 컬렉션 OK
    @OneToMany(mappedBy = "reply", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Comment> children = new ArrayList<>();

    // ✅ 댓글 좋아요는 CommentLike의 'comment' 필드가 주인
    @OneToMany(mappedBy = "comment", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<CommentLike> commentLikes = new ArrayList<>();
    public Long getPostId() {
        return this.post.getId();
    }

    public void setDeleted(boolean b) {
        this.isDeleted = b;
    }
}
