package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.exception.EventErrorCode;
import com.ceos.beatbuddy.domain.event.repository.EventRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class EventValidator {
    private final MemberService memberService;
    private final EventRepository eventRepository;

    public void checkAccessForEvent(Long eventId, Long memberId) {
        Member host = memberService.validateAndGetMember(memberId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(EventErrorCode.NOT_FOUND_EVENT));

        if (host.isAdmin()) return;

        if (!Objects.equals(event.getHost().getId(), memberId)) {
            throw new CustomException(EventErrorCode.FORBIDDEN_EVENT_ACCESS);
        }
    }
}
