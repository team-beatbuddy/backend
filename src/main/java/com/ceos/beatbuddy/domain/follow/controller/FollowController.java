package com.ceos.beatbuddy.domain.follow.controller;

import com.ceos.beatbuddy.domain.follow.application.FollowService;
import com.ceos.beatbuddy.domain.follow.dto.FollowResponseDTO;
import com.ceos.beatbuddy.domain.member.entity.Member;
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

    // 내가 팔로우한 사람들 목록
    @Override
    @GetMapping("/followings")
    public ResponseEntity<ResponseDTO<List<FollowResponseDTO>>> getFollowings() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        List<FollowResponseDTO> result = followService.getFollowings(memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_FOLLOWINGS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_FOLLOWINGS, result));
    }
}
