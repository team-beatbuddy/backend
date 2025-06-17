package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.dto.EventCommentCreateRequestDTO;
import com.ceos.beatbuddy.domain.event.dto.EventCommentResponseDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventComment;
import com.ceos.beatbuddy.domain.event.exception.EventErrorCode;
import com.ceos.beatbuddy.domain.event.repository.EventCommentRepository;
import com.ceos.beatbuddy.domain.event.repository.EventRepository;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventCommentService {

    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final EventCommentRepository eventCommentRepository;

    // 댓글 작성
    public EventCommentResponseDTO createEventComment(Long eventId, Long memberId, EventCommentCreateRequestDTO dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(EventErrorCode.NOT_FOUND_EVENT));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        int level = 0;
        if (dto.getParentCommentId() != null) {
            EventComment parent = eventCommentRepository.findById(dto.getParentCommentId())
                    .orElseThrow(() -> new CustomException(EventErrorCode.NOT_FOUND_COMMENT));
            level = parent.getLevel() + 1;
        }

        EventComment comment = EventCommentCreateRequestDTO.toEntity(dto, event, member, level);
        eventCommentRepository.save(comment);

        return EventCommentResponseDTO.toDTO(comment);
    }
}