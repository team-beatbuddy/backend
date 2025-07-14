package com.ceos.beatbuddy.domain.firebase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NotificationPageDTO {
    private int page;
    private int size;
    private int totalElements;
    private int totalPages;
    private List<NotificationListDTO> content;
}
