package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.dto.EventAttendanceRequestDTO;
import com.ceos.beatbuddy.domain.event.dto.EventAttendanceUpdateDTO;
import com.ceos.beatbuddy.domain.event.dto.EventUpdateRequestDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventAttendance;
import com.ceos.beatbuddy.domain.event.entity.EventComment;
import com.ceos.beatbuddy.domain.event.exception.EventErrorCode;
import com.ceos.beatbuddy.domain.event.repository.EventRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class EventValidator {
    private final MemberService memberService;
    private final EventRepository eventRepository;
    
    @Value("${event.allow-past-on-create:false}")
    private boolean allowPastOnCreate;

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

    // 에약금을 받지만 계좌 정보가 없는 경우
    public void validateReceiveMoney(boolean receiveMoney, String depositAccount, Integer depositMoney) {
        if (receiveMoney) {
            if (depositMoney == null || depositMoney <= 0 || depositAccount == null || depositAccount.isBlank()) {
                throw new CustomException(EventErrorCode.NEED_DEPOSIT_INFO);
            }
        }
    }



    protected void validateAttendanceInput(EventAttendanceRequestDTO dto, Event event) {
        if (event.isReceiveInfo()) {
            if (event.isReceiveName() && isEmpty(dto.getName())) {
                throw new CustomException(EventErrorCode.MISSING_NAME);
            }
            if (event.isReceiveGender() && isEmpty(dto.getGender())) {
                throw new CustomException(EventErrorCode.MISSING_GENDER);
            }
            if (event.isReceivePhoneNumber() && isEmpty(dto.getPhoneNumber())) {
                throw new CustomException(EventErrorCode.MISSING_PHONE);
            }
            if (event.isReceiveTotalCount() && dto.getTotalNumber() == null) {
                throw new CustomException(EventErrorCode.MISSING_TOTAL_COUNT);
            }
            if (event.isReceiveSNSId() && (isEmpty(dto.getSnsType()) || isEmpty(dto.getSnsId()))) {
                throw new CustomException(EventErrorCode.MISSING_SNS_ID_OR_TYPE);
            }
            if (event.isReceiveMoney() && dto.getIsPaid() == null) {
                throw new CustomException(EventErrorCode.MISSING_PAYMENT);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void validateField(boolean isRequired, T dtoValue, T existingValue, EventErrorCode errorCode) {
        if (!isRequired) return;
        if (isEmpty(dtoValue) && isEmpty(existingValue)) {
            throw new CustomException(errorCode);
        }
    }

    private boolean isEmpty(Object value) {
        if (value == null) return true;
        if (value instanceof String str) return str.trim().isEmpty();
        return false;
    }

    protected void validateAttendanceUpdateInput(EventAttendanceUpdateDTO dto, Event event, EventAttendance existing) {
        if (!event.isReceiveInfo()) return;

        // 이름
        validateField(
                event.isReceiveName(),
                dto.getName(),
                existing.getName(),
                EventErrorCode.MISSING_NAME
        );

        // 성별
        validateField(
                event.isReceiveGender(),
                dto.getGender(),
                existing.getGender(),
                EventErrorCode.MISSING_GENDER
        );

        // 전화번호
        validateField(
                event.isReceivePhoneNumber(),
                dto.getPhoneNumber(),
                existing.getPhoneNumber(),
                EventErrorCode.MISSING_PHONE
        );

        // 동행인원 수
        validateField(
                event.isReceiveTotalCount(),
                dto.getTotalMember(),
                existing.getTotalMember(),
                EventErrorCode.MISSING_TOTAL_COUNT
        );

        // SNS ID & Type
        if (event.isReceiveSNSId()) {
            validateSnsInfo(dto, existing);
        }

        // 예약금 납부 여부
        validateField(
                event.isReceiveMoney(),
                dto.getHasPaid(),
                existing.getHasPaid(),
                EventErrorCode.MISSING_PAYMENT
        );
    }

    // SNS 정보가 비어있을 때 예외 처리
    private void validateSnsInfo(EventAttendanceUpdateDTO dto, EventAttendance existing) {
        boolean dtoMissing = isEmpty(dto.getSnsType()) || isEmpty(dto.getSnsId());
        boolean existingMissing = isEmpty(existing.getSnsType()) || isEmpty(existing.getSnsId());

        if (dtoMissing && existingMissing) {
            throw new CustomException(EventErrorCode.MISSING_SNS_ID_OR_TYPE);
        }
    }

    // 댓글을 수정하려는 게 본인이 맞는지 확인
    protected void validateCommentAuthor(Long commentAuthorId, Long memberId) {
        if (!Objects.equals(commentAuthorId, memberId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }
    }

    // 댓글이 해당 이벤트에 속하는지 확인
    protected void validateCommentBelongsToEvent(EventComment comment, Long eventId) {
        if (!comment.getEvent().getId().equals(eventId)) {
            throw new CustomException(ErrorCode.NOT_FOUND_COMMENT_IN_EVENT);
        }
    }
    
    /**
     * 이벤트 날짜 유효성 검증
     */
    public void validateEventDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new CustomException(EventErrorCode.MISSING_DATE);
        }
        
        // 시작일이 종료일보다 늦으면 안됨
        if (startDate.isAfter(endDate)) {
            throw new CustomException(EventErrorCode.INVALID_DATE_RANGE);
        }
        
        // 환경별 설정에 따른 과거 날짜 이벤트 생성 제한
        if (!allowPastOnCreate) {
            LocalDateTime now = LocalDateTime.now();
            if (startDate.isBefore(now) || endDate.isBefore(now)) {
                throw new CustomException(EventErrorCode.PAST_EVENT_NOT_ALLOWED);
            }
        }
    }
    
    /**
     * 이벤트 수정 시 날짜 유효성 검증 (기존 값과 조합하여 검증)
     */
    public void validateEventDatesForUpdate(LocalDateTime newStartDate, LocalDateTime newEndDate, 
                                          LocalDateTime existingStartDate, LocalDateTime existingEndDate) {
        // 최종 적용될 날짜 계산 (새 값이 있으면 새 값, 없으면 기존 값)
        LocalDateTime finalStartDate = newStartDate != null ? newStartDate : existingStartDate;
        LocalDateTime finalEndDate = newEndDate != null ? newEndDate : existingEndDate;
        
        // 최종 날짜가 모두 있어야 검증 가능
        if (finalStartDate == null || finalEndDate == null) {
            return;
        }
        
        // 시작일이 종료일보다 늦으면 안됨
        if (finalStartDate.isAfter(finalEndDate)) {
            throw new CustomException(EventErrorCode.INVALID_DATE_RANGE);
        }
    }
}
