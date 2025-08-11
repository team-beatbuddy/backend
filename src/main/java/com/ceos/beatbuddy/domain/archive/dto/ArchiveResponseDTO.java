package com.ceos.beatbuddy.domain.archive.dto;


import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class ArchiveResponseDTO {

    private Long archiveId;
    private Long memberId;
    private List<String> preferenceList;
    private LocalDateTime updatedAt;
}
