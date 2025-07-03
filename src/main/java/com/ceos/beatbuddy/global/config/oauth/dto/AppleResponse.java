package com.ceos.beatbuddy.global.config.oauth.dto;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class AppleResponse implements OAuth2Response {
    private final Map<String, Object> attributes;

    @Override
    public String getProvider() {
        return "apple";
    }

    @Override
    public String getProviderId() {
        return attributes.get("sub").toString(); // Apple의 고유 ID
    }

    @Override
    public String getName() {
        return (String) attributes.getOrDefault("email", "apple_user");
    }
}