package com.ceos.beatbuddy.domain.post.dto;

import com.ceos.beatbuddy.domain.post.entity.Post;
import lombok.*;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ResponsePostDto {
    private Long id;
    private String title;
    private String role;
    private int likes;
    private int comments;
    private LocalDate createAt;
    private String nickname;

    public static ResponsePostDto of(Post post){
        ResponsePostDto.ResponsePostDtoBuilder builder = ResponsePostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .role(post.getMember().getRole().toString())
                .createAt(post.getCreatedAt().toLocalDate())
                .likes(post.getLikes())
                .comments(post.getComments());

        if (post.isAnonymous()) {
            builder.nickname(post.getMember().getNickname());
        }

        return builder.build();
    }
}
