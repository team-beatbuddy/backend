package com.ceos.beatbuddy.domain.post.application;

import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.repository.FreePostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReindexService {
    private final FreePostRepository freePostRepository;
    private final FreePostSearchService freePostSearchService;
    
    public void reindexAllFreePosts() {
        log.info("Starting reindexing of all FreePosts");
        
        List<FreePost> allPosts = freePostRepository.findAll();
        log.info("Found {} posts to reindex", allPosts.size());
        
        int count = 0;
        for (FreePost post : allPosts) {
            try {
                freePostSearchService.save(post);
                count++;
                if (count % 100 == 0) {
                    log.info("Reindexed {} posts", count);
                }
            } catch (Exception e) {
                log.error("Failed to reindex post {}: {}", post.getId(), e.getMessage());
            }
        }
        
        log.info("Completed reindexing. Successfully reindexed {} out of {} posts", count, allPosts.size());
    }
}