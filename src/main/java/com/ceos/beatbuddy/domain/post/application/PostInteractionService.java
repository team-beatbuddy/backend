package com.ceos.beatbuddy.domain.post.application;

import com.ceos.beatbuddy.domain.comment.repository.CommentRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.dto.PostInteractionStatus;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.domain.post.repository.PostRepository;
import com.ceos.beatbuddy.domain.scrapandlike.entity.PostLike;
import com.ceos.beatbuddy.domain.scrapandlike.entity.PostScrap;
import com.ceos.beatbuddy.domain.scrapandlike.repository.PostLikeRepository;
import com.ceos.beatbuddy.domain.scrapandlike.repository.PostScrapRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostInteractionService {
    private final MemberService memberService;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostScrapRepository postScrapRepository;
    private final CommentRepository commentRepository;
    private final PostValidationHelper postValidationHelper;
    @Transactional
    public void likePost(Long postId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        Post post = postValidationHelper.validateAndGetPost(postId);

        if (postLikeRepository.existsByMember_IdAndPost_Id(memberId, postId)) {
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        }

        PostLike postLike = PostLike.builder()
                .post(post)
                .member(member)
                .build();

        postLikeRepository.save(postLike);
        postRepository.increaseLike(postId);
    }


    @Transactional
    public void deletePostLike(Long postId, Long memberId) {
        memberService.validateAndGetMember(memberId);

        postValidationHelper.validateAndGetPost(postId);

        int deletedCount = postLikeRepository.deleteByMember_IdAndPost_Id(memberId, postId);
        if (deletedCount == 0) {
            throw new CustomException(ErrorCode.NOT_FOUND_LIKE);
        }
        
        if (deletedCount > 1) {
            log.warn("Multiple post likes deleted for single request - postId: {}, memberId: {}, deletedCount: {}", 
                    postId, memberId, deletedCount);
        }

        // 실제 삭제된 수만큼 카운트 감소
        if (deletedCount > 1) {
            postRepository.decreaseLike(postId, deletedCount);
        } else {
            postRepository.decreaseLike(postId);
        }
    }

    @Transactional
    public void scrapPost(Long postId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        Post post = postValidationHelper.validateAndGetPost(postId);

        if (postScrapRepository.existsByMember_IdAndPost_Id(memberId, postId)) {
            throw new CustomException(ErrorCode.ALREADY_SCRAPPED);
        }

        PostScrap postScrap = PostScrap.builder()
                .post(post)
                .member(member)
                .build();

        postScrapRepository.save(postScrap);
        postRepository.increaseScrap(postId);
    }


    @Transactional
    public void deletePostScrap(Long postId, Long memberId) {
        memberService.validateAndGetMember(memberId);

        postValidationHelper.validateAndGetPost(postId);

        int deletedCount = postScrapRepository.deleteByMember_IdAndPost_Id(memberId, postId);
        if (deletedCount == 0) {
            throw new CustomException(ErrorCode.NOT_FOUND_SCRAP);
        }
        
        if (deletedCount > 1) {
            log.warn("Multiple post scraps deleted for single request - postId: {}, memberId: {}, deletedCount: {}", 
                    postId, memberId, deletedCount);
        }

        // 실제 삭제된 수만큼 카운트 감소
        if (deletedCount > 1) {
            postRepository.decreaseScrap(postId, deletedCount);
        } else {
            postRepository.decreaseScrap(postId);
        }
    }


    private Set<Long> getLikedPostIds(Long memberId, List<Long> postIds) {
        return postLikeRepository.findAllByMember_IdAndPost_IdIn(memberId, postIds)
                .stream()
                .map(pl -> pl.getPost().getId())
                .collect(Collectors.toSet());
    }

    private Set<Long> getCommentedPostIds(Long memberId, List<Long> postIds) {
        return commentRepository.findAllByMember_IdAndPost_IdInAndIsDeletedFalse(memberId, postIds)
                .stream()
                .map(c -> c.getPost().getId())
                .collect(Collectors.toSet());
    }

    private Set<Long> getScrappedPostIds(Long memberId, List<Long> postIds) {
        return postScrapRepository.findAllByMember_IdAndPost_IdIn(memberId, postIds)
                .stream()
                .map(ps -> ps.getPost().getId())
                .collect(Collectors.toSet());
    }

    public Post validateAndGetPost(Long postId) {
        return postValidationHelper.validateAndGetPost(postId);
    }

    public PostInteractionStatus getAllPostInteractions(Long memberId, List<Long> postIds) {
        Set<Long> liked = getLikedPostIds(memberId, postIds);
        Set<Long> scrapped = getScrappedPostIds(memberId, postIds);
        Set<Long> commented = getCommentedPostIds(memberId, postIds);

        return new PostInteractionStatus(liked, scrapped, commented);
    }

}
