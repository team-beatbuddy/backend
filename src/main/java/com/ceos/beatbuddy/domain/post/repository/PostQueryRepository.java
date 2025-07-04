package com.ceos.beatbuddy.domain.post.repository;

import com.ceos.beatbuddy.domain.post.entity.FixedHashtag;
import com.ceos.beatbuddy.domain.post.entity.Post;

import java.util.List;

public interface PostQueryRepository {
    List<Post> findHotPostsWithin12Hours();
}
