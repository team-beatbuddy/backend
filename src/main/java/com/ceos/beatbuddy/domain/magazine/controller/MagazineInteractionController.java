package com.ceos.beatbuddy.domain.magazine.controller;

import com.ceos.beatbuddy.domain.magazine.application.MagazineInteractionService;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/magazines")
@RequiredArgsConstructor
@Tag(name = "Magazine Interaction Controller", description = "매거진 좋아요 기능\n")
public class MagazineInteractionController implements MagazineInteractionApiDocs {
    private final MagazineInteractionService magazineInteractionService;

    // 매거진에 좋아요 표시하기
    @Override
    @PostMapping("/{magazineId}/like")
    public ResponseEntity<ResponseDTO<String>> likeMagazine(@PathVariable Long magazineId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        magazineInteractionService.likeMagazine(magazineId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_LIKE_MAGAZINE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_LIKE_MAGAZINE, "좋아요를 표시했습니다."));
    }

    // 매거진 좋아요 삭제
    @Override
    @DeleteMapping("/{magazineId}/like")
    public ResponseEntity<ResponseDTO<String>> deleteLikeMagazine(@PathVariable Long magazineId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        magazineInteractionService.deleteLikeMagazine(magazineId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_DELETE_LIKE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_DELETE_LIKE, "좋아요를 취소했습니다."));
    }
}
