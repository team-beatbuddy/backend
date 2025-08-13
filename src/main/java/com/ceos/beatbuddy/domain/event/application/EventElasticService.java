package com.ceos.beatbuddy.domain.event.application;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.ceos.beatbuddy.domain.event.dto.EventListResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventDocument;
import com.ceos.beatbuddy.domain.event.repository.EventAttendanceRepository;
import com.ceos.beatbuddy.domain.event.repository.EventLikeRepository;
import com.ceos.beatbuddy.domain.event.repository.EventRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.recent_search.application.RecentSearchService;
import com.ceos.beatbuddy.domain.recent_search.entity.SearchTypeEnum;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventElasticService {
    private final EventRepository eventRepository;
    private final EventLikeRepository eventLikeRepository;
    private final EventAttendanceRepository eventAttendanceRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final MemberService memberService;
    private final RecentSearchService recentSearchService;

    public void syncAll() throws IOException {
        List<Event> events = eventRepository.findAll();

        for (Event event : events) {
            EventDocument doc = EventDocument.from(event);
            elasticsearchClient.index(i -> i
                    .index("event")
                    .id(doc.getId().toString())
                    .document(doc)
            );
        }
    }
    // 저장 (신규 생성)
    public void save(Event event) {
        try {
            EventDocument doc = EventDocument.from(event);
            elasticsearchClient.index(i -> i
                    .index("event")
                    .id(doc.getId().toString())
                    .document(doc)
            );
        } catch (IOException e) {
            log.error("Event 인덱싱 실패: id={}, error={}", event.getId(), e.getMessage(), e);
            throw new CustomException(ErrorCode.ELASTICSEARCH_INDEXING_FAILED);
        }
    }

    // 삭제
    public void delete(Long eventId) {
        try {
            elasticsearchClient.delete(d -> d
                    .index("event")
                    .id(eventId.toString())
            );
        } catch (IOException e) {
            log.error("Event 삭제 실패: id={}, error={}", eventId, e.getMessage(), e);
            throw new CustomException(ErrorCode.ELASTICSEARCH_DELETION_FAILED);
        }
    }

    public EventListResponseDTO search(String keyword, Long memberId, int page, int size,
                                       LocalDateTime startDate, LocalDateTime endDate) {
        Member member = memberService.validateAndGetMember(memberId);
        if (page < 1) throw new CustomException(ErrorCode.PAGE_OUT_OF_BOUNDS);

        recentSearchService.saveRecentSearch(SearchTypeEnum.EVENT.name(), keyword, memberId);

        boolean isAdmin = member.isAdmin();
        Set<Long> likedEventIds = new HashSet<>(eventLikeRepository.findLikedEventIdsByMember(member));
        Set<Long> attendingEventIds = eventAttendanceRepository.findByMember(member)
                .stream().map(att -> att.getEvent().getId()).collect(Collectors.toSet());

        // ---- Query build (옵션 조건만 추가) ----
        var bool = new co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder();

        // 키워드 있을 때만
        if (keyword != null && !keyword.isBlank()) {
            bool.must(m -> m.multiMatch(mm -> mm
                    .fields(
                            "title^2.0",
                            "region^2.0",
                            "location^2.0",
                            "venueLocation^2.0",
                            "venueKoreanName^2.0",
                            "venueEnglishName^2.0",
                            "content",
                            "notice",
                            "entranceNotice",
                            "isFreeEntrance"
                    )
                    .query(keyword)
            ));
        }

        // 공개 여부 (관리자만 제외)
        if (!isAdmin) {
            bool.filter(f -> f.term(t -> t.field("isVisible").value(true)));
        }

        // 기간 필터 (있을 때만)
        if (endDate != null) {
            bool.filter(f -> f.range(r -> r.field("startDate").lte(co.elastic.clients.json.JsonData.of(endDate))));
        }
        if (startDate != null) {
            bool.filter(f -> f.range(r -> r.field("endDate").gte(co.elastic.clients.json.JsonData.of(startDate))));
        }

        Query query = Query.of(q -> q.bool(bool.build()));

        int from = (Math.max(1, page) - 1) * size;

        SearchResponse<EventDocument> response;
        try {
            response = elasticsearchClient.search(s -> s
                            .index("event")
                            .from(from)
                            .size(size)
                            .query(query),
                    EventDocument.class);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.ELASTICSEARCH_SEARCH_FAILED);
        }

        List<Long> ids = response.hits().hits().stream()
                .map(Hit::source).filter(Objects::nonNull).map(EventDocument::getId).toList();

        Map<Long, Event> eventMap = eventRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));

        List<EventResponseDTO> items = ids.stream()
                .map(eventMap::get).filter(Objects::nonNull)
                .map(ev -> EventResponseDTO.toListDTO(
                        ev, ev.getHost().getId().equals(memberId),
                        likedEventIds.contains(ev.getId()),
                        attendingEventIds.contains(ev.getId())))
                .toList();

        int total = (int) Objects.requireNonNull(response.hits().total()).value();
        return EventListResponseDTO.builder()
                .sort("search")
                .page(page).size(size).totalSize(total)
                .eventResponseDTOS(items)
                .build();
    }

}
