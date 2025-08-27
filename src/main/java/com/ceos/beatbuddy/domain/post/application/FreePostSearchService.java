package com.ceos.beatbuddy.domain.post.application;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.ceos.beatbuddy.domain.follow.repository.FollowRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.dto.PostInteractionStatus;
import com.ceos.beatbuddy.domain.post.dto.PostListResponseDTO;
import com.ceos.beatbuddy.domain.post.dto.PostPageResponseDTO;
import com.ceos.beatbuddy.domain.post.entity.FixedHashtag;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.FreePostDocument;
import com.ceos.beatbuddy.domain.post.repository.FreePostRepository;
import com.ceos.beatbuddy.domain.recent_search.application.RecentSearchService;
import com.ceos.beatbuddy.domain.recent_search.entity.SearchTypeEnum;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class FreePostSearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final FreePostRepository freePostRepository;
    private final PostInteractionService postInteractionService;
    private final FollowRepository followRepository;
    private final MemberService memberService;
    private final RecentSearchService recentSearchService;
    private final PostResponseHelper postResponseHelper;

    @Async
    public void save(FreePost post) {
        try {
            FreePostDocument document = FreePostDocument.toDTO(post); // DTO or 도큐먼트 변환
            log.info("ES 인덱싱 (비동기) - postId: {}, hashtags: {}", post.getId(), document.getHashtags());
            elasticsearchClient.index(i -> i
                    .index("post")
                    .id(post.getId().toString())
                    .document(document)
            );
            log.debug("ES 인덱싱 완료 - postId: {}", post.getId());
        } catch (IOException e) {
            log.warn("ES 인덱싱 실패: postId={}, error={}", post.getId(), e.getMessage());
            // 비동기에서는 예외를 던지지 않고 로깅만 함 (Post 생성 실패로 이어지면 안 됨)
        } catch (Exception e) {
            log.error("ES 인덱싱 중 예상치 못한 오류: postId={}", post.getId(), e);
        }
    }

    @Async
    public void delete(Long postId) {
        try {
            elasticsearchClient.delete(d -> d
                    .index("post")
                    .id(postId.toString())
            );
            log.debug("ES 인덱싱 삭제 완료 - postId: {}", postId);
        } catch (IOException e) {
            log.warn("ES 인덱싱 삭제 실패: postId={}, error={}", postId, e.getMessage());
            // 비동기에서는 예외를 던지지 않고 로깅만 함
        } catch (Exception e) {
            log.error("ES 인덱싱 삭제 중 예상치 못한 오류: postId={}", postId, e);
        }
    }

    public PostListResponseDTO searchPosts(String keyword, int page, int size, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        // 최근 검색어 추가
        recentSearchService.saveRecentSearch(SearchTypeEnum.FREE_POST.name(), keyword, memberId);

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

            return processSearchResponse(response, page, size, memberId, member);

        } catch (IOException e) {
            log.warn("ES 검색 실패: keyword={}, error={}", keyword, e.getMessage());
            throw new CustomException(ErrorCode.ELASTICSEARCH_SEARCH_FAILED);
        }
    }

    // 해시태그 검색
    public PostListResponseDTO searchPostsByHashtags(List<FixedHashtag> fixedHashtags, int page, int size, Long memberId) {
        if (fixedHashtags.isEmpty()) {
            return createEmptyResponse(page, size);
        }
        
        Member member = memberService.validateAndGetMember(memberId);

        int adjustedPage = Math.max(0, page - 1);

        List<String> tags = fixedHashtags.stream()
                .map(FixedHashtag::getDisplayName) // 인덱싱과 동일한 displayName 사용
                .toList();

        log.info("검색할 해시태그들: {}", tags);

        try {

            List<Query> filters = tags.stream()
                    .map(tag -> Query.of(q -> q.match(m -> m.field("hashtags").query(tag))))
                    .toList();

            Query query = Query.of(q -> q.bool(b -> b.filter(filters)));
            
            log.info("Elasticsearch 쿼리: {}", query);

            // 1. Elasticsearch에서 검색
            SearchResponse<FreePostDocument> response = elasticsearchClient.search(s -> s
                            .index("post")
                            .from(adjustedPage * size)
                            .size(size)
                            .query(query),
                    FreePostDocument.class
            );
            
            log.info("ES 검색 결과 개수: {}", response.hits().hits().size());

            return processSearchResponse(response, page, size, memberId, member);
        } catch (IOException e) {
            log.warn("ES 해시태그 검색 실패: hashtags={}, error={}", tags, e.getMessage());
            throw new CustomException(ErrorCode.ELASTICSEARCH_SEARCH_FAILED);
        }
    }

    private PostListResponseDTO processSearchResponse(SearchResponse<FreePostDocument> response, int page, int size, Long memberId, Member member) {
        if (response.hits().hits().isEmpty()) {
            return createEmptyResponse(page, size);
        }

        List<Long> postIds = extractPostIds(response);
        Set<Long> blockedIds = memberService.getBlockedMemberIds(memberId);
        PostInteractionStatus status = postInteractionService.getAllPostInteractions(memberId, postIds);
        Set<Long> followingIds = followRepository.findFollowingMemberIds(member.getId());
        
        List<FreePost> filteredPosts = getFilteredPosts(postIds, blockedIds);
        List<FreePost> orderedPosts = maintainElasticsearchOrder(postIds, filteredPosts);
        List<PostPageResponseDTO> dtoList = convertToDTO(orderedPosts, status, memberId, followingIds);
        
        long total = response.hits().total() != null ? response.hits().total().value() : dtoList.size();
        
        return PostListResponseDTO.builder()
                .totalPost((int) total)
                .page(page)
                .size(size)
                .responseDTOS(dtoList)
                .build();
    }

    private PostListResponseDTO createEmptyResponse(int page, int size) {
        return postResponseHelper.createEmptyPostListResponse(page, size);
    }

    private List<Long> extractPostIds(SearchResponse<FreePostDocument> response) {
        return response.hits().hits().stream()
                .map(Hit::id)
                .map(Long::parseLong)
                .toList();
    }

    private List<FreePost> getFilteredPosts(List<Long> postIds, Set<Long> blockedIds) {
        List<FreePost> posts = freePostRepository.findAllById(postIds);
        return posts.stream()
                .filter(post -> !blockedIds.contains(post.getMember().getId()))
                .toList();
    }

    private List<FreePost> maintainElasticsearchOrder(List<Long> postIds, List<FreePost> posts) {
        Map<Long, FreePost> postMap = posts.stream()
                .collect(Collectors.toMap(FreePost::getId, Function.identity()));
        
        return postIds.stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<PostPageResponseDTO> convertToDTO(List<FreePost> posts, PostInteractionStatus status, Long memberId, Set<Long> followingIds) {
        return posts.stream()
                .map(post -> postResponseHelper.createPostPageResponseDTO(post, status, memberId, followingIds))
                .toList();
    }
}