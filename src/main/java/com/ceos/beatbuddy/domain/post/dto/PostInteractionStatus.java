package com.ceos.beatbuddy.domain.post.dto;

import java.util.Set;

public record PostInteractionStatus(
        Set<Long> likedPostIds,
        Set<Long> scrappedPostIds,
        Set<Long> commentedPostIds
) { }
