package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventAttendance;
import com.ceos.beatbuddy.domain.event.repository.EventAttendanceRepository;
import com.ceos.beatbuddy.domain.event.repository.EventLikeRepository;
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


    // 내가 좋아요 + 참여하는 이벤트 (upcoming)
    public List<EventResponseDTO> getMyPageEventsUpcoming(Long memberId, String sort) {
        Member member = memberService.validateAndGetMember(memberId);
        LocalDate today = LocalDate.now();

        // 좋아요 + 참석한 이벤트
        Set<Event> myEvents = getMyPageEventList(member);

        // 예정(upcoming): 오늘 이후
        return myEvents.stream()
                .filter(e -> e.getStartDate().isAfter(today))
                .sorted(getComparator(sort, "upcoming"))
                .map(event -> EventResponseDTO.toUpcomingListDTO(event, event.getHost().getId().equals(memberId)))
                .toList();
    }

    // 내가 좋아요 + 참여하는 이벤트 (now)
    public List<EventResponseDTO> getMyPageEventsNow(Long memberId, String sort) {
        Member member = memberService.validateAndGetMember(memberId);
        LocalDate today = LocalDate.now();

        // 좋아요 + 참석한 이벤트
        Set<Event> myEvents = getMyPageEventList(member);

        // 진행중(now): 오늘 포함
        return myEvents.stream()
                .filter(e -> !e.getStartDate().isAfter(today) && !e.getEndDate().isBefore(today))
                .sorted(getComparator(sort, "now"))
                .map((event -> EventResponseDTO.toNowListDTO(event, event.getHost().getId().equals(member.getId())))) // 필요 시 toNowListDTO로 변경 가능
                .toList();
    }

    // 내가 좋아요 + 참여하는 이벤트 (past)
    public List<EventResponseDTO> getMyPageEventsPast(Long memberId, String sort) {
        Member member = memberService.validateAndGetMember(memberId);
        LocalDate today = LocalDate.now();

        // 좋아요 + 참석한 이벤트
        Set<Event> myEvents = getMyPageEventList(member);

        // 종료(past): 종료일이 오늘 이전
        return myEvents.stream()
                .filter(e -> e.getEndDate().isBefore(today))
                .sorted(getComparator(sort, "past"))
                .map(event -> EventResponseDTO.toPastListDTO(event, event.getHost().getId().equals(memberId)))
                .toList();
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

    private Comparator<Event> getComparator(String sort, String type) {
        return switch (type) {
            case "upcoming" -> "latest".equalsIgnoreCase(sort)
                    ? Comparator.comparing(Event::getStartDate)                // 최신: 가까운 순
                    : Comparator.comparing(Event::getStartDate).reversed();   // 과거: 먼 순
            case "now" -> "latest".equalsIgnoreCase(sort)
                    ? Comparator.comparing(Event::getStartDate).reversed()    // 최신: 최근 시작
                    : Comparator.comparing(Event::getStartDate);              // 과거: 오래 시작
            case "past" -> "latest".equalsIgnoreCase(sort)
                    ? Comparator.comparing(Event::getEndDate).reversed()      // 최신: 최근 종료
                    : Comparator.comparing(Event::getEndDate);                // 과거: 오래전 종료
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };
    }

}
