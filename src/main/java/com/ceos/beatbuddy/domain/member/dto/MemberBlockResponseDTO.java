package com.ceos.beatbuddy.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MemberBlockResponseDTO {
    private String message;
    private List<Long> blockedMemberIds;
}