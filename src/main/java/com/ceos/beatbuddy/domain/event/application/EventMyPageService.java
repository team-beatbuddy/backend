package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.dto.EventListResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventAttendance;
import com.ceos.beatbuddy.domain.event.entity.EventStatus;
import com.ceos.beatbuddy.domain.event.repository.EventAttendanceRepository;
import com.ceos.beatbuddy.domain.event.repository.EventLikeRepository;
import com.ceos.beatbuddy.domain.event.repository.EventQueryRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.EventLike;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventMyPageService{
    private final MemberService memberService;
    private final EventLikeRepository eventLikeRepository;
    private final EventAttendanceRepository eventAttendanceRepository;
    private final EventQueryRepository eventQueryRepository;

    // 내가 좋아요 + 참여하는 이벤트 (past)
    public EventListResponseDTO getMyPageEventsPast(Long memberId, List<String> regionStr, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);

        Set<Event.Region> regions = regionStr != null
                ? regionStr.stream().map(Event.Region::of).collect(Collectors.toSet())
                : null;

        Set<Event> myEvents = getMyPageEventList(member);
        Set<Long> likedEventIds = eventLikeRepository.findLikedEventIdsByMember(member);
        Set<Long> attendingEventIds = eventAttendanceRepository.findByMember(member).stream()
                .map(att -> att.getEvent().getId())
                .collect(Collectors.toSet());

        List<EventResponseDTO> filtered = myEvents.stream()
                .filter(e -> e.getStatus() == EventStatus.PAST)
                .filter(e -> regions == null || regions.contains(e.getRegion()))
                .sorted(Comparator.comparing(Event::getEndDate).reversed()) // 최근 종료 순
                .map(event -> EventResponseDTO.toListDTO(
                        event,
                        event.getHost().getId().equals(memberId),
                        likedEventIds.contains(event.getId()),
                        attendingEventIds.contains(event.getId())))
                .toList();

        return buildResponse("latest", page, size, filtered);
    }

    private Set<Event> getMyPageEventList(Member member) {
        Set<Event> myEvents = new HashSet<>();

        List<Event> likedEvents = eventLikeRepository.findByMember(member).stream()
                .map(EventLike::getEvent)
                .toList();
        List<Event> attendedEvents = eventAttendanceRepository.findByMember(member).stream()
                .map(EventAttendance::getEvent)
                .toList();

        myEvents.addAll(likedEvents);
        myEvents.addAll(attendedEvents);

        return myEvents;
    }


    // 내가 작성한 이벤트 (과거)
    public EventListResponseDTO getMyPageEventsByStatus(Long memberId, List<String> regionStr, EventStatus status, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);
        Set<Event.Region> regions = regionStr != null
                ? regionStr.stream().map(Event.Region::of).collect(Collectors.toSet())
                : null;
        Set<Event> myEvents = getMyPageEventList(member);
        return filterMyEventsByStatus(member, myEvents, status, regions, page, size);
    }

    public EventListResponseDTO getMyPageEventsByStatuses(
            Long memberId,
            List<String> regionStr,
            Set<EventStatus> statuses,
            int page,
            int size
    ) {
        Member member = memberService.validateAndGetMember(memberId);
        Set<Event.Region> regions = regionStr != null
                ? regionStr.stream().map(Event.Region::of).collect(Collectors.toSet())
                : null;

        Set<Event> myEvents = getMyPageEventList(member);
        return filterMyEventsByStatuses(member, myEvents, statuses, regions, page, size);
    }

    // 내가 작성한 이벤트 (upcoming + now)
    private EventListResponseDTO filterMyEventsByStatuses(
            Member member,
            Set<Event> baseEvents,
            Set<EventStatus> statuses,
            Set<Event.Region> regions,
            int page,
            int size
    ) {
        Set<Long> likedEventIds = eventLikeRepository.findLikedEventIdsByMember(member);
        Set<Long> attendingEventIds = eventAttendanceRepository.findByMember(member).stream()
                .map(att -> att.getEvent().getId())
                .collect(Collectors.toSet());

        List<EventResponseDTO> filtered = baseEvents.stream()
                .filter(e -> statuses.contains(e.getStatus()))
                .filter(e -> regions == null || regions.contains(e.getRegion()))
                .sorted(
                        Comparator.comparing(Event::getStartDate)
                                .thenComparing(Event::getEndDate)
                )
                .map(event -> EventResponseDTO.toListDTO(
                        event,
                        event.getHost().getId().equals(member.getId()),
                        likedEventIds.contains(event.getId()),
                        attendingEventIds.contains(event.getId())))
                .toList();


        return buildResponse("latest", page, size, filtered);
    }


    public List<EventResponseDTO> getMyUpcomingTop3(Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);
        Set<Event> myEvents = getMyPageEventList(member);
        Set<Long> likedEventIds = eventLikeRepository.findLikedEventIdsByMember(member);
        Set<Long> attendingEventIds = eventAttendanceRepository.findByMember(member).stream()
                .map(att -> att.getEvent().getId())
                .collect(Collectors.toSet());

        return myEvents.stream()
                .filter(e -> e.getStatus() == EventStatus.UPCOMING) // 오늘 이후 시작
                .sorted(Comparator.comparing(Event::getStartDate)) // 가까운 순
                .limit(3)
                .map(event -> EventResponseDTO.toListDTO(
                        event,
                        event.getHost().getId().equals(memberId),
                        likedEventIds.contains(event.getId()),
                        attendingEventIds.contains(event.getId())))
                .toList();
    }

    private EventListResponseDTO buildResponse(String sort, int page, int size, List<EventResponseDTO> dtoList) {
        int totalSize = dtoList.size();
        List<EventResponseDTO> paged = dtoList.stream()
                .skip((long) (page - 1) * size)
                .limit(size)
                .toList();

        return EventListResponseDTO.builder()
                .sort(sort)
                .page(page)
                .size(size)
                .totalSize(totalSize)
                .eventResponseDTOS(paged)
                .build();
    }

    private EventListResponseDTO filterMyEventsByStatus(
            Member member,
            Set<Event> baseEvents,
            EventStatus status,
            Set<Event.Region> regions,
            int page,
            int size
    ) {
        Set<Long> likedEventIds = eventLikeRepository.findLikedEventIdsByMember(member);
        Set<Long> attendingEventIds = eventAttendanceRepository.findByMember(member).stream()
                .map(att -> att.getEvent().getId())
                .collect(Collectors.toSet());

        List<EventResponseDTO> filtered = baseEvents.stream()
                .filter(e -> e.getStatus() == status)
                .filter(e -> regions == null || regions.contains(e.getRegion()))
                .sorted(getComparator(status))
                .map(event -> EventResponseDTO.toListDTO(
                        event,
                        event.getHost().getId().equals(member.getId()),
                        likedEventIds.contains(event.getId()),
                        attendingEventIds.contains(event.getId())))
                .toList();

        return buildResponse("latest", page, size, filtered);
    }

    // 내가 참여하는 이벤트만 (upcoming + now)
    public EventListResponseDTO getMyPageEventsNowAndUpcomingAttendance(Long memberId, List<String> regionStr, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);

        Set<Event.Region> regions = regionStr != null
                ? regionStr.stream().map(Event.Region::of).collect(Collectors.toSet())
                : null;

        List<Event> attendingEvents = eventAttendanceRepository.findByMember(member).stream()
                .map(EventAttendance::getEvent)
                .toList();

        Set<Long> likedEventIds = eventLikeRepository.findLikedEventIdsByMember(member);
        Set<Long> attendingEventIds = attendingEvents.stream()
                .map(Event::getId)
                .collect(Collectors.toSet());

        List<EventResponseDTO> filtered = attendingEvents.stream()
                .filter(e -> e.getStatus() == EventStatus.UPCOMING || e.getStatus() == EventStatus.NOW)
                .filter(e -> regions == null || regions.contains(e.getRegion()))
                .sorted(
                        Comparator.comparing(Event::getStartDate)
                                .thenComparing(Event::getEndDate)
                )
                .map(event -> EventResponseDTO.toListDTO(
                        event,
                        event.getHost().getId().equals(memberId),
                        likedEventIds.contains(event.getId()),
                        attendingEventIds.contains(event.getId())))
                .toList();

        return buildResponse("latest", page, size, filtered);
    }

    // 내가 좋아요한 이벤트만 (upcoming + now)
    public EventListResponseDTO getMyPageEventsNowAndUpcomingLiked(Long memberId, List<String> regionStr, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);

        Set<Event.Region> regions = regionStr != null
                ? regionStr.stream().map(Event.Region::of).collect(Collectors.toSet())
                : null;

        List<Event> likedEvents = eventLikeRepository.findByMember(member).stream()
                .map(EventLike::getEvent)
                .toList();

        Set<Long> likedEventIds = likedEvents.stream()
                .map(Event::getId)
                .collect(Collectors.toSet());
        Set<Long> attendingEventIds = eventAttendanceRepository.findByMember(member).stream()
                .map(att -> att.getEvent().getId())
                .collect(Collectors.toSet());

        List<EventResponseDTO> filtered = likedEvents.stream()
                .filter(e -> e.getStatus() == EventStatus.UPCOMING || e.getStatus() == EventStatus.NOW)
                .filter(e -> regions == null || regions.contains(e.getRegion()))
                .sorted(
                        Comparator.comparing(Event::getStartDate)
                                .thenComparing(Event::getEndDate)
                )
                .map(event -> EventResponseDTO.toListDTO(
                        event,
                        event.getHost().getId().equals(memberId),
                        likedEventIds.contains(event.getId()),
                        attendingEventIds.contains(event.getId())))
                .toList();

        return buildResponse("latest", page, size, filtered);
    }

    private Comparator<Event> getComparator(EventStatus status) {
        return switch (status) {
            case UPCOMING -> Comparator.comparing(Event::getStartDate);
            case NOW -> Comparator.comparing(Event::getStartDate).reversed();
            case PAST -> Comparator.comparing(Event::getEndDate).reversed();
        };
    }



}
