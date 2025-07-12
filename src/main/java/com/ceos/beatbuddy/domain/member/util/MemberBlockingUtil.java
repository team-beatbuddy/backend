package com.ceos.beatbuddy.domain.member.util;

import com.ceos.beatbuddy.domain.member.application.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 멤버 차단 기능을 지원하는 유틸리티 클래스
 * 다른 서비스들에서 차단된 멤버 목록을 쉽게 조회할 수 있도록 도움
 */
@Component
@RequiredArgsConstructor
public class MemberBlockingUtil {
    
    private final MemberService memberService;
    
    /**
     * 특정 사용자가 차단한 멤버들의 ID 목록을 조회
     * @param memberId 현재 사용자 ID
     * @return 차단된 멤버 ID 목록 (빈 리스트일 수 있음)
     */
    public List<Long> getBlockedMemberIds(Long memberId) {
        if (memberId == null) {
            return Collections.emptyList();
        }
        
        try {
            return memberService.getBlockedMemberIds(memberId);
        } catch (Exception e) {
            // 에러 발생 시 빈 리스트 반환하여 서비스 중단 방지
            return Collections.emptyList();
        }
    }
    
    /**
     * 차단 목록이 비어있는지 확인
     * @param blockedMemberIds 차단된 멤버 ID 목록
     * @return 비어있으면 true
     */
    public boolean isBlockListEmpty(List<Long> blockedMemberIds) {
        return blockedMemberIds == null || blockedMemberIds.isEmpty();
    }
}