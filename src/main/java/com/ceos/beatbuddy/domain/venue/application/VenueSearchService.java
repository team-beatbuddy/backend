package com.ceos.beatbuddy.domain.venue.application;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.ceos.beatbuddy.domain.venue.dto.VenueSearchResponseDTO;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.domain.venue.entity.VenueDocument;
import com.ceos.beatbuddy.domain.venue.entity.VenueGenre;
import com.ceos.beatbuddy.domain.venue.entity.VenueMood;
import com.ceos.beatbuddy.domain.venue.repository.VenueGenreRepository;
import com.ceos.beatbuddy.domain.venue.repository.VenueMoodRepository;
import com.ceos.beatbuddy.domain.venue.repository.VenueRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import com.ceos.beatbuddy.global.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class VenueSearchService {
    private final VenueRepository venueRepository;

    private final ElasticsearchClient elasticsearchClient;
    private final VenueGenreRepository venueGenreRepository;
    private final VenueMoodRepository venueMoodRepository;

    public void indexVenue(VenueDocument venue) throws IOException {
        elasticsearchClient.index(i -> i
                .index("venue")
                .id(String.valueOf(venue.getId()))
                .document(venue)
        );
    }

    public List<VenueSearchResponseDTO> searchByKeywordForAddress(String keyword) throws IOException {
        SearchResponse<VenueDocument> response = elasticsearchClient.search(s -> s
                        .index("venue")
                        .query(q -> q
                                .multiMatch(m -> m
                                        .fields("englishName", "koreanName", "address")
                                        .query(keyword)
                                        .fuzziness("AUTO")
                                )
                        ),
                VenueDocument.class
        );

        if (response.hits().hits().isEmpty()) {
            throw new CustomException(SuccessCode.SUCCESS_BUT_EMPTY_LIST);
        }

        return response.hits().hits().stream()
                .map(Hit::source).filter(Objects::nonNull)
                .map(VenueSearchResponseDTO::toDTO)
                .collect(Collectors.toList());
    }

    public void save(Venue venue, VenueGenre venueGenre, VenueMood venueMood) {
        try {
            VenueDocument doc = VenueDocument.from(venue, venueGenre, venueMood);
            elasticsearchClient.index(i -> i
                    .index("venue")
                    .id(doc.getId().toString())
                    .document(doc)
            );
        } catch (IOException e) {
            log.error("Venue 인덱싱 실패: venueId={}, error={}", venue.getId(), e.getMessage(), e);
            throw new CustomException(ErrorCode.ELASTICSEARCH_INDEXING_FAILED);
        }
    }

    public void delete(Long venueId) {
        try {
            elasticsearchClient.delete(d -> d
                    .index("venue")
                    .id(venueId.toString())
            );
        } catch (IOException e) {
            log.error("Venue 삭제 실패: venueId={}, error={}", venueId, e.getMessage(), e);
            throw new CustomException(ErrorCode.ELASTICSEARCH_DELETION_FAILED);
        }
    }


    public void syncVenueFromDBToES() throws IOException {
        List<Venue> venues = venueRepository.findAll(); // SQL DB에서 가져옴
        
        // 배치로 모든 장르/무드 정보 조회
        List<Long> venueIds = venues.stream().map(Venue::getId).toList();
        Map<Long, VenueGenre> genreMap = venueGenreRepository.findByVenueIdIn(venueIds)
                .stream().collect(Collectors.toMap(vg -> vg.getVenue().getId(), Function.identity()));
        Map<Long, VenueMood> moodMap = venueMoodRepository.findByVenueIdIn(venueIds)
                .stream().collect(Collectors.toMap(vm -> vm.getVenue().getId(), Function.identity()));

        for (Venue venue : venues) {
            VenueDocument doc = VenueDocument.from(venue,
                    genreMap.get(venue.getId()),
                    moodMap.get(venue.getId())
            );

            elasticsearchClient.index(i -> i
                    .index("venue")
                    .id(doc.getId().toString())
                    .document(doc)
            );
        }
    }


    public List<VenueDocument> searchMapDropDown(String keyword, String regionTag, String genreTag){
        log.info("검색 파라미터 - keyword: {}, genreTag: {}, regionTag: {}", keyword, genreTag, regionTag);

        try {
            List<Query> innerMostBoolMustClauses = new ArrayList<>();

            // 1. 키워드 검색 조건: keyword가 있으면 multi_match, 없으면 matchAll을 must에 추가
            if (keyword != null && !keyword.isBlank()) {
                innerMostBoolMustClauses.add(Query.of(q -> q.multiMatch(mm -> mm
                        .query(keyword)
                        .fields("koreanName", "englishName", "address", "genre", "mood", "region")
                        .fuzziness("AUTO")
                )));
            } else {
                innerMostBoolMustClauses.add(Query.of(q -> q.matchAll(ma -> ma)));
            }

            // genreTag 필터링 (genre.keyword, 대문자 변환)
            if (genreTag != null && !genreTag.isBlank()) {
                // TermQuery 대신 Query 타입으로 받습니다.
                Query genreTermQuery = QueryBuilders.term(t -> t.field("genre.keyword").value(genreTag.toUpperCase()));
                innerMostBoolMustClauses.add(Query.of(q -> q.bool(b -> b
                        .should(genreTermQuery) // 직접 Query 객체를 전달
                        .minimumShouldMatch("1")
                )));
            }

            // regionTag 필터링 (region.keyword, 한글이므로 변환 없음)
            if (regionTag != null && !regionTag.isBlank()) {
                // TermQuery 대신 Query 타입으로 받습니다.
                Query regionTermQuery = QueryBuilders.term(t -> t.field("region.keyword").value(regionTag));
                innerMostBoolMustClauses.add(Query.of(q -> q.bool(b -> b
                        .should(regionTermQuery) // 직접 Query 객체를 전달
                        .minimumShouldMatch("1")
                )));
            }
            // 최종 쿼리 생성: 복잡한 중첩 구조
            Query finalQuery = Query.of(q -> q.bool(b -> b
                    .must(Query.of(mq -> mq.bool(innerMostBoolBuilder -> innerMostBoolBuilder
                            .must(innerMostBoolMustClauses)
                    )))
            ));

            // --- Debugging ---
            // ElasticsearchClient가 생성하는 최종 JSON 쿼리를 로그로 출력
            // 이 로그를 Kibana Dev Tools에서 성공했던 JSON과 비교하는 것이 가장 중요합니다.
            log.info("Generated Elasticsearch Query JSON: {}", finalQuery.toString());
            // --- End Debugging ---


            // 실제 검색 요청
            SearchResponse<VenueDocument> response = elasticsearchClient.search(s -> s
                            .index("venue")
                            .query(finalQuery) // 새로 구성한 finalQuery 사용
                    // runtime_mappings, script_fields는 필요하다면 추가
                    ,
                    VenueDocument.class
            );

            List<Hit<VenueDocument>> hits = response.hits().hits();

            // 결과 로그
            log.info("Elasticsearch 검색 결과 개수: {}", hits.size());
            log.info("전체 매치 수: {}, 최대 스코어: {}", Objects.requireNonNull(response.hits().total()).value(), response.hits().maxScore());

            if (!hits.isEmpty()) {
                log.info("첫 번째 검색 결과: {}", hits.get(0).source());
                for (int i = 0; i < Math.min(hits.size(), 5); i++) {
                    VenueDocument doc = hits.get(i).source();
                    if (doc != null) {
                        log.info("결과 {}: 이름={}, 지역={}, 장르={}", i + 1, doc.getKoreanName(), doc.getRegion(), doc.getGenre());
                    }
                }
            } else {
                log.warn("검색 결과가 없습니다. 필터 조건을 확인해주세요.");
            }

            return hits.stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Venue 검색 실패: keyword={}, genreTag={}, regionTag={}, error={}", keyword, genreTag, regionTag, e.getMessage(), e);
            throw new CustomException(ErrorCode.ELASTICSEARCH_SEARCH_FAILED);
        }
    }
}