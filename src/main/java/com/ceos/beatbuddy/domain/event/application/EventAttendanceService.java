package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.dto.EventAttendanceRequestDTO;
import com.ceos.beatbuddy.domain.event.dto.EventAttendanceResponseDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventAttendance;
import com.ceos.beatbuddy.domain.event.entity.EventAttendanceId;
import com.ceos.beatbuddy.domain.event.exception.EventErrorCode;
import com.ceos.beatbuddy.domain.event.repository.EventAttendanceRepository;
import com.ceos.beatbuddy.domain.event.repository.EventRepository;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventAttendanceService {
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final EventAttendanceRepository eventAttendanceRepository;

    @Transactional
    public EventAttendanceResponseDTO addEventAttendance(Long memberId, EventAttendanceRequestDTO dto, Long eventId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new CustomException(EventErrorCode.NOT_FOUND_EVENT)
        );

        validateAttendanceInput(dto, event);

        boolean alreadyAttendance = eventAttendanceRepository.existsById(new EventAttendanceId(event.getId(), memberId));
        if (alreadyAttendance) {
            throw new CustomException(EventErrorCode.ALREADY_ATTENDANCE_EVENT);
        }

        EventAttendance eventAttendance = EventAttendanceRequestDTO.toEntity(dto, member, event);

        eventAttendanceRepository.save(eventAttendance);

        return EventAttendanceResponseDTO.toDTO(eventAttendance);
    }

    private void validateAttendanceInput(EventAttendanceRequestDTO dto, Event event) {
        if (event.isReceiveInfo()) {
            if (event.isReceiveName() && isBlank(dto.getName())) {
                throw new CustomException(EventErrorCode.MISSING_NAME);
            }
            if (event.isReceiveGender() && isBlank(dto.getGender())) {
                throw new CustomException(EventErrorCode.MISSING_GENDER);
            }
            if (event.isReceivePhoneNumber() && isBlank(dto.getPhoneNumber())) {
                throw new CustomException(EventErrorCode.MISSING_PHONE);
            }
            if (event.isReceiveTotalCount() && dto.getTotalNumber() == null) {
                throw new CustomException(EventErrorCode.MISSING_TOTAL_COUNT);
            }
            if (event.isReceiveSNSId() && (isBlank(dto.getSnsType()) || isBlank(dto.getSnsId()))) {
                throw new CustomException(EventErrorCode.MISSING_SNS_ID_OR_TYPE);
            }
            if (event.isReceiveMoney() && dto.getIsPaid() == null) {
                throw new CustomException(EventErrorCode.MISSING_PAYMENT);
            }
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
