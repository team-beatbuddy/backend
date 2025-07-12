package com.ceos.beatbuddy.domain.recent_search.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(indexes = {
        @Index(name = "idx_member_searchtype_updatedat",
                columnList = "memberId, searchType, updatedAt")
})
public class RecentSearch extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "searchType", nullable = false)
    private SearchTypeEnum searchType;

    @ManyToOne
    @JoinColumn(name = "memberId", nullable = false)
    private Member member; // 검색어를 저장한 회원

    @Column(name = "keyword", nullable = false, length = 20)
    private String keyword; // 검색어
}
