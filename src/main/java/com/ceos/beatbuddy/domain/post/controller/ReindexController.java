package com.ceos.beatbuddy.domain.post.controller;

import com.ceos.beatbuddy.domain.post.application.ReindexService;
import com.ceos.beatbuddy.global.ResponseTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ReindexController {
    private final ReindexService reindexService;
    
    @PostMapping("/reindex-posts")
    public ResponseTemplate reindexPosts() {
        reindexService.reindexAllFreePosts();
        return ResponseTemplate.builder()
                .status(200)
                .message("모든 게시글 재인덱싱이 완료되었습니다.")
                .build();
    }
}