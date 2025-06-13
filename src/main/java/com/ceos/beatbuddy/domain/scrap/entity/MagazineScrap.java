package com.ceos.beatbuddy.domain.scrap.entity;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class MagazineScrap extends AbstractScrap{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "magazineId")
    @Getter
    private Magazine magazine;

    public static MagazineScrap toEntity(Member member, Magazine magazine) {
        return MagazineScrap.builder()
                .magazine(magazine)
                .member(member)
                .build();
    }
}
