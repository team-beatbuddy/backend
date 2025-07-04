package com.ceos.beatbuddy.domain.venue.application;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.ceos.beatbuddy.domain.venue.dto.VenueSearchResponseDTO;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.domain.venue.entity.VenueDocument;
import com.ceos.beatbuddy.domain.venue.repository.VenueRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import com.ceos.beatbuddy.global.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch.core.search.Hit;

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

    public void indexVenue(VenueDocument venue) throws IOException {
        elasticsearchClient.index(i -> i
                .index("venue")
                .id(String.valueOf(venue.getId()))
                .document(venue)
        );
    }

    public List<VenueSearchResponseDTO> searchByKeyword(String keyword) throws IOException {
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

    public void save(Venue venue) {
        try {
            VenueDocument doc = VenueDocument.from(venue);
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
            VenueDocument doc = VenueDocument.from(venue);

            elasticsearchClient.index(i -> i
                    .index("venue")
                    .id(doc.getId().toString())
                    .document(doc)
            );
        }
    }

}