package com.ceos.beatbuddy.domain.post.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@NoArgsConstructor(access = PROTECTED)
@Entity
@SuperBuilder
@AllArgsConstructor
public class FreePost extends Post{
    @Getter
    @ElementCollection(targetClass = FixedHashtag.class)
    @Enumerated(EnumType.STRING)
    private List<FixedHashtag> hashtag;

    public void updateHashtags(List<FixedHashtag> hashtags) {
        this.hashtag = hashtags;
    }
}
