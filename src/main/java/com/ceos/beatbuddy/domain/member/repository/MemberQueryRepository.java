package com.ceos.beatbuddy.domain.member.repository;

import com.ceos.beatbuddy.domain.member.dto.response.MemberProfileSummaryDTO;

public interface MemberQueryRepository {
    MemberProfileSummaryDTO getMemberSummary(Long memberId);
}
