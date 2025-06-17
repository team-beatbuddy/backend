package com.ceos.beatbuddy.domain.mypage.application;

import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventAttendance;
import com.ceos.beatbuddy.domain.event.repository.EventAttendanceRepository;
import com.ceos.beatbuddy.domain.event.repository.EventLikeRepository;
import com.ceos.beatbuddy.domain.event.repository.EventRepository;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.domain.scrapandlike.entity.EventLike;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final MemberRepository memberRepository;
    private final EventAttendanceRepository eventAttendanceRepository;
    private final EventLikeRepository eventLikeRepository;
    private final EventRepository eventRepository;

    public Map<String, List<EventResponseDTO>> getMyPageEvents(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        LocalDate today = LocalDate.now();
        Set<Event> myEvents = new HashSet<>();

        if (member.getRole().equals("BUSINESS")) {
            // 비즈니스 회원: 내가 생성한 이벤트
            myEvents.addAll(eventRepository.findAllByHost(member));
        } else {
            // 일반 회원: 좋아요 + 참석한 이벤트
            List<Event> likedEvents = eventLikeRepository.findByMember(member).stream()
                    .map(EventLike::getEvent)
                    .toList();
            List<Event> attendedEvents = eventAttendanceRepository.findByMember(member).stream()
                    .map(EventAttendance::getEvent)
                    .toList();

            myEvents.addAll(likedEvents);
            myEvents.addAll(attendedEvents);
        }

        // 상태별 분리 및 정렬
        List<EventResponseDTO> upcoming = myEvents.stream()
                .filter(e -> !e.getStartDate().isBefore(today))
                .sorted(Comparator.comparing(Event::getStartDate).reversed())
                .map(EventResponseDTO::toUpcomingListDTO)
                .toList();

        List<EventResponseDTO> past = myEvents.stream()
                .filter(e -> e.getStartDate().isBefore(today))
                .sorted(Comparator.comparing(Event::getStartDate).reversed())
                .map(EventResponseDTO::toPastListDTO)
                .toList();

        Map<String, List<EventResponseDTO>> result = new HashMap<>();
        result.put("upcoming", upcoming);
        result.put("past", past);

        return result;
    }
}
