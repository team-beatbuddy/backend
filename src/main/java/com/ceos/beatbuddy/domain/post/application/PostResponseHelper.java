package com.ceos.beatbuddy.domain.post.application;

import com.ceos.beatbuddy.domain.follow.repository.FollowRepository;
import com.ceos.beatbuddy.domain.post.dto.PostInteractionStatus;
import com.ceos.beatbuddy.domain.post.dto.PostListResponseDTO;
import com.ceos.beatbuddy.domain.post.dto.PostPageResponseDTO;
import com.ceos.beatbuddy.domain.post.entity.FixedHashtag;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PostResponseHelper {
    private final PostInteractionService postInteractionService;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;

    public PostListResponseDTO createPostListResponse(Page<? extends Post> postPage, Long memberId) {
        List<? extends Post> posts = postPage.getContent();
        List<Long> postIds = posts.stream().map(Post::getId).toList();

        PostInteractionStatus status = postInteractionService.getAllPostInteractions(memberId, postIds);
        Set<Long> followingIds = followRepository.findFollowingMemberIds(memberId);

        List<PostPageResponseDTO> dtoList = posts.stream()
                .map(post -> createPostPageResponseDTO(post, status, memberId, followingIds))
                .toList();

        return PostListResponseDTO.builder()
                .totalPost((int) postPage.getTotalElements())
                .page(postPage.getNumber() + 1)
                .size(postPage.getSize())
                .responseDTOS(dtoList)
                .build();
    }

    public List<PostPageResponseDTO> createPostPageResponseDTOList(List<? extends Post> posts, Long memberId) {
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        
        PostInteractionStatus status = postInteractionService.getAllPostInteractions(memberId, postIds);
        Set<Long> followingIds = followRepository.findFollowingMemberIds(memberId);

        return posts.stream()
                .map(post -> createPostPageResponseDTO(post, status, memberId, followingIds))
                .toList();
    }

    public PostPageResponseDTO createPostPageResponseDTO(Post post, PostInteractionStatus status, Long memberId, Set<Long> followingIds) {
        List<FixedHashtag> hashtags = getHashtagsForPost(post);

        return PostPageResponseDTO.toDTO(
                post,
                status.likedPostIds().contains(post.getId()),
                status.scrappedPostIds().contains(post.getId()),
                status.commentedPostIds().contains(post.getId()),
                hashtags,
                post.getMember().getId().equals(memberId),
                followingIds.contains(post.getMember().getId())
        );
    }

    public PostListResponseDTO createEmptyPostListResponse(int page, int size) {
        return PostListResponseDTO.builder()
                .totalPost(0)
                .page(page)
                .size(size)
                .responseDTOS(Collections.emptyList())
                .build();
    }

    private List<FixedHashtag> getHashtagsForPost(Post post) {
        if (post instanceof FreePost freePost) {
            return freePost.getHashtag() != null ? freePost.getHashtag() : Collections.emptyList();
        }
        return postRepository.findHashtagsByPostId(post.getId());
    }
}