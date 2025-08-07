package com.ceos.beatbuddy.domain.post.dto;

import com.ceos.beatbuddy.domain.post.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Schema(description = "게시글 응답 DTO")
public class ResponsePostDto {
    @Schema(description = "게시글 ID", example = "1")
    private Long id;
    @Schema(description = "게시글 제목", example = "오늘의 공연 후기")
    private String title;
    @Schema(description = "게시글 작성자 역할", example = "USER")
    private String role;
    @Schema(description = "게시글 좋아요 수", example = "100")
    private int likes;
    @Schema(description = "게시글 댓글 수", example = "20")
    private int comments;
    @Schema(description = "게시글 작성 날짜", example = "2023-10-01T12:00:00")
    private LocalDate createAt;
    @Schema(description = "게시글 작성자 닉네임", example = "요시")
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
            builder.nickname("익명");
        } else {
            // 게시물용 프로필이 있으면 사용, 없으면 일반 닉네임 사용
            String postProfileNickname = post.getMember().getPostProfileInfo() != null 
                    && post.getMember().getPostProfileInfo().getPostProfileNickname() != null
                    ? post.getMember().getPostProfileInfo().getPostProfileNickname()
                    : post.getMember().getNickname();
            builder.nickname(postProfileNickname);
        }

        return builder.build();
    }
}
