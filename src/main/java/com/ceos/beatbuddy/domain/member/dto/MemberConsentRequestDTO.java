package com.ceos.beatbuddy.domain.member.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class MemberConsentRequestDTO {
    @NonNull
    private Boolean isLocationConsent;
    @NonNull
    private Boolean isMarketingConsent;
}
