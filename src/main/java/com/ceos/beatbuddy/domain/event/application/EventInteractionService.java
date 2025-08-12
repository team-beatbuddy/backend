package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.repository.EventLikeRepository;
import com.ceos.beatbuddy.domain.event.repository.EventRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.EventLike;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventInteractionService {
    private final MemberService memberService;
    private final EventRepository eventRepository;
    private final EventLikeRepository eventLikeRepository;
    private final EventService eventService;

    @Transactional
    public void likeEvent(Long eventId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        Event event = eventService.validateAndGet(eventId);

        if (eventLikeRepository.existsByMemberIdAndEventId(memberId, eventId)) {
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        }

        EventLike eventLike = EventLike.toEntity(member, event);
        eventLikeRepository.save(eventLike);
        eventRepository.increaseLike(eventId);
    }

    @Transactional
    public void deleteLikeEvent(Long eventId, Long memberId) {
        // 멤버 조회
        memberService.validateAndGetMember(memberId);

        // 이벤트 조회
        eventService.validateAndGet(eventId);


        // 좋아요 여부 확인
        if (!eventLikeRepository.existsByMemberIdAndEventId(memberId, eventId)) {
            throw new CustomException(ErrorCode.NOT_FOUND_LIKE);
        }

        eventLikeRepository.deleteByMemberIdAndEventId(memberId, eventId);
        eventRepository.decreaseLike(eventId);
    }
}
