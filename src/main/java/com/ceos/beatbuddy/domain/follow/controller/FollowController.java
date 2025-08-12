package com.ceos.beatbuddy.domain.follow.controller;

import com.ceos.beatbuddy.domain.follow.application.FollowService;
import com.ceos.beatbuddy.domain.follow.dto.FollowResponseDTO;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/follows")
@Tag(name = "Follow Controller", description = "팔로우 기능\n")
public class FollowController implements FollowApiDocs{
    private final FollowService followService;

    @Override
    @PostMapping("/{followingId}")
    public ResponseEntity<ResponseDTO<FollowResponseDTO>> addFollow(@Valid @PathVariable Long followingId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        FollowResponseDTO result = followService.follow(memberId, followingId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_FOLLOW.getHttpStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_FOLLOW, result));
    }

    @Override
    @DeleteMapping("/{followingId}")
    public ResponseEntity<ResponseDTO<String>> deleteFollow(@Valid @PathVariable Long followingId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        followService.unfollow(memberId, followingId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_FOLLOW_DELETE.getHttpStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_FOLLOW_DELETE, "팔로우 취소 완료"));
    }

    // 팔로잉 목록 조회 (본인 또는 다른 사용자)
    @Override
    @GetMapping("/followings")
    public ResponseEntity<ResponseDTO<List<FollowResponseDTO>>> getFollowings(
            @RequestParam(required = false) Long targetMemberId
    ) {
        Long currentMemberId = SecurityUtils.getCurrentMemberId();
        Long actualTargetId = targetMemberId != null ? targetMemberId : currentMemberId;
        
        List<FollowResponseDTO> result = followService.getFollowings(actualTargetId, currentMemberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_FOLLOWINGS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_FOLLOWINGS, result));
    }

    // 팔로워 목록 조회 (본인 또는 다른 사용자)
    @Override
    @GetMapping("/followers")
    public ResponseEntity<ResponseDTO<List<FollowResponseDTO>>> getFollowers(
            @RequestParam(required = false) Long targetMemberId
    ) {
        Long currentMemberId = SecurityUtils.getCurrentMemberId();
        Long actualTargetId = targetMemberId != null ? targetMemberId : currentMemberId;
        
        List<FollowResponseDTO> result = followService.getFollowers(actualTargetId, currentMemberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_FOLLOWERS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_FOLLOWERS, result));
    }
}
