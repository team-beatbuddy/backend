package com.ceos.beatbuddy.domain.post.controller;

import com.ceos.beatbuddy.domain.post.application.FreePostSearchService;
import com.ceos.beatbuddy.domain.post.dto.PostListResponseDTO;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/es/free-posts")
public class FreePostSearchController implements FreePostApiDocs {
    private final FreePostSearchService freePostSearchService;


    @Override
    @GetMapping("/search")
    public ResponseEntity<ResponseDTO<PostListResponseDTO>> searchPosts(
            @RequestParam (required = false) @NotNull(message = "검색 시, 키워드는 필수입니다.") @Size(min = 2, message = "2글자 이상 입력해야 합니다.") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        PostListResponseDTO result = freePostSearchService.searchPosts(keyword, page, size, memberId);

        if (result.getResponseDTOS().isEmpty()) {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_BUT_EMPTY_LIST.getStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_BUT_EMPTY_LIST, result));
        }

        return ResponseEntity
                .status(SuccessCode.SUCCESS_POST_SEARCH.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_POST_SEARCH, result));
    }
}
