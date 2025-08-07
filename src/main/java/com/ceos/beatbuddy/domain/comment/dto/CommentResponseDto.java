package com.ceos.beatbuddy.domain.comment.dto;

import com.ceos.beatbuddy.domain.comment.entity.Comment;
import java.time.LocalDateTime;

public record CommentResponseDto(
        Long id,
        String content,
        Boolean isAnonymous,
        Long replyId,
        String memberName,
        Integer likes,
        LocalDateTime createdAt,
        Boolean isAuthor,
        Long writerId,
        Boolean isFollowing,
        Boolean isBlocked,
        Boolean isDeleted

        ) {
    public static CommentResponseDto from(Comment comment, Boolean isAuthor, Boolean isFollowing, Boolean isBlockedMember, Boolean isDeleted) {
        return new CommentResponseDto(
                comment.getId(),
                isBlockedMember ? "차단한 멤버의 댓글입니다." : comment.getContent(),
                comment.isAnonymous(),
                comment.getReply() != null ? comment.getReply().getId() : null,
                comment.isAnonymous() ? "익명" : 
                    (comment.getMember().getPostProfileInfo() != null && comment.getMember().getPostProfileInfo().getPostProfileNickname() != null
                        ? comment.getMember().getPostProfileInfo().getPostProfileNickname()
                        : comment.getMember().getNickname()),
                comment.getLikes(),
                comment.getCreatedAt(),
                isAuthor,
                comment.getMember().getId(),
                isFollowing,
                isBlockedMember,
                isDeleted

        );
    }
}
