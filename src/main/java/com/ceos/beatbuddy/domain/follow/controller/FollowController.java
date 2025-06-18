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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
