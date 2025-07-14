package com.ceos.beatbuddy.domain.firebase.entity;

public enum FirebaseMessageType {
    EVENT_COMMENT("EVENT_COMMENT"),
    POST_COMMENT("POST_COMMENT"),
    FOLLOW("FOLLOW"),
    EVENT("EVENT"),
    MAGAZINE("MAGAZINE"),
    VENUE("VENUE");

    private final String type;

    FirebaseMessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
