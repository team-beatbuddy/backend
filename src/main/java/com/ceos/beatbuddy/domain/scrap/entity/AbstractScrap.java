package com.ceos.beatbuddy.domain.scrap.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@Getter
public abstract class AbstractScrap extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    protected Member member;
}