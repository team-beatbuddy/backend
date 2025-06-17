package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.dto.EventCommentCreateRequestDTO;
import com.ceos.beatbuddy.domain.event.dto.EventCommentResponseDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventComment;
import com.ceos.beatbuddy.domain.event.entity.EventCommentId;
import com.ceos.beatbuddy.domain.event.exception.EventErrorCode;
import com.ceos.beatbuddy.domain.event.repository.EventCommentRepository;
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
public class EventCommentService {

    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final EventCommentRepository eventCommentRepository;

    @Transactional
    public EventCommentResponseDTO createComment(Long eventId, Long memberId, EventCommentCreateRequestDTO dto, Long parentCommentId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(EventErrorCode.NOT_FOUND_EVENT));

        Long commentId;
        int level = 0;

        if (parentCommentId == null) {
            // 원댓글이면 새로운 그룹 ID 생성
            commentId = eventCommentRepository.getNextCommentGroupId(); // ★ 별도로 시퀀스 또는 max(id)+1 로직 필요
        } else {
            // 대댓글이면 부모 댓글 확인
            EventComment parent = eventCommentRepository.findTopByIdOrderByLevelDesc(parentCommentId)
                    .orElseThrow(() -> new CustomException(EventErrorCode.NOT_FOUND_COMMENT));
            commentId = parent.getId(); // 부모 id 따라감
            level = parent.getLevel() + 1;
        }

        EventComment comment = EventComment.builder()
                .id(commentId)
                .level(level)
                .event(event)
                .author(member)
                .content(dto.getContent())
                .anonymous(dto.isAnonymous())
                .build();

        EventComment saved = eventCommentRepository.save(comment);
        return EventCommentResponseDTO.toDTO(saved);
    }


    @Transactional
    public void deleteComment(Long eventId, Long commentId, Integer level, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(EventErrorCode.NOT_FOUND_EVENT));

        EventComment target = eventCommentRepository.findById(new EventCommentId(commentId, level))
                .orElseThrow(() -> new CustomException(EventErrorCode.NOT_FOUND_COMMENT));

        if (!target.getAuthor().getId().equals(memberId)) {
            throw new CustomException(EventErrorCode.NOT_COMMENT_OWNER);
        }

        if (level == 0) {
            // 원댓글이면 해당 대댓글도 전체 삭제
            eventCommentRepository.deleteAllById(commentId);
        } else {
            // 대댓글이면 해당 댓글만 삭제
            eventCommentRepository.deleteByIdAndLevel(commentId, level);
        }
    }
}