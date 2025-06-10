package com.ceos.beatbuddy.domain.home.dto;

import com.ceos.beatbuddy.domain.member.constant.Region;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KeywordResponseDTO {
    private List<String> genres;
    private List<String> moods;
    private List<Region> regions;
}