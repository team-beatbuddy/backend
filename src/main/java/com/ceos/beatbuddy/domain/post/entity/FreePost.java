package com.ceos.beatbuddy.domain.post.entity;

import static lombok.AccessLevel.PROTECTED;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import jakarta.persistence.*;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;

@NoArgsConstructor(access = PROTECTED)
@Entity
@SuperBuilder
@AllArgsConstructor
public class FreePost extends Post{
    @ManyToOne(fetch = FetchType.LAZY)
    @Nullable
    @JoinColumn(name = "venueId")
    private Venue venue;

    @ElementCollection
    @Getter
    @Enumerated(EnumType.STRING)
    private List<FixedHashtag> hashtag;
}
