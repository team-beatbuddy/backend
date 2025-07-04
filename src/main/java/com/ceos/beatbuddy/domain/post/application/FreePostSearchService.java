package com.ceos.beatbuddy.domain.post.application;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.ceos.beatbuddy.domain.post.dto.PostListResponseDTO;
import com.ceos.beatbuddy.domain.post.dto.PostPageResponseDTO;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.FreePostDocument;
import com.ceos.beatbuddy.domain.post.repository.FreePostRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import com.ceos.beatbuddy.global.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class FreePostSearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final FreePostRepository freePostRepository;
    private final PostLikeScrapService postLikeScrapService;

    public void save(FreePost post) {
        try {
            FreePostDocument document = FreePostDocument.toDTO(post); // DTO or 도큐먼트 변환
            elasticsearchClient.index(i -> i
                    .index("post")
                    .id(post.getId().toString())
                    .document(document)
            );
        } catch (IOException e) {
            log.warn("ES 인덱싱 실패: postId={}, error={}", post.getId(), e.getMessage());
            throw new CustomException(ErrorCode.ELASTICSEARCH_INDEXING_FAILED);
        }
    }

    public void delete(Long postId) {
        try {
            elasticsearchClient.delete(d -> d
                    .index("post")
                    .id(postId.toString())
            );
        } catch (IOException e) {
            log.warn("ES 인덱싱 삭제 실패: postId={}, error={}", postId, e.getMessage());
            throw new CustomException(ErrorCode.ELASTICSEARCH_DELETION_FAILED);
        }
    }

    public PostListResponseDTO searchPosts(String keyword, int page, int size, Long memberId) {
        int adjustedPage = Math.max(0, page - 1);

        try {
            // 1. Elasticsearch에서 검색 (ID, title, content, hashtags 기반)
            SearchResponse<FreePostDocument> response = elasticsearchClient.search(s -> s
                            .index("post")
                            .from(adjustedPage * size)
                            .size(size)
                            .query(q -> q
                                    .multiMatch(m -> m
                                            .fields("title", "content", "hashtags")
                                            .query(keyword)
                                    )
                            ),
                    FreePostDocument.class
            );

            if (response.hits().hits().isEmpty()) {
                return PostListResponseDTO.builder()
                        .totalPost(0)
                        .page(page)
                        .size(size)
                        .responseDTOS(Collections.emptyList())
                        .build();
            }

            // 2. 검색 결과 ID 추출
            List<String> documentIds = response.hits().hits().stream()
                    .map(Hit::id)
                    .toList();

            List<Long> postIds = documentIds.stream()
                    .map(Long::parseLong)
                    .toList();

            // 3. DB에서 실제 FreePost 조회
            List<FreePost> posts = freePostRepository.findAllById(postIds);

            Map<Long, FreePost> postMap = posts.stream()
                    .collect(Collectors.toMap(FreePost::getId, Function.identity()));
            List<FreePost> orderedPosts = postIds.stream()
                    .map(postMap::get)
                    .toList();

            // 4. 좋아요/스크랩/댓글 여부 IN 쿼리
            Set<Long> likedPostIds = postLikeScrapService.getLikedPostIds(memberId, postIds);
            Set<Long> scrappedPostIds = postLikeScrapService.getScrappedPostIds(memberId, postIds);
            Set<Long> commentedPostIds = postLikeScrapService.getCommentedPostIds(memberId, postIds);

            // 5. DTO로 변환
            List<PostPageResponseDTO> dtoList = orderedPosts.stream()
                    .map(post -> PostPageResponseDTO.toDTO(
                            post,
                            likedPostIds.contains(post.getId()),
                            scrappedPostIds.contains(post.getId()),
                            commentedPostIds.contains(post.getId())
                    ))
                    .toList();

            // 6. total 개수는 Elasticsearch 기준
            long total = response.hits().total() != null ? response.hits().total().value() : dtoList.size();

            return PostListResponseDTO.builder()
                    .totalPost((int) total)
                    .page(page)
                    .size(size)
                    .responseDTOS(dtoList)
                    .build();

        } catch (IOException e) {
            log.warn("ES 검색 실패: keyword={}, error={}", keyword, e.getMessage());
            throw new CustomException(ErrorCode.ELASTICSEARCH_SEARCH_FAILED);
        }
    }
}
