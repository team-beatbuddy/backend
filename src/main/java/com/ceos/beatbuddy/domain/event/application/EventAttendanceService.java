package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.dto.EventAttendanceExportDTO;
import com.ceos.beatbuddy.domain.event.dto.EventAttendanceExportListDTO;
import com.ceos.beatbuddy.domain.event.dto.EventAttendanceRequestDTO;
import com.ceos.beatbuddy.domain.event.dto.EventAttendanceResponseDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventAttendance;
import com.ceos.beatbuddy.domain.event.entity.EventAttendanceId;
import com.ceos.beatbuddy.domain.event.exception.EventErrorCode;
import com.ceos.beatbuddy.domain.event.repository.EventAttendanceRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EventAttendanceService {
    private final MemberService memberService;
    private final EventService eventService;
    private final EventAttendanceRepository eventAttendanceRepository;
    private final EventValidator eventValidator;

    @Transactional
    public EventAttendanceResponseDTO addEventAttendance(Long memberId, EventAttendanceRequestDTO dto, Long eventId) {
        Member member = memberService.validateAndGetMember(memberId);

        Event event = eventService.validateAndGet(eventId);

        validateAttendanceInput(dto, event);

        boolean alreadyAttendance = eventAttendanceRepository.existsById(new EventAttendanceId(event.getId(), memberId));

        if (alreadyAttendance) {
            throw new CustomException(EventErrorCode.ALREADY_ATTENDANCE_EVENT);
        }

        EventAttendance eventAttendance = EventAttendanceRequestDTO.toEntity(dto, member, event);

        EventAttendance saved = eventAttendanceRepository.save(eventAttendance);

        return EventAttendanceResponseDTO.toDTO(saved);
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

    public EventAttendanceExportListDTO getAttendanceList(Long eventId, Long memberId) {
        eventService.validateAndGet(eventId);

        // admin 은 모두 조회 가능
        eventValidator.checkAccessForEvent(eventId, memberId);

        List<EventAttendance> attendances = eventAttendanceRepository.findAllByEventId(eventId);
        List<EventAttendanceExportDTO> eventAttendanceExportDTOS = attendances.stream()
                .map(EventAttendanceExportDTO::toDTO)
                .toList();

        return EventAttendanceExportListDTO.builder()
                .eventId(eventId)
                .totalMember(eventAttendanceExportDTOS.size())
                .eventAttendanceExportDTOS(eventAttendanceExportDTOS)
                .build();
    }


    public List<EventAttendanceExportDTO> getAttendanceListForExcel(Long eventId, Long memberId) {
        eventService.validateAndGet(eventId);

        // admin 은 모두 조회 가능
        eventValidator.checkAccessForEvent(eventId, memberId);

        List<EventAttendance> attendances = eventAttendanceRepository.findAllByEventId(eventId);
        return attendances.stream()
                .map(EventAttendanceExportDTO::toDTOForExcel)
                .toList();
    }

    @Transactional
    public void deleteAttendance(Long eventId, Long memberId) {
        eventService.validateAndGet(eventId);

        // 권한 체크할 필요 없이 본인 것만 검색됨.
        EventAttendance entity = validateAndGetAttendance(eventId, memberId);
        eventAttendanceRepository.delete(entity);
    }

    private EventAttendance validateAndGetAttendance(Long eventId, Long memberId) {
        EventAttendanceId id = new EventAttendanceId(eventId, memberId);
        return eventAttendanceRepository.findById(id).orElseThrow(
                () -> new CustomException(EventErrorCode.ATTENDANCE_NOT_FOUND));

    }
}
