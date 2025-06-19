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

    @Builder
    public FreePost(List<String> hashtag, List<String> imageUrls, String title, String content,
                    Boolean anonymous, Member member, Venue venue) {
        super(title, content, anonymous, imageUrls, member);
        this.venue = venue;
        this.hashtag = hashtag;
    }
}
