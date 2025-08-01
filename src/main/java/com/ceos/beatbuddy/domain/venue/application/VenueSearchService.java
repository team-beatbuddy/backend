package com.ceos.beatbuddy.domain.venue.application;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
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
import java.util.List;
import java.util.Objects;
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

        for (Venue venue : venues) {
            VenueDocument doc = VenueDocument.from(venue,
                    venueGenreRepository.findByVenue(venue).orElse(null),
                    venueMoodRepository.findByVenue(venue).orElse(null)
            );

            elasticsearchClient.index(i -> i
                    .index("venue")
                    .id(doc.getId().toString())
                    .document(doc)
            );
        }
    }


    public List<VenueDocument> searchMapDropDown(String keyword, String genreTag, String regionTag) {
        log.info("검색 파라미터 - keyword: {}, genreTag: {}, regionTag: {}", keyword, genreTag, regionTag);
        
        try {
            // 먼저 필터 없이 키워드만으로 검색해보기
            SearchResponse<VenueDocument> testResponse = elasticsearchClient.search(s -> s
                            .index("venue")
                            .query(q -> q.multiMatch(mm -> mm
                                    .query(keyword)
                                    .fields("koreanName", "englishName", "address", "genre", "mood", "region")
                                    .fuzziness("AUTO")
                            ))
                    , VenueDocument.class
            );
            log.info("필터 없는 검색 결과: {} 개", testResponse.hits().total().value());
            
            // 이제 실제 검색
            SearchResponse<VenueDocument> response = elasticsearchClient.search(s -> s
                            .index("venue")
                            .query(q -> q.bool(b -> {
                                // 키워드 검색 (필수)
                                if (keyword != null && !keyword.isBlank()) {
                                    b.must(m -> m.multiMatch(mm -> mm
                                            .query(keyword)
                                            .fields("koreanName", "englishName", "address", "genre", "mood", "region")
                                            .fuzziness("AUTO")
                                    ));
                                } else {
                                    // 키워드가 없으면 모든 문서 매치
                                    b.must(m -> m.matchAll(ma -> ma));
                                }
                                
                                // 장르 필터
                                if (genreTag != null && !genreTag.isBlank()) {
                                    log.info("장르 필터 적용: {}", genreTag);
                                    b.filter(f -> f.term(t -> t.field("genre").value(genreTag)));
                                }
                                
                                // 지역 필터
                                if (regionTag != null && !regionTag.isBlank()) {
                                    log.info("지역 필터 적용: {}", regionTag);
                                    b.filter(f -> f.term(t -> t.field("region").value(regionTag)));
                                }
                                
                                return b;
                            }))
                    , VenueDocument.class
            );

            List<Hit<VenueDocument>> hits = response.hits().hits();

            // 결과 로그 출력
            log.info("Elasticsearch 검색 결과 개수: {}", hits.size());
            log.info("전체 매치 수: {}, 최대 스코어: {}", response.hits().total().value(), response.hits().maxScore());
            
            if (!hits.isEmpty()) {
                log.info("첫 번째 검색 결과: {}", hits.get(0).source());
                // 모든 결과의 region 필드 확인
                for (int i = 0; i < Math.min(hits.size(), 5); i++) {
                    VenueDocument doc = hits.get(i).source();
                    if (doc != null) {
                        log.info("결과 {}: 이름={}, 지역={}, 장르={}", i+1, doc.getKoreanName(), doc.getRegion(), doc.getGenre());
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