package com.ceos.beatbuddy.global.config.oauth.dto;

import java.util.Map;

public class GoogleResponse implements OAuth2Response {
    private final Map<String, Object> attributes;

    public GoogleResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub"); // 구글 고유 ID
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}