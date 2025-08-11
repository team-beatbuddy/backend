package com.ceos.beatbuddy.domain.comment.presentation;

import com.ceos.beatbuddy.domain.comment.application.CommentService;
import com.ceos.beatbuddy.domain.comment.dto.CommentRequestDto;
import com.ceos.beatbuddy.domain.comment.dto.CommentResponseDto;
import com.ceos.beatbuddy.global.SwaggerExamples;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Comment Controller", description = "댓글 컨트롤러")
@RequestMapping("/posts/{postId}/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "댓글 생성", description = "게시글에 새로운 댓글을 작성합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 작성 성공"),
            @ApiResponse(responseCode = "404", description = "게시글이 존재하지 않습니다")
    })
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long postId,
            @RequestBody CommentRequestDto requestDto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(commentService.createComment(memberId, postId, requestDto));
    }

    @PostMapping("/{commentId}/reply")
    @Operation(summary = "대댓글 생성", description = "댓글에 대한 답글을 작성합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "대댓글 작성 성공"),
            @ApiResponse(responseCode = "404", description = "댓글이 존재하지 않습니다")
    })
    public ResponseEntity<CommentResponseDto> createReply(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto requestDto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(commentService.createReply(memberId, postId, commentId, requestDto));
    }

    @GetMapping("/{commentId}")
    @Operation(summary = "댓글 조회", description = "특정 댓글을 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 조회 성공"),
            @ApiResponse(responseCode = "404", description = "댓글이 존재하지 않습니다")
    })
    public ResponseEntity<CommentResponseDto> getComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(commentService.getComment(commentId, memberId));
    }

    @GetMapping
    @Operation(summary = "댓글 목록 조회", description = "게시글의 모든 댓글을 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공")
    })
    public ResponseEntity<Page<CommentResponseDto>> getAllComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(commentService.getAllComments(postId, page, size, memberId));
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "댓글 수정", description = "댓글 내용을 수정합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
            @ApiResponse(responseCode = "403", description = "댓글 수정 권한이 없습니다")
    })
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto requestDto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(commentService.updateComment(commentId, memberId, requestDto));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "댓글 삭제 권한이 없습니다")
    })
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        commentService.deleteComment(commentId, memberId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{commentId}/like")
    @Operation(summary = "댓글 좋아요", description = "댓글에 좋아요를 추가합니다")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "좋아요 성공"),
        @ApiResponse(responseCode = "404", description = "리소스 없음",
                content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
                        examples = {@ExampleObject(name = "댓글 없음", value = SwaggerExamples.NOT_FOUND_COMMENT),
                                @ExampleObject(name = "멤버 없음", value = SwaggerExamples.MEMBER_NOT_EXIST)})),
        @ApiResponse(responseCode = "400", description = "차단한 사용자의 댓글입니다."),
        @ApiResponse(responseCode = "409", description = "이미 좋아요를 누른 댓글입니다",
                content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
                        examples = {@ExampleObject(name = "이미 좋아요", value = SwaggerExamples.ALREADY_LIKED)}))
    })
    public ResponseEntity<CommentResponseDto> addLike(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(commentService.addLike(commentId, memberId));
    }

    @DeleteMapping("/{commentId}/like")
    @Operation(summary = "댓글 좋아요 삭제", description = "댓글에 좋아요를 삭제합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "리소스 없음",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
                            examples = {@ExampleObject(name = "댓글 없음", value = SwaggerExamples.NOT_FOUND_COMMENT),
                                    @ExampleObject(name = "멤버 없음", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "좋아요 없음", value = SwaggerExamples.NOT_FOUND_LIKE)})),
            @ApiResponse(responseCode = "400", description = "차단한 사용자의 댓글입니다.")
    })
    public ResponseEntity<CommentResponseDto> deleteLike(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(commentService.deleteLike(commentId, memberId));
    }
}