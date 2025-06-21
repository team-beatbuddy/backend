package com.ceos.beatbuddy.domain.post.dto;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public interface PostCreateRequestDTO {
    @NotNull(message = "제목은 비어있을 수 없습니다.")
    String title();
    @NotNull(message = "내용은 비어있을 수 없습니다.")
    String content();

    Boolean anonymous();
    Long venueId();
}
