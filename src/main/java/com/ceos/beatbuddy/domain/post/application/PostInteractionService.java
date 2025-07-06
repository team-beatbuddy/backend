package com.ceos.beatbuddy.domain.post.application;

import com.ceos.beatbuddy.domain.comment.repository.CommentRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.dto.PostInteractionStatus;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.domain.post.exception.PostErrorCode;
import com.ceos.beatbuddy.domain.post.repository.PostRepository;
import com.ceos.beatbuddy.domain.scrapandlike.entity.PostInteractionId;
import com.ceos.beatbuddy.domain.scrapandlike.entity.PostLike;
import com.ceos.beatbuddy.domain.scrapandlike.entity.PostScrap;
import com.ceos.beatbuddy.domain.scrapandlike.repository.PostLikeRepository;
import com.ceos.beatbuddy.domain.scrapandlike.repository.PostScrapRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostInteractionService {
    private final MemberService memberService;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostScrapRepository postScrapRepository;
    private final CommentRepository commentRepository;
    @Transactional
    public void likePost(Long postId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        Post post = validateAndGetPost(postId);

        PostInteractionId likeId = new PostInteractionId(memberId, post.getId());
        if (postLikeRepository.existsById(likeId)) {
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        }

        PostLike postLike = PostLike.builder()
                .post(post)
                .member(member)
                .id(likeId)
                .build();

        postLikeRepository.save(postLike);
        postRepository.increaseLike(postId);
    }


    @Transactional
    public void deletePostLike(Long postId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);


        Post post = validateAndGetPost(postId);

        PostInteractionId likeId = new PostInteractionId(member.getId(), post.getId());
        PostLike postLike = postLikeRepository.findById(likeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_LIKE));

        postLikeRepository.delete(postLike);
        postRepository.decreaseLike(postId);

    }

    @Transactional
    public void scrapPost(Long postId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);


        Post post = validateAndGetPost(postId);

        PostInteractionId scrapId = new PostInteractionId(memberId, post.getId());
        if (postScrapRepository.existsById(scrapId)) {
            throw new CustomException(ErrorCode.ALREADY_SCRAPPED);
        }

        PostScrap postScrap = PostScrap.builder()
                .post(post)
                .member(member)
                .id(scrapId)
                .build();

        postScrapRepository.save(postScrap);
        postRepository.increaseScrap(postId);
    }


    @Transactional
    public void deletePostScrap(Long postId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);


        Post post = validateAndGetPost(postId);

        PostInteractionId scrapId = new PostInteractionId(member.getId(), post.getId());
        PostScrap postScrap = postScrapRepository.findById(scrapId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SCRAP));

        postScrapRepository.delete(postScrap);
        postRepository.decreaseScrap(postId);
    }


    private Set<Long> getLikedPostIds(Long memberId, List<Long> postIds) {
        return postLikeRepository.findAllByMember_IdAndPost_IdIn(memberId, postIds)
                .stream()
                .map(pl -> pl.getPost().getId())
                .collect(Collectors.toSet());
    }

    private Set<Long> getCommentedPostIds(Long memberId, List<Long> postIds) {
        return commentRepository.findAllByMember_IdAndPost_IdIn(memberId, postIds)
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
        return  postRepository.findById(postId).orElseThrow(
                () -> new CustomException(PostErrorCode.POST_NOT_EXIST)
        );
    }

    public PostInteractionStatus getAllPostInteractions(Long memberId, List<Long> postIds) {
        Set<Long> liked = getLikedPostIds(memberId, postIds);
        Set<Long> scrapped = getScrappedPostIds(memberId, postIds);
        Set<Long> commented = getCommentedPostIds(memberId, postIds);

        return new PostInteractionStatus(liked, scrapped, commented);
    }

}
