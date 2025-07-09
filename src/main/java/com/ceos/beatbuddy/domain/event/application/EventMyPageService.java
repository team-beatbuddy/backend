package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.dto.EventListResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventAttendance;
import com.ceos.beatbuddy.domain.event.repository.EventAttendanceRepository;
import com.ceos.beatbuddy.domain.event.repository.EventLikeRepository;
import com.ceos.beatbuddy.domain.event.repository.EventQueryRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.EventLike;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        LocalDate today = LocalDate.now();

        Event.Region region = regionStr != null ? Event.of(regionStr) : null;
        Set<Event> myEvents = getMyPageEventList(member);
        Set<Long> likedEventIds = eventLikeRepository.findLikedEventIdsByMember(member);

        List<EventResponseDTO> filtered = myEvents.stream()
                .filter(e -> region == null || e.getRegion() == region)
                .sorted(Comparator.comparing(Event::getStartDate)) // 최신순: 가까운 순
                .map(event -> EventResponseDTO.toUpcomingListDTO(
                        event,
                        event.getHost().getId().equals(memberId),
                        likedEventIds.contains(event.getId())))
                .toList();

        return buildResponse("latest", page, size, filtered);
    }

    // 내가 좋아요 + 참여하는 이벤트 (now)
    public EventListResponseDTO getMyPageEventsNow(Long memberId, String regionStr, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);
        LocalDate today = LocalDate.now();
        Event.Region region = regionStr != null ? Event.of(regionStr) : null;

        Set<Event> myEvents = getMyPageEventList(member);
        Set<Long> likedEventIds = eventLikeRepository.findLikedEventIdsByMember(member);

        List<EventResponseDTO> filtered = myEvents.stream()
                .filter(e -> region == null || e.getRegion() == region)
                .sorted(Comparator.comparing(Event::getStartDate).reversed()) // 최근 시작 순
                .map(event -> EventResponseDTO.toNowListDTO(
                        event,
                        event.getHost().getId().equals(memberId),
                        likedEventIds.contains(event.getId())))
                .toList();

        return buildResponse("latest", page, size, filtered);
    }

    // 내가 좋아요 + 참여하는 이벤트 (past)
    public EventListResponseDTO getMyPageEventsPast(Long memberId, String regionStr, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);
        LocalDate today = LocalDate.now();
        Event.Region region = regionStr != null ? Event.of(regionStr) : null;

        Set<Event> myEvents = getMyPageEventList(member);
        Set<Long> likedEventIds = eventLikeRepository.findLikedEventIdsByMember(member);

        List<EventResponseDTO> filtered = myEvents.stream()
                .filter(e -> region == null || e.getRegion() == region)
                .sorted(Comparator.comparing(Event::getEndDate).reversed()) // 최근 종료 순
                .map(event -> EventResponseDTO.toPastListDTO(
                        event,
                        event.getHost().getId().equals(memberId),
                        likedEventIds.contains(event.getId())))
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
        Event.Region region = regionStr != null ? Event.of(regionStr) : null;
        Set<Long> likedEventIds = new HashSet<>(eventLikeRepository.findLikedEventIdsByMember(member));
        LocalDate today = LocalDate.now();

        List<EventResponseDTO> filtered = eventQueryRepository.findMyUpcomingEvents(member).stream()
                .filter(e -> region == null || e.getRegion() == region)
                .filter(e -> e.getStartDate().isAfter(today))
                .sorted(Comparator.comparing(Event::getStartDate)) // 가까운 순
                .map(event -> EventResponseDTO.toUpcomingListDTO(
                        event,
                        true, // 작성자 본인
                        likedEventIds.contains(event.getId())))
                .toList();

        return buildResponse("latest", page, size, filtered);
    }

    public EventListResponseDTO getMyNowEvents(Long memberId, String regionStr, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);
        Event.Region region = regionStr != null ? Event.of(regionStr) : null;
        Set<Long> likedEventIds = new HashSet<>(eventLikeRepository.findLikedEventIdsByMember(member));
        LocalDate today = LocalDate.now();

        List<EventResponseDTO> filtered = eventQueryRepository.findMyNowEvents(member).stream()
                .filter(e -> region == null || e.getRegion() == region)
                .filter(e -> !e.getStartDate().isAfter(today) && !e.getEndDate().isBefore(today))
                .sorted(Comparator.comparing(Event::getStartDate).reversed()) // 최근 시작
                .map(event -> EventResponseDTO.toNowListDTO(
                        event,
                        true,
                        likedEventIds.contains(event.getId())))
                .toList();

        return buildResponse("latest", page, size, filtered);
    }

    public EventListResponseDTO getMyPastEvents(Long memberId, String regionStr, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);
        Event.Region region = regionStr != null ? Event.of(regionStr) : null;
        Set<Long> likedEventIds = new HashSet<>(eventLikeRepository.findLikedEventIdsByMember(member));
        LocalDate today = LocalDate.now();

        List<EventResponseDTO> filtered = eventQueryRepository.findMyPastEvents(member).stream()
                .filter(e -> region == null || e.getRegion() == region)
                .filter(e -> e.getEndDate().isBefore(today))
                .sorted(Comparator.comparing(Event::getEndDate).reversed()) // 최근 종료
                .map(event -> EventResponseDTO.toPastListDTO(
                        event,
                        true,
                        likedEventIds.contains(event.getId())))
                .toList();

        return buildResponse("latest", page, size, filtered);
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
