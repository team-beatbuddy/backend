package com.ceos.beatbuddy.domain.member.repository;

import com.ceos.beatbuddy.domain.member.dto.MemberProfileSummaryDTO;
import org.springframework.stereotype.Repository;

public interface MemberQueryRepository {
    MemberProfileSummaryDTO getMemberSummary(Long memberId);
}
