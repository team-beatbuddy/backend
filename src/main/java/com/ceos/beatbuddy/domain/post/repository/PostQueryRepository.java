package com.ceos.beatbuddy.domain.post.repository;

import com.ceos.beatbuddy.domain.post.entity.FixedHashtag;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface PostQueryRepository {
    List<Post> findHotPostsWithin12Hours();

    Page<FreePost> findPostsByHashtags(List<FixedHashtag> hashtags, Pageable pageable);

    Page<FreePost> readAllPostsByUserExcludingAnonymous(Long userId, Pageable pageable);
    
    /**
     * 핫 포스트 조회 (차단된 멤버 제외)
     * @param blockedMemberIds 차단된 멤버 ID 목록
     * @return 핫 포스트 목록 (차단된 멤버 제외)
     */
    List<Post> findHotPostsWithin12HoursExcludingBlocked(List<Long> blockedMemberIds);
    
    /**
     * 해시태그별 포스트 조회 (차단된 멤버 제외)
     * @param hashtags 해시태그 목록
     * @param pageable 페이징 정보
     * @param blockedMemberIds 차단된 멤버 ID 목록
     * @return 포스트 페이지 (차단된 멤버 제외)
     */
    Page<FreePost> findPostsByHashtagsExcludingBlocked(List<FixedHashtag> hashtags, Pageable pageable, List<Long> blockedMemberIds);
}
