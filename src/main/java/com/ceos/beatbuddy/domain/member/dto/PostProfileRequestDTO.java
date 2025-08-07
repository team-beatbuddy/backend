package com.ceos.beatbuddy.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "게시글 작성 시 프로필 정보 요청 DTO")
public class PostProfileRequestDTO {
    @Schema(description = "게시글 작성자의 닉네임", example = "홍길동")
    private String postProfileNickname;
}
