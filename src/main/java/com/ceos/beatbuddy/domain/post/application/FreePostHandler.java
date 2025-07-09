package com.ceos.beatbuddy.domain.post.application;

import com.ceos.beatbuddy.domain.follow.entity.Follow;
import com.ceos.beatbuddy.domain.follow.repository.FollowRepository;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.dto.*;
import com.ceos.beatbuddy.domain.post.entity.FixedHashtag;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.domain.post.exception.PostErrorCode;
import com.ceos.beatbuddy.domain.post.repository.FreePostRepository;
import com.ceos.beatbuddy.domain.post.repository.PostQueryRepository;
import com.ceos.beatbuddy.domain.post.repository.PostRepository;
import com.ceos.beatbuddy.domain.venue.application.VenueInfoService;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component("free")
@RequiredArgsConstructor
public class FreePostHandler implements PostTypeHandler{
    private final FreePostRepository freePostRepository;
    private final VenueInfoService venueInfoService;
    private final FreePostSearchService freePostSearchService;
    private final PostInteractionService postInteractionService;
    private final PostQueryRepository postQueryRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;

    @Override
    public boolean supports(Post post) {
        return post instanceof FreePost;
    }

    @Override
    @Transactional
    public Post createPost(PostCreateRequestDTO dto, Member member, List<String> imageUrls) {
        Venue venue = Optional.ofNullable(dto.getVenueId())
                .map(venueInfoService::validateAndGetVenue)
                .orElse(null);


        List<FixedHashtag> hashtags = validateAndGetHashtags(dto.getHashtags());

        FreePost freePost = PostCreateRequestDTO.toEntity(dto, imageUrls, member, hashtags);

        freePost = freePostRepository.save(freePost);
        freePostSearchService.save(freePost); // 게시글 생성 시 검색 인덱스에 저장
        return freePost;
    }

    @Override
    public Page<? extends Post> readAllPosts(Pageable pageable) {
        return freePostRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Post readPost(Long postId) {
        Post post = validateAndGetPost(postId);

        postRepository.increaseViews(postId); // 조회수 증가
        return post;
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Member member) {
        Post post = validateAndGetPost(postId);
        validateWriter(post, member);
        freePostRepository.deleteById(post.getId());
        freePostSearchService.delete(postId); // 게시글 삭제 시 검색 인덱스에서 제거
    }

    @Override
    public Post validateAndGetPost(Long postId) {
        return freePostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_EXIST));
    }

    @Override
    public void validateWriter(Post post, Member member) {
        if (!post.getMember().equals(member)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }
    }



    /**
     * 업데이트 요청 DTO로부터 제목과 내용을 가져와, null이 아니고 공백이 아닌 경우에만 게시글의 제목과 내용을 수정합니다.
     *
     * DTO의 각 필드 값이 null이 아니고 앞뒤 공백을 제외했을 때 비어 있지 않은 경우에만 해당 필드를 업데이트합니다.
     */
    protected void updateCommonFields(UpdatePostRequestDTO dto, FreePost post) {
        if ((dto.getTitle() != null) && (!dto.getTitle().trim().isEmpty())) {
            post.updateTitle(dto.getTitle());
        }
        if ((dto.getContent() != null) && (!dto.getContent().trim().isEmpty())) {
            post.updateContent(dto.getContent());
        }
        if (dto.getHashtags() != null && !dto.getHashtags().isEmpty()) {
            List<FixedHashtag> hashtags = validateAndGetHashtags(dto.getHashtags());
            post.updateHashtags(hashtags);
        }
        if (dto.getAnonymous() != null) {
            post.setAnonymous(dto.getAnonymous());
        }
    }

    @Override
    @Transactional
    public Post updatePost(UpdatePostRequestDTO dto, Post post, Member member) {
        FreePost freepost = (FreePost) post;
        updateCommonFields(dto, freepost);
        return post;
    }

    @Override
    public Page<? extends Post> readAllPostsByUserExcludingAnonymous(Long userId, Pageable pageable) {
        return postQueryRepository.readAllPostsByUserExcludingAnonymous(userId, pageable);
    }

    @Override
    public Page<? extends Post> readAllPostsByMember(Long memberId, Pageable pageable) {
        return freePostRepository.findByMemberId(memberId, pageable);
    }

    // 해시태그로 게시글 목록 불러오기
    @Override
    @Transactional(readOnly = true)
    public PostListResponseDTO hashTagPostList(List<String> hashtags, Pageable pageable, Member member) {
        if (hashtags == null || hashtags.isEmpty()) {
            throw new CustomException(PostErrorCode.NOT_FOUND_HASHTAG);
        }

        // 해시태그 유효성 검사 및 변환
        List<FixedHashtag> fixedHashtags = validateAndGetHashtags(hashtags);

        // 정렬은 QueryDSL 내부에서 처리 createdAt DESC
        Page<FreePost> posts = postQueryRepository.findPostsByHashtags(fixedHashtags, pageable);

        if (posts.isEmpty()) {
            return PostListResponseDTO.builder()
                    .totalPost(0)
                    .page(pageable.getPageNumber())
                    .size(pageable.getPageSize())
                    .responseDTOS(Collections.emptyList())
                    .build();
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();


        // 회원의 상호작용 정보 조회
        PostInteractionStatus status = postInteractionService.getAllPostInteractions(member.getId(), postIds);

        // 팔로잉 중인 대상 ID 목록 가져오기
        Set<Long> followingIds = followRepository.findFollowingMemberIds(member.getId());


        List<PostPageResponseDTO> dtos = posts.stream()
                .map(post -> PostPageResponseDTO.toDTO(
                        post,
                        status.likedPostIds().contains(post.getId()),
                        status.scrappedPostIds().contains(post.getId()),
                        status.commentedPostIds().contains(post.getId()),
                        post.getHashtag(),
                        post.getMember().getId().equals(member.getId()),
                        followingIds.contains(post.getMember().getId())
                ))
                .toList();

        return PostListResponseDTO.builder()
                .totalPost((int) posts.getTotalElements())
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .responseDTOS(dtos)
                .build();
    }


    protected List<FixedHashtag> validateAndGetHashtags(List<String> hashtags) {
        if (hashtags == null || hashtags.isEmpty()) {
            return List.of();
        }

        // 중복 체크
        long distinctCount = hashtags.stream().distinct().count();
        if (distinctCount != hashtags.size()) {
            throw new CustomException(PostErrorCode.DUPLICATE_HASHTAG_NOT_ALLOWED);
        }

        return hashtags.stream()
                .map(FixedHashtag::fromDisplayName)
                .toList();
    }

}
