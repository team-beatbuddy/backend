package com.ceos.beatbuddy.domain.post.entity;

import static lombok.AccessLevel.PROTECTED;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import jakarta.persistence.*;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@NoArgsConstructor(access = PROTECTED)
@Entity
public class FreePost extends Post{
    @ManyToOne(fetch = FetchType.LAZY)
    @Nullable
    @JoinColumn(name = "venueId")
    private Venue venue;

    @ElementCollection
    private List<String> hashtag;

    /**
     * 해시태그, 이미지 URL, 제목, 내용, 익명 여부, 작성자, 공연장 정보를 사용하여 FreePost 엔티티를 생성합니다.
     *
     * @param hashtag   게시글에 연결된 해시태그 목록
     * @param imageUrls 게시글에 첨부된 이미지 URL 목록
     * @param title     게시글 제목
     * @param content   게시글 내용
     * @param anonymous 작성자 익명 여부
     * @param member    게시글 작성자
     * @param venue     게시글과 연관된 공연장(선택적)
     */
    @Builder
    public FreePost(List<String> hashtag, List<String> imageUrls, String title, String content,
                    Boolean anonymous, Member member, Venue venue) {
        super(title, content, anonymous, imageUrls, member);
        this.venue = venue;
        this.hashtag = hashtag;
    }
}
