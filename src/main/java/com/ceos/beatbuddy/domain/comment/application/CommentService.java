package com.ceos.beatbuddy.domain.comment.application;

import com.ceos.beatbuddy.domain.comment.dto.CommentRequestDto;
import com.ceos.beatbuddy.domain.comment.dto.CommentResponseDto;
import com.ceos.beatbuddy.domain.comment.entity.Comment;
import com.ceos.beatbuddy.domain.comment.entity.PostCommentCreatedEvent;
import com.ceos.beatbuddy.domain.comment.exception.CommentErrorCode;
import com.ceos.beatbuddy.domain.comment.repository.CommentRepository;
import com.ceos.beatbuddy.domain.follow.repository.FollowRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.application.PostService;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.domain.scrapandlike.entity.CommentLike;
import com.ceos.beatbuddy.domain.scrapandlike.repository.CommentLikeRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final MemberService memberService;
    private final PostService postService;
    private final FollowRepository followRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CommentLikeRepository commentLikeRepository;
    @PersistenceContext
    private EntityManager em;

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

        // ========  알림 전송
        eventPublisher.publishEvent(new PostCommentCreatedEvent(post, savedComment, member));

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

        // ========  알림 전송
        eventPublisher.publishEvent(new PostCommentCreatedEvent(post, savedReply, member));

        return CommentResponseDto.from(savedReply, true, isFollowing, false); // 자신이 작성, 스스로는 차단할 수 없음
    }

    public CommentResponseDto getComment(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(memberId, comment.getMember().getId());
        boolean isBlockedMember = memberService.isBlocked(memberId, comment.getMember().getId());

        return CommentResponseDto.from(comment, comment.getMember().getId().equals(memberId), isFollowing, isBlockedMember); // 자신이 작성한 댓글인지 여부
    }

    public Page<CommentResponseDto> getAllComments(Long postId, int page, int size, Long memberId) {
        if (page < 1) {
            throw new CustomException(ErrorCode.PAGE_OUT_OF_BOUNDS);
        }

        // 전체 댓글 조회 (createdAt 기준 정렬)
        List<Comment> allComments = commentRepository.findAllByPost_IdOrderByCreatedAtAsc(postId);

        // 차단/팔로우 처리
        Set<Long> blockedMemberIds = memberService.getBlockedMemberIds(memberId);
        Set<Long> followingIds = followRepository.findFollowingMemberIds(memberId);

        // 익명 처리용 ID → 익명 N 매핑
        List<Long> anonymousAuthorIds = allComments.stream()
                .filter(Comment::isAnonymous)
                .map(c -> c.getMember().getId())
                .distinct()
                .toList();

        Map<Long, String> anonymousNameMap = new HashMap<>();
        for (int i = 0; i < anonymousAuthorIds.size(); i++) {
            anonymousNameMap.put(anonymousAuthorIds.get(i), "익명 " + (i + 1));
        }

        // 트리 정렬: 부모 댓글 아래에 자식 댓글 붙이기
        List<Comment> sortedComments = new ArrayList<>();
        Map<Long, List<Comment>> repliesGrouped = allComments.stream()
                .filter(c -> c.getReply() != null)
                .collect(Collectors.groupingBy(c -> c.getReply().getId()));

        allComments.stream()
                .filter(c -> c.getReply() == null)
                .forEach(parent -> {
                    sortedComments.add(parent);
                    List<Comment> replies = repliesGrouped.getOrDefault(parent.getId(), Collections.emptyList());
                    sortedComments.addAll(replies);
                });

        // 페이징 수동 처리
        int start = Math.min((page - 1) * size, sortedComments.size());
        int end = Math.min(start + size, sortedComments.size());

        List<Comment> paged = sortedComments.subList(start, end);

        List<CommentResponseDto> dtoList = paged.stream().map(comment -> {
            Long writerId = comment.getMember().getId();
            boolean isAuthor = writerId.equals(memberId);
            boolean isFollowing = followingIds.contains(writerId);
            boolean isBlocked = blockedMemberIds.contains(writerId);

            String mappedName = comment.isAnonymous()
                    ? anonymousNameMap.get(writerId)
                    : comment.getMember().getNickname();

            return new CommentResponseDto(
                    comment.getId(),
                    isBlocked ? "차단한 멤버의 댓글입니다." : comment.getContent(),
                    comment.isAnonymous(),
                    comment.getReply() != null ? comment.getReply().getId() : null,
                    mappedName,
                    comment.getLikes(),
                    comment.getCreatedAt(),
                    isAuthor,
                    writerId,
                    isFollowing,
                    isBlocked
            );
        }).toList();

        return new PageImpl<>(dtoList, PageRequest.of(page - 1, size), sortedComments.size());
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

        // 1. 댓글에 달린 모든 좋아요 먼저 삭제
        commentLikeRepository.deleteByComment_Id(commentId);

        List<Comment> childReplies = commentRepository.findAllByReplyId(commentId);
        commentRepository.deleteAll(childReplies); // 또는 soft delete
        
        // 2. 댓글 개수 감소
        comment.getPost().decreaseComments();
        
        // 3. 댓글 삭제
        commentRepository.delete(comment);
    }

    @Transactional
    public CommentResponseDto addLike(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

        Member member = memberService.validateAndGetMember(memberId);

        boolean isBlockedMember = memberService.isBlocked(memberId, comment.getMember().getId());
        if (isBlockedMember) {
            throw new CustomException(ErrorCode.BLOCKED_MEMBER);
        }

        // 이미 좋아요를 누른 경우 예외 처리
        if (commentLikeRepository.existsByComment_IdAndMember_Id(commentId, memberId)) {
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        }

        // 좋아요 엔티티 생성 및 저장
        CommentLike commentLike = CommentLike.builder()
                .member(member)
                .comment(comment)
                .build();

        commentLikeRepository.save(commentLike);

        // 좋아요 로직 구현 필요 (중복 좋아요 방지 등)
        commentRepository.increaseLikesById(commentId); // 좋아요 수 증가

        em.refresh(comment);

        // 팔로잉 여부
        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(memberId, comment.getMember().getId());
        // 차단 여부
        return CommentResponseDto.from(comment, comment.getMember().getId().equals(memberId), isFollowing, isBlockedMember); // 자신이 작성한 댓글인지 여부
    }

    @Transactional
    public CommentResponseDto deleteLike(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

        boolean isBlockedMember = memberService.isBlocked(memberId, comment.getMember().getId());
        if (isBlockedMember) {
            throw new CustomException(ErrorCode.BLOCKED_MEMBER);
        }

        // 좋아요 엔티티가 존재하는지 확인
        if (!commentLikeRepository.existsByComment_IdAndMember_Id(commentId, memberId)) {
            throw new CustomException(ErrorCode.NOT_FOUND_LIKE);
        }

        // 좋아요 엔티티 삭제
        commentLikeRepository.deleteByComment_IdAndMember_Id(commentId, memberId);

        // 좋아요 로직 구현 필요 (중복 좋아요 방지 등)
        commentRepository.decreaseLikesById(commentId); // 좋아요 수 감소

        em.refresh(comment);

        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(memberId, comment.getMember().getId());

        return CommentResponseDto.from(comment, comment.getMember().getId().equals(memberId), isFollowing, isBlockedMember); // 자신이 작성한 댓글인지 여부
    }
}
