package com.ceos.beatbuddy.domain.magazine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MagazinePageResponseDTO {
    private int page;
    private int size;
    private int totalCount;
    private List<MagazineDetailDTO> magazines;
}
