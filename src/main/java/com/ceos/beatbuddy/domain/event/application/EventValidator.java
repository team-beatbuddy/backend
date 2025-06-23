package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.dto.EventUpdateRequestDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventAttendance;
import com.ceos.beatbuddy.domain.event.entity.EventAttendanceId;
import com.ceos.beatbuddy.domain.event.exception.EventErrorCode;
import com.ceos.beatbuddy.domain.event.repository.EventAttendanceRepository;
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
    private final EventAttendanceRepository eventAttendanceRepository;

    public void checkAccessForEvent(Long eventId, Long memberId) {
        Member host = memberService.validateAndGetMember(memberId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(EventErrorCode.NOT_FOUND_EVENT));

        if (host.isAdmin()) return;

        if (!Objects.equals(event.getHost().getId(), memberId)) {
            throw new CustomException(EventErrorCode.FORBIDDEN_EVENT_ACCESS);
        }
    }

    public void validateReceiveInfoConfig(EventUpdateRequestDTO dto) {
        if (Boolean.FALSE.equals(dto.getReceiveInfo())) {
            if (Boolean.TRUE.equals(dto.getReceiveName()) ||
                    Boolean.TRUE.equals(dto.getReceiveGender()) ||
                    Boolean.TRUE.equals(dto.getReceivePhoneNumber()) ||
                    Boolean.TRUE.equals(dto.getReceiveTotalCount()) ||
                    Boolean.TRUE.equals(dto.getReceiveSNSId())) {
                throw new CustomException(EventErrorCode.INVALID_RECEIVE_INFO_CONFIGURATION);
            }
        }
    }

    public boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }


    // 에약금을 받지만 계좌 정보가 없는 경우
    public void validateReceiveMoney(boolean receiveMoney, String depositAccount, Integer depositMoney) {
        if (receiveMoney) {
            if (depositMoney == null || depositMoney <= 0 || depositAccount == null || depositAccount.isBlank()) {
                throw new CustomException(EventErrorCode.NEED_DEPOSIT_INFO);
            }
        }
    }

    public EventAttendance validateAndGetAttendance(Long eventId, Long memberId) {
        EventAttendanceId id = new EventAttendanceId(eventId, memberId);
        return eventAttendanceRepository.findById(id).orElseThrow(
                () -> new CustomException(EventErrorCode.ATTENDANCE_NOT_FOUND));

    }
}
