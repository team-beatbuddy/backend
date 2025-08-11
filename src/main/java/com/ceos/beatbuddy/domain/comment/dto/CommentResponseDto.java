package com.ceos.beatbuddy.domain.comment.dto;

import com.ceos.beatbuddy.domain.comment.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponseDto(
        Long id,
        String content,
        Boolean isAnonymous,
        Long replyId,
        String memberName,
        String imageUrl,
        Integer likes,
        LocalDateTime createdAt,
        Boolean isAuthor,
        Long writerId,
        Boolean isFollowing,
        Boolean isBlocked,
        Boolean isDeleted,
        Boolean isPostWriter

        ) {
    public static CommentResponseDto from(Comment comment, Boolean isAuthor, Boolean isFollowing, Boolean isBlockedMember, Boolean isDeleted, Boolean isPostWriter, Boolean isPostAnonymous) {
        // 닉네임 결정 로직
        String displayName;
        if (comment.isAnonymous()) {
            // 익명 댓글
            displayName = comment.getAnonymousNickname() != null ? comment.getAnonymousNickname() : "익명";
        } else if (isPostWriter) {
            // 글 작성자의 실명 댓글 → 게시판용 닉네임 사용
            displayName = comment.getMember().getPostProfileInfo() != null && comment.getMember().getPostProfileInfo().getPostProfileNickname() != null
                    ? comment.getMember().getPostProfileInfo().getPostProfileNickname()
                    : comment.getMember().getNickname();
        } else {
            // 일반 사용자의 실명 댓글
            displayName = comment.getMember().getPostProfileInfo() != null && comment.getMember().getPostProfileInfo().getPostProfileNickname() != null
                    ? comment.getMember().getPostProfileInfo().getPostProfileNickname()
                    : comment.getMember().getNickname();
        }
        
        // isPostWriter 결정 로직
        Boolean finalIsPostWriter = isPostWriter;
        if (isPostWriter) {
            // 글 작성자인 경우
            if (isPostAnonymous && !comment.isAnonymous()) {
                // 익명 게시물 + 실명 댓글 → 작성자임을 숨김
                finalIsPostWriter = false;
            } else if (!isPostAnonymous && comment.isAnonymous()) {
                // 실명 게시물 + 익명 댓글 → 작성자임을 숨김
                finalIsPostWriter = false;
            }
            // 익명 게시물 + 익명 댓글, 실명 게시물 + 실명 댓글은 그대로 유지
        }

        return new CommentResponseDto(
                comment.getId(),
                isBlockedMember ? "차단한 멤버의 댓글입니다." : comment.getContent(),
                comment.isAnonymous(),
                comment.getReply() != null ? comment.getReply().getId() : null,
                displayName,
                comment.isAnonymous() ? "" :
                    (comment.getMember().getPostProfileInfo() != null && comment.getMember().getPostProfileInfo().getPostProfileImageUrl() != null
                        ? comment.getMember().getPostProfileInfo().getPostProfileImageUrl()
                        : (comment.getMember().getProfileImage() != null
                                ? comment.getMember().getProfileImage()
                                : "")),
                comment.getLikes(),
                comment.getCreatedAt(),
                isAuthor,
                comment.getMember().getId(),
                isFollowing,
                isBlockedMember,
                isDeleted,
                finalIsPostWriter

        );
    }
}
