package com.ceos.beatbuddy.domain.post.controller;

import com.ceos.beatbuddy.domain.post.application.PostInteractionService;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/post-interactions")
@Tag(name = "Post Interactions Controller", description = "게시물 좋아요/스크랩 컨트롤러\n"
        + "게시물에 좋아요/스크랩하는 기능이 있습니다.")
public class PostLikeScrapController implements PostLikeScrapApiDocs {
    private final PostInteractionService postInteractionService;

    @Override
    @PostMapping("/{postId}/like")
    public ResponseEntity<ResponseDTO<String>> addPostLike(@PathVariable Long postId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        postInteractionService.likePost(postId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_LIKE_POST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_LIKE_POST, "좋아요를 눌렀습니다."));
    }
    @Override
    @DeleteMapping("/{postId}/like")
    public ResponseEntity<ResponseDTO<String>> deletePostLike(@PathVariable Long postId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        postInteractionService.deletePostLike(postId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_DELETE_LIKE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_DELETE_LIKE, "좋아요를 취소했습니다."));
    }

    @Override
    @PostMapping("/{postId}/scrap")
    public ResponseEntity<ResponseDTO<String>> scrapPost(@PathVariable Long postId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        postInteractionService.scrapPost(postId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_SCRAP_POST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_SCRAP_POST, "스크랩을 완료했습니다."));
    }

    @Override
    @DeleteMapping("/{postId}/scrap")
    public ResponseEntity<ResponseDTO<String>> deleteScrapPost(@PathVariable Long postId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        postInteractionService.deletePostScrap(postId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_DELETE_SCRAP.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_DELETE_SCRAP, "스크랩을 취소했습니다."));
    }
}
