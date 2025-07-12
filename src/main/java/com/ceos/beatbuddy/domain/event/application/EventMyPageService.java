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


    // 내가 좋아요 + 참여하는 이벤트 (upcoming)
    public EventListResponseDTO getMyPageEventsUpcoming(Long memberId, String regionStr, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);

        Event.Region region = regionStr != null ? Event.Region.of(regionStr) : null;
        Set<Event> myEvents = getMyPageEventList(member);
        Set<Long> likedEventIds = eventLikeRepository.findLikedEventIdsByMember(member);
        Set<Long> attendingEventIds = eventAttendanceRepository.findByMember(member).stream()
                .map(att -> att.getEvent().getId())
                .collect(Collectors.toSet());

        List<EventResponseDTO> filtered = myEvents.stream()
                .filter(e -> e.getStatus() == EventStatus.UPCOMING)
                .filter(e -> region == null || e.getRegion() == region)
                .sorted(Comparator.comparing(Event::getStartDate))
                .map(event -> EventResponseDTO.toListDTO(
                        event,
                        event.getHost().getId().equals(memberId),
                        likedEventIds.contains(event.getId()),
                        attendingEventIds.contains(event.getId())))
                .toList();

        return buildResponse("latest", page, size, filtered);
    }

    // 내가 좋아요 + 참여하는 이벤트 (now)
    public EventListResponseDTO getMyPageEventsNow(Long memberId, String regionStr, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);
        Event.Region region = regionStr != null ? Event.Region.of(regionStr) : null;

        Set<Event> myEvents = getMyPageEventList(member);
        Set<Long> likedEventIds = eventLikeRepository.findLikedEventIdsByMember(member);
        Set<Long> attendingEventIds = eventAttendanceRepository.findByMember(member).stream()
                .map(att -> att.getEvent().getId())
                .collect(Collectors.toSet());

        List<EventResponseDTO> filtered = myEvents.stream()
                .filter(e -> e.getStatus() == EventStatus.NOW)
                .filter(e -> region == null || e.getRegion() == region)
                .sorted(Comparator.comparing(Event::getStartDate).reversed()) // 최근 시작 순
                .map(event -> EventResponseDTO.toListDTO(
                        event,
                        event.getHost().getId().equals(memberId),
                        likedEventIds.contains(event.getId()),
                        attendingEventIds.contains(event.getId())))
                .toList();

        return buildResponse("latest", page, size, filtered);
    }

    // 내가 좋아요 + 참여하는 이벤트 (past)
    public EventListResponseDTO getMyPageEventsPast(Long memberId, String regionStr, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);
        Event.Region region = regionStr != null ? Event.Region.of(regionStr) : null;

        Set<Event> myEvents = getMyPageEventList(member);
        Set<Long> likedEventIds = eventLikeRepository.findLikedEventIdsByMember(member);
        Set<Long> attendingEventIds = eventAttendanceRepository.findByMember(member).stream()
                .map(att -> att.getEvent().getId())
                .collect(Collectors.toSet());

        List<EventResponseDTO> filtered = myEvents.stream()
                .filter(e -> e.getStatus() == EventStatus.PAST)
                .filter(e -> region == null || e.getRegion() == region)
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


    // 내가 작성한 이벤트
    public EventListResponseDTO getMyUpcomingEvents(Long memberId, String regionStr, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);
        Event.Region region = regionStr != null ? Event.Region.of(regionStr) : null;
        Set<Long> likedEventIds = new HashSet<>(eventLikeRepository.findLikedEventIdsByMember(member));
        Set<Long> attendingEventIds = eventAttendanceRepository.findByMember(member).stream()
                .map(att -> att.getEvent().getId())
                .collect(Collectors.toSet());

        List<EventResponseDTO> filtered = eventQueryRepository.findMyUpcomingEvents(member).stream()
                .filter(e -> region == null || e.getRegion() == region)
                .filter(e -> e.getStatus() == EventStatus.UPCOMING)
                .sorted(Comparator.comparing(Event::getStartDate)) // 가까운 순
                .map(event -> EventResponseDTO.toListDTO(
                        event,
                        true, // 작성자 본인
                        likedEventIds.contains(event.getId()),
                        attendingEventIds.contains(event.getId())))
                .toList();

        return buildResponse("latest", page, size, filtered);
    }

    public EventListResponseDTO getMyNowEvents(Long memberId, String regionStr, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);
        Event.Region region = regionStr != null ? Event.Region.of(regionStr) : null;
        Set<Long> likedEventIds = new HashSet<>(eventLikeRepository.findLikedEventIdsByMember(member));
        Set<Long> attendingEventIds = eventAttendanceRepository.findByMember(member).stream()
                .map(att -> att.getEvent().getId())
                .collect(Collectors.toSet());

        List<EventResponseDTO> filtered = eventQueryRepository.findMyNowEvents(member).stream()
                .filter(e -> region == null || e.getRegion() == region)
                .filter(e -> e.getStatus() == EventStatus.NOW)
                .sorted(Comparator.comparing(Event::getStartDate).reversed()) // 최근 시작
                .map(event -> EventResponseDTO.toListDTO(
                        event,
                        true,
                        likedEventIds.contains(event.getId()),
                        attendingEventIds.contains(event.getId())))
                .toList();

        return buildResponse("latest", page, size, filtered);
    }

    public EventListResponseDTO getMyPastEvents(Long memberId, String regionStr, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);
        Event.Region region = regionStr != null ? Event.Region.of(regionStr) : null;
        Set<Long> likedEventIds = new HashSet<>(eventLikeRepository.findLikedEventIdsByMember(member));
        Set<Long> attendingEventIds = eventAttendanceRepository.findByMember(member).stream()
                .map(att -> att.getEvent().getId())
                .collect(Collectors.toSet());

        List<EventResponseDTO> filtered = eventQueryRepository.findMyPastEvents(member).stream()
                .filter(e -> region == null || e.getRegion() == region)
                .filter(e -> e.getStatus() == EventStatus.PAST)
                .sorted(Comparator.comparing(Event::getEndDate).reversed()) // 최근 종료
                .map(event -> EventResponseDTO.toListDTO(
                        event,
                        true,
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


}
