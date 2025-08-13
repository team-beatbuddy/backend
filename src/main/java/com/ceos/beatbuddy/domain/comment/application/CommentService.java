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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final MemberService memberService;
    private final PostService postService;
    private final FollowRepository followRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CommentLikeRepository commentLikeRepository;
    private final AnonymousNicknameService anonymousNicknameService;

    @Transactional
    public CommentResponseDto createComment(Long memberId, Long postId, CommentRequestDto requestDto) {
        Member member = memberService.validateAndGetMember(memberId);

        Post post = postService.validateAndGetPost(postId);

        // 익명 닉네임 처리
        String anonymousNickname = null;
        if (requestDto.isAnonymous()) {
            anonymousNickname = anonymousNicknameService.getOrCreateAnonymousNickname(
                    postId, memberId, post.getMember().getId(), post.isAnonymous());
        }

        Comment comment = Comment.builder()
                .content(requestDto.content())
                .isAnonymous(requestDto.isAnonymous())
                .anonymousNickname(anonymousNickname)
                .member(member)
                .post(post)
                .likes(0)
                .isDeleted(false)
                .build();

        Comment savedComment = commentRepository.save(comment);
        post.increaseComments();

        boolean isFollowing = followRepository.existsByFollower_IdAndFollowing_Id(memberId, member.getId());

        // ========  알림 전송
        eventPublisher.publishEvent(new PostCommentCreatedEvent(post, savedComment, member));

        boolean isPostWriter = post.getMember().getId().equals(member.getId());
        return CommentResponseDto.from(savedComment, true, isFollowing, false, false, isPostWriter, post.isAnonymous()); // 자신이 작성, 스스로는 차단할 수 없음
    }

    @Transactional
    public CommentResponseDto createReply(Long memberId, Long postId, Long commentId, CommentRequestDto requestDto) {
        Member member = memberService.validateAndGetMember(memberId);

        Post post = postService.validateAndGetPost(postId);

        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

        // 익명 닉네임 처리
        String anonymousNickname = null;
        if (requestDto.isAnonymous()) {
            anonymousNickname = anonymousNicknameService.getOrCreateAnonymousNickname(
                    postId, memberId, post.getMember().getId(), post.isAnonymous());
        }

        Comment reply = Comment.builder()
                .content(requestDto.content())
                .isAnonymous(requestDto.isAnonymous())
                .anonymousNickname(anonymousNickname)
                .member(member)
                .post(post)
                .reply(parentComment)
                .likes(0)
                .isDeleted(false) // 기본값 false로 설정
                .build();

        Comment savedReply = commentRepository.save(reply);
        post.increaseComments();

        boolean isFollowing = followRepository.existsByFollower_IdAndFollowing_Id(memberId, member.getId());

        // ========  알림 전송
        eventPublisher.publishEvent(new PostCommentCreatedEvent(post, savedReply, member));

        boolean isPostWriter = post.getMember().getId().equals(member.getId());
        return CommentResponseDto.from(savedReply, true, isFollowing, false, false, isPostWriter, post.isAnonymous()); // 자신이 작성, 스스로는 차단할 수 없음
    }

    public CommentResponseDto getComment(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

        boolean isFollowing = followRepository.existsByFollower_IdAndFollowing_Id(memberId, comment.getMember().getId());
        boolean isBlockedMember = memberService.isBlocked(memberId, comment.getMember().getId());
        boolean isPostWriter = comment.getPost().getMember().getId().equals(comment.getMember().getId());

        return CommentResponseDto.from(comment, comment.getMember().getId().equals(memberId), isFollowing, isBlockedMember, comment.isDeleted(), isPostWriter, comment.getPost().isAnonymous()); // 자신이 작성한 댓글인지 여부
    }

    public Page<CommentResponseDto> getAllComments(Long postId, int page, int size, Long memberId) {
        if (page < 1) {
            throw new CustomException(ErrorCode.PAGE_OUT_OF_BOUNDS);
        }

        // 전체 댓글 조회 (createdAt 기준 정렬)
        List<Comment> allComments = commentRepository.findAllByPost_IdOrderByCreatedAtAsc(postId);

        // 해당 포스트의 작성자 ID 및 익명 여부 가져오기
        Long postWriterId = allComments.isEmpty() ? null : allComments.get(0).getPost().getMember().getId();
        boolean isPostAnonymous = allComments.isEmpty() ? false : allComments.get(0).getPost().isAnonymous();

        // 차단/팔로우 처리
        Set<Long> blockedMemberIds = memberService.getBlockedMemberIds(memberId);
        Set<Long> followingIds = followRepository.findFollowingMemberIds(memberId);

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
            boolean isPostWriter = writerId.equals(postWriterId);

            String mappedName = comment.isAnonymous() 
                    ? (comment.getAnonymousNickname() != null ? comment.getAnonymousNickname() : "익명")
                    : (comment.getMember().getPostProfileInfo() != null && comment.getMember().getPostProfileInfo().getPostProfileNickname() != null
                        ? comment.getMember().getPostProfileInfo().getPostProfileNickname()
                        : comment.getMember().getNickname());

            String imageUrl = comment.isAnonymous() ? "" :
                    (comment.getMember().getPostProfileInfo() != null && comment.getMember().getPostProfileInfo().getPostProfileImageUrl() != null
                        ? comment.getMember().getPostProfileInfo().getPostProfileImageUrl()
                        : (comment.getMember().getProfileImage() != null
                                ? comment.getMember().getProfileImage()
                                : ""));

            // isPostWriter 결정 로직
            Boolean finalIsPostWriter = isPostWriter;
            if (isPostWriter) {
                // 글 작성자인 경우
                if (isPostAnonymous && !comment.isAnonymous()) {
                    // 익명 게시물 + 실명 댓글 → 작성자임을 숨김
                    finalIsPostWriter = false;
                } else if (!isPostAnonymous && comment.isAnonymous()) {
                    // 실명 게시물 + 익명 댓글 → 작성자임을 숨김
                    finalIsPostWriter = false;
                }
                // 익명 게시물 + 익명 댓글, 실명 게시물 + 실명 댓글은 그대로 유지
            }

            return new CommentResponseDto(
                    comment.getId(),
                    isBlocked ? "차단한 멤버의 댓글입니다." : comment.getContent(),
                    comment.isAnonymous(),
                    comment.getReply() != null ? comment.getReply().getId() : null,
                    mappedName,
                    imageUrl,
                    comment.getLikes(),
                    comment.getCreatedAt(),
                    isAuthor,
                    writerId,
                    isFollowing,
                    isBlocked,
                    comment.isDeleted(), // 삭제 여부 추가
                    finalIsPostWriter
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

        // 익명 닉네임 처리 (수정 시에도 기존 닉네임 유지 또는 새로 생성)
        String anonymousNickname = comment.getAnonymousNickname();
        if (requestDto.isAnonymous() && anonymousNickname == null) {
            // 기존에 익명이 아니었다가 익명으로 변경된 경우 새로 생성
            anonymousNickname = anonymousNicknameService.getOrCreateAnonymousNickname(
                    comment.getPost().getId(), memberId, comment.getPost().getMember().getId(), comment.getPost().isAnonymous());
        } else if (!requestDto.isAnonymous()) {
            // 익명에서 실명으로 변경된 경우 null로 설정
            anonymousNickname = null;
        }

        Comment updatedComment = Comment.builder()
                .id(comment.getId())
                .content(requestDto.content())
                .isAnonymous(requestDto.isAnonymous())
                .anonymousNickname(anonymousNickname)
                .member(comment.getMember())
                .post(comment.getPost())
                .reply(comment.getReply())
                .likes(comment.getLikes())
                .build();

        boolean isFollowing = followRepository.existsByFollower_IdAndFollowing_Id(memberId, comment.getMember().getId());
        
        Comment saved = commentRepository.save(updatedComment);
        boolean isPostWriter = saved.getPost().getMember().getId().equals(saved.getMember().getId());
        return CommentResponseDto.from(saved, true, isFollowing, false, false, isPostWriter, saved.getPost().isAnonymous()); // 자신이 작성한 댓글이므로 true, 자신을 차단할 수 없음.
    }

    @Transactional
    public void deleteComment(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        // 1. 댓글에 달린 모든 좋아요 먼저 삭제
        commentLikeRepository.deleteByCommentId(commentId);

        // 2. 댓글 개수 감소
        comment.getPost().decreaseComments();


        boolean hasChildReplies = commentRepository.existsByReplyId(commentId);
        if (hasChildReplies) {
            comment.setDeleted(true);
            commentRepository.save(comment);
            return;
        }

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

        // 팔로잉 여부
        boolean isFollowing = followRepository.existsByFollower_IdAndFollowing_Id(memberId, comment.getMember().getId());
        // 차단 여부
        boolean isPostWriter = comment.getPost().getMember().getId().equals(comment.getMember().getId());
        return CommentResponseDto.from(comment, comment.getMember().getId().equals(memberId), isFollowing, isBlockedMember, false, isPostWriter, comment.getPost().isAnonymous()); // 자신이 작성한 댓글인지 여부
    }

    @Transactional
    public CommentResponseDto deleteLike(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

        boolean isBlockedMember = memberService.isBlocked(memberId, comment.getMember().getId());
        if (isBlockedMember) {
            throw new CustomException(ErrorCode.BLOCKED_MEMBER);
        }

        // 좋아요 엔티티 삭제 (실제 삭제된 행 수 확인)
        int deletedCount = commentLikeRepository.deleteByComment_IdAndMember_Id(commentId, memberId);
        if (deletedCount == 0) {
            throw new CustomException(ErrorCode.NOT_FOUND_LIKE);
        }
        
        if (deletedCount > 1) {
            log.warn("Multiple comment likes deleted for single request - commentId: {}, memberId: {}, deletedCount: {}", 
                    commentId, memberId, deletedCount);
        }

        // 실제 삭제된 수만큼 카운트 감소
        if (deletedCount > 1) {
            commentRepository.decreaseLikesById(commentId, deletedCount);
        } else {
            commentRepository.decreaseLikesById(commentId);
        }

        boolean isFollowing = followRepository.existsByFollower_IdAndFollowing_Id(memberId, comment.getMember().getId());
        boolean isPostWriter = comment.getPost().getMember().getId().equals(comment.getMember().getId());

        return CommentResponseDto.from(comment, comment.getMember().getId().equals(memberId), isFollowing, isBlockedMember, false, isPostWriter, comment.getPost().isAnonymous()); // 자신이 작성한 댓글인지 여부
    }
}
