package com.ceos.beatbuddy.domain.comment.application;

import com.ceos.beatbuddy.domain.comment.dto.CommentRequestDto;
import com.ceos.beatbuddy.domain.comment.dto.CommentResponseDto;
import com.ceos.beatbuddy.domain.comment.entity.Comment;
import com.ceos.beatbuddy.domain.comment.exception.CommentErrorCode;
import com.ceos.beatbuddy.domain.comment.repository.CommentRepository;
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

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final MemberService memberService;
    private final PostService postService;

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

        return CommentResponseDto.from(savedComment, true); // 자신이 작성
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

        return CommentResponseDto.from(savedReply, true); // 자신이 작성
    }

    public CommentResponseDto getComment(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));
        return CommentResponseDto.from(comment, comment.getMember().getId().equals(memberId)); // 자신이 작성한 댓글인지 여부
    }

    public Page<CommentResponseDto> getAllComments(Long postId, int page, int size, Long memberId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentRepository.findAllByPostId(postId, pageable);
        return comments.map(comment ->
            CommentResponseDto.from(comment, comment.getMember().getId().equals(memberId))
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

        return CommentResponseDto.from(commentRepository.save(updatedComment), true); // 자신이 작성한 댓글이므로 true
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

        // 좋아요 로직 구현 필요 (중복 좋아요 방지 등)
        comment.increaseLike();

        return CommentResponseDto.from(comment, comment.getMember().getId().equals(memberId)); // 자신이 작성한 댓글인지 여부
    }

    @Transactional
    public CommentResponseDto deleteLike(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

        // 좋아요 로직 구현 필요 (중복 좋아요 방지 등)
        comment.decreaseLike();

        return CommentResponseDto.from(comment, comment.getMember().getId().equals(memberId)); // 자신이 작성한 댓글인지 여부
    }
}
