package com.ceos.beatbuddy.domain.comment.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.entity.Post;

public record PostCommentCreatedEvent(Post post, Comment comment, Member writer) {
}