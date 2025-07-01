package com.ceos.beatbuddy.domain.scrapandlike.entity;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MagazineLike extends BaseTimeEntity {

    @EmbeddedId
    private MagazineInteractionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "memberId")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("magazineId")
    @JoinColumn(name = "magazineId")
    private Magazine magazine;

    public static MagazineLike toEntity(Member member, Magazine magazine) {
        return MagazineLike.builder()
                .id(new MagazineInteractionId(member.getId(), magazine.getId()))
                .member(member)
                .magazine(magazine)
                .build();
    }
}
