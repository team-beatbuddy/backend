package com.ceos.beatbuddy.domain.event.application;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.ceos.beatbuddy.domain.admin.application.AdminService;
import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventSearchListResponseDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventDocument;
import com.ceos.beatbuddy.domain.event.repository.EventAttendanceRepository;
import com.ceos.beatbuddy.domain.event.repository.EventElasticRepository;
import com.ceos.beatbuddy.domain.event.repository.EventLikeRepository;
import com.ceos.beatbuddy.domain.event.repository.EventRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
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

    public EventSearchListResponseDTO searchCategorized(String keyword, Long memberId) throws IOException {
        Member member = memberService.validateAndGetMember(memberId);
        boolean isAdmin = member.isAdmin();

        Set<Long> likedEventIds = new HashSet<>(eventLikeRepository.findLikedEventIdsByMember(member));
        Set<Long> attendingEventIds = eventAttendanceRepository.findByMember(member).stream()
                .map(att -> att.getEvent().getId())
                .collect(Collectors.toSet());

        Query query = isAdmin
                ? Query.of(q -> q
                .multiMatch(m -> m
                        .fields("title", "content", "location", "notice",
                                "entranceNotice", "venueKoreanName", "venueEnglishName", "venueLocation", "region", "isFreeEntrance")
                        .query(keyword)
                        .fuzziness("AUTO")
                ))
                : Query.of(q -> q
                .bool(b -> b
                        .must(m -> m
                                .multiMatch(mm -> mm
                                        .fields("title", "content", "location", "notice",
                                                "entranceNotice", "venueKoreanName", "venueEnglishName", "venueLocation", "region", "isFreeEntrance")
                                        .query(keyword)
                                        .fuzziness("AUTO")
                                )
                        )
                        .filter(f -> f.term(t -> t.field("isVisible").value(true)))
                )
        );

        SearchResponse<EventDocument> response = elasticsearchClient.search(s -> s
                        .index("event")
                        .query(query),
                EventDocument.class
        );

        LocalDate today = LocalDate.now();

        List<Event> matchedEvents = response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .map(doc -> eventRepository.findById(doc.getId()).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        // 카테고리별 분류
        Map<String, List<EventResponseDTO>> categorized = new HashMap<>();

        for (Event event : matchedEvents) {
            String category;
            if (!event.getStartDate().isAfter(today) && !event.getEndDate().isBefore(today)) {
                category = "now";
            } else if (event.getStartDate().isAfter(today)) {
                category = "upcoming";
            } else {
                category = "past";
            }

            EventResponseDTO dto = switch (category) {
                case "now" -> EventResponseDTO.toNowListDTO(
                        event,
                        event.getHost().getId().equals(memberId),
                        likedEventIds.contains(event.getId()),
                        attendingEventIds.contains(event.getId())
                );
                case "upcoming" -> EventResponseDTO.toUpcomingListDTO(
                        event,
                        event.getHost().getId().equals(memberId),
                        likedEventIds.contains(event.getId()),
                        attendingEventIds.contains(event.getId())
                );
                case "past" -> EventResponseDTO.toPastListDTO(event,
                        event.getHost().getId().equals(memberId),
                        likedEventIds.contains(event.getId()),
                        attendingEventIds.contains(event.getId())
                );
                default -> throw new IllegalStateException("Unexpected category: " + category);
            };

            categorized.computeIfAbsent(category, k -> new ArrayList<>()).add(dto);
        }

        // 빈 카테고리 보완 (프론트에서 키가 항상 존재하도록)
        categorized.putIfAbsent("now", List.of());
        categorized.putIfAbsent("upcoming", List.of());
        categorized.putIfAbsent("past", List.of());

        return EventSearchListResponseDTO.builder()
                .eventResponseDTOS(categorized)
                .build();
    }
}
