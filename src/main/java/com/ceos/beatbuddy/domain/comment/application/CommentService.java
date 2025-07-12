package com.ceos.beatbuddy.domain.comment.application;

import com.ceos.beatbuddy.domain.comment.dto.CommentRequestDto;
import com.ceos.beatbuddy.domain.comment.dto.CommentResponseDto;
import com.ceos.beatbuddy.domain.comment.entity.Comment;
import com.ceos.beatbuddy.domain.comment.exception.CommentErrorCode;
import com.ceos.beatbuddy.domain.comment.repository.CommentRepository;
import com.ceos.beatbuddy.domain.follow.repository.FollowRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.application.PostService;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final MemberService memberService;
    private final PostService postService;
    private final FollowRepository followRepository;

    @Transactional
    public CommentResponseDto createComment(Long memberId, Long postId, CommentRequestDto requestDto) {
        Member member = memberService.validateAndGetMember(memberId);

        Post post = postService.validateAndGetPost(postId);

        Comment comment = Comment.builder()
                .content(requestDto.content())
                .isAnonymous(requestDto.isAnonymous())
                .member(member)
                .post(post)
                .likes(0)
                .build();

        Comment savedComment = commentRepository.save(comment);
        post.increaseComments();

        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(memberId, member.getId());

        return CommentResponseDto.from(savedComment, true, isFollowing, false); // 자신이 작성, 스스로는 차단할 수 없음
    }

    @Transactional
    public CommentResponseDto createReply(Long memberId, Long postId, Long commentId, CommentRequestDto requestDto) {
        Member member = memberService.validateAndGetMember(memberId);

        Post post = postService.validateAndGetPost(postId);

        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

        Comment reply = Comment.builder()
                .content(requestDto.content())
                .isAnonymous(requestDto.isAnonymous())
                .member(member)
                .post(post)
                .reply(parentComment)
                .likes(0)
                .build();

        Comment savedReply = commentRepository.save(reply);
        post.increaseComments();

        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(memberId, member.getId());

        return CommentResponseDto.from(savedReply, true, isFollowing, false); // 자신이 작성, 스스로는 차단할 수 없음
    }

    public CommentResponseDto getComment(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(memberId, comment.getMember().getId());
        boolean isBlockedByWriter = memberService.isBlocked(memberId, comment.getMember().getId());

        return CommentResponseDto.from(comment, comment.getMember().getId().equals(memberId), isFollowing, isBlockedByWriter); // 자신이 작성한 댓글인지 여부
    }

    public Page<CommentResponseDto> getAllComments(Long postId, int page, int size, Long memberId) {
        // 페이지 유효성 조회
        if (page < 1) {
            throw new CustomException(ErrorCode.PAGE_OUT_OF_BOUNDS);
        }
        Pageable pageable = PageRequest.of(page-1, size);

        // 차단한 사용자
        Set<Long> blockedMemberIds = memberService.getBlockedMemberIds(memberId);

        Page<Comment> comments =  commentRepository.findAllByPost_Id(postId, pageable);

        Set<Long> followingIds = followRepository.findFollowingMemberIds(memberId);
        return comments.map(comment ->
                    CommentResponseDto.from(comment, comment.getMember().getId().equals(memberId),
                            followingIds.contains(comment.getMember().getId()),
                            blockedMemberIds.contains(comment.getMember().getId())
                    )
        );
    }

    @Transactional
public CommentResponseDto updateComment(Long commentId, Long memberId, CommentRequestDto requestDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        Comment updatedComment = Comment.builder()
                .id(comment.getId())
                .content(requestDto.content())
                .isAnonymous(requestDto.isAnonymous())
                .member(comment.getMember())
                .post(comment.getPost())
                .reply(comment.getReply())
                .likes(comment.getLikes())
                .build();

        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(memberId, comment.getMember().getId());

        return CommentResponseDto.from(commentRepository.save(updatedComment), true, isFollowing, false); // 자신이 작성한 댓글이므로 true, 자신을 차단할 수 없음.
    }

    @Transactional
    public void deleteComment(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        comment.getPost().decreaseComments();
        commentRepository.delete(comment);
    }

    @Transactional
    public CommentResponseDto addLike(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

        boolean isBlockedByWriter = memberService.isBlocked(memberId, comment.getMember().getId());
        if (isBlockedByWriter) {
            throw new CustomException(ErrorCode.BLOCKED_BY_WRITER);
        }

        // 좋아요 로직 구현 필요 (중복 좋아요 방지 등)
        comment.increaseLike();

        // 팔로잉 여부
        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(memberId, comment.getMember().getId());
        // 차단 여부

        return CommentResponseDto.from(comment, comment.getMember().getId().equals(memberId), isFollowing, isBlockedByWriter); // 자신이 작성한 댓글인지 여부
    }

    @Transactional
    public CommentResponseDto deleteLike(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

        boolean isBlockedByWriter = memberService.isBlocked(memberId, comment.getMember().getId());
        if (isBlockedByWriter) {
            throw new CustomException(ErrorCode.BLOCKED_BY_WRITER);
        }

        // 좋아요 로직 구현 필요 (중복 좋아요 방지 등)
        comment.decreaseLike();
        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(memberId, comment.getMember().getId());

        return CommentResponseDto.from(comment, comment.getMember().getId().equals(memberId), isFollowing, isBlockedByWriter); // 자신이 작성한 댓글인지 여부
    }
}
