package com.ceos.beatbuddy.domain.member.repository;

import com.ceos.beatbuddy.domain.member.dto.MemberProfileSummaryDTO;

public interface MemberQueryRepository {
    MemberProfileSummaryDTO getMemberSummary(Long memberId);
}
