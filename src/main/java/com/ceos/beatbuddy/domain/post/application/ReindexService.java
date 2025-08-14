package com.ceos.beatbuddy.domain.post.application;

import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.repository.FreePostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReindexService {
    private final FreePostRepository freePostRepository;
    private final FreePostSearchService freePostSearchService;
    
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public void reindexAllFreePosts() {
        log.info("Starting reindexing of all FreePosts");

        int page = 0;
        int size = 1000; // 배치 크기 조정 가능
        int success = 0;
        int total = 0;

        while (true) {
            var pageable = org.springframework.data.domain.PageRequest.of(page, size);
            var chunk = freePostRepository.findAll(pageable);
            if (chunk.isEmpty()) break;

            total += chunk.getNumberOfElements();

            for (FreePost post : chunk.getContent()) {
                try {
                    freePostSearchService.save(post);
                    success++;
                    if (success % 100 == 0) {
                        log.info("Reindexed {} posts (page={}, size={})", success, page, size);
                    }
                } catch (Exception e) {
                    log.error("Failed to reindex post {}: {}", post.getId(), e.getMessage());
                }
            }

            if (!chunk.hasNext()) break;
            page++;
        }

        log.info("Completed reindexing. Successfully reindexed {} out of {} posts", success, total);
    }
}