package com.ceos.beatbuddy.domain.venue.application;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
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
        // bool query 내부 구성
        Query query = Query.of(q -> q.bool(b -> {
            // 검색어 OR 조건
            if (keyword != null && !keyword.isBlank()) {
                b.should(s -> s.match(m -> m.field("koreanName").query(keyword)))
                        .should(s -> s.match(m -> m.field("englishName").query(keyword)))
                        .should(s -> s.match(m -> m.field("address").query(keyword)))
                        .should(s -> s.match(m -> m.field("genre").query(keyword)))
                        .should(s -> s.match(m -> m.field("mood").query(keyword)))
                        .should(s -> s.match(m -> m.field("region").query(keyword)))
                        .minimumShouldMatch("1");
            }

            // genreTag 필터 (정확 일치)
            if (genreTag != null && !genreTag.isBlank()) {
                b.filter(f -> f.term(t -> t.field("genre.keyword").value(genreTag)));
            }

            // regionTag 필터 (정확 일치)
            if (regionTag != null && !regionTag.isBlank()) {
                b.filter(f -> f.term(t -> t.field("region.keyword").value(regionTag)));
            }

            return b;
        }));

        // 검색 요청
        try {
            SearchResponse<VenueDocument> response = elasticsearchClient.search(s -> s
                            .index("venue")
                            .query(query),
                    VenueDocument.class);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Venue 검색 실패: keyword={}, genreTag={}, regionTag={}, error={}", keyword, genreTag, regionTag, e.getMessage(), e);
            throw new CustomException(ErrorCode.ELASTICSEARCH_SEARCH_FAILED);
        }
    }

}