package com.ceos.beatbuddy.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberBlockRequestDTO {
    private Long blockedMemberId;
}