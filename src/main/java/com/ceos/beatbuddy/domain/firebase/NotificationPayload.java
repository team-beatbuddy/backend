package com.ceos.beatbuddy.domain.firebase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationPayload {
    private String title;
    private String body;
    private String imageUrl;
    private Map<String, String> data;

    public NotificationPayload copy() {
        Map<String, String> copiedData = new HashMap<>(this.data);

        return NotificationPayload.builder()
                .title(this.title)
                .body(this.body)
                .imageUrl(this.imageUrl)
                .data(copiedData)
                .build();
    }

}
