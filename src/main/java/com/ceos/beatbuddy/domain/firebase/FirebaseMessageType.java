package com.ceos.beatbuddy.domain.firebase;

public enum FirebaseMessageType {
    EVENT_COMMENT("EVENT_COMMENT"),
    POST_COMMENT("POST_COMMENT"),
    FOLLOW("FOLLOW"),
    NEW_EVENT_PROMOTION("NEW_EVENT_PROMOTION"),
    NEW_MAGAZINE_PROMOTION("NEW_MAGAZINE_PROMOTION"),
    NEW_POST_PROMOTION("NEW_POST_PROMOTION");

    private final String type;

    FirebaseMessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
