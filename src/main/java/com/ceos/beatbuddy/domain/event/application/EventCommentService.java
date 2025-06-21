package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.dto.EventCommentCreateRequestDTO;
import com.ceos.beatbuddy.domain.event.dto.EventCommentResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventCommentTreeResponseDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventComment;
import com.ceos.beatbuddy.domain.event.entity.EventCommentId;
import com.ceos.beatbuddy.domain.event.exception.EventErrorCode;
import com.ceos.beatbuddy.domain.event.repository.EventCommentRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventCommentService {

    private final MemberService memberService;
    private final EventService eventService;
    private final EventCommentRepository eventCommentRepository;

    @Transactional
    public EventCommentResponseDTO createComment(Long eventId, Long memberId, EventCommentCreateRequestDTO dto, Long parentCommentId) {
        Member member = memberService.validateAndGetMember(memberId);

        Event event = eventService.validateAndGet(eventId);

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
        Member member = memberService.validateAndGetMember(memberId);

        Event event = eventService.validateAndGet(eventId);

        EventComment target = eventCommentRepository.findById(new EventCommentId(commentId, level))
                .orElseThrow(() -> new CustomException(EventErrorCode.NOT_FOUND_COMMENT));

        if (!target.getAuthor().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        if (level == 0) {
            // 원댓글이면 해당 대댓글도 전체 삭제
            eventCommentRepository.deleteAllById(commentId);
        } else {
            // 대댓글이면 해당 댓글만 삭제
            eventCommentRepository.deleteByIdAndLevel(commentId, level);
        }
    }


    @Transactional(readOnly = true)
    public List<EventCommentTreeResponseDTO> getSortedEventComments(Long memberId, Long eventId) {
        Member member = memberService.validateAndGetMember(memberId);

        Event event = eventService.validateAndGet(eventId);

        List<EventComment> all = eventCommentRepository.findAllByEvent(event);

        // commentId 기준 그룹핑
        Map<Long, List<EventComment>> grouped = all.stream()
                .collect(Collectors.groupingBy(EventComment::getId));

        // level == 0만 추출해서 최신순 정렬, 나머지는 오래된 순으로 정렬
        return grouped.values().stream()
                .map(list -> {
                    EventComment parent = list.stream()
                            .filter(c -> c.getLevel() == 0)
                            .findFirst()
                            .orElseThrow();

                    List<EventCommentResponseDTO> replies = list.stream()
                            .filter(c -> c.getLevel() > 0)
                            .sorted(Comparator.comparing(EventComment::getCreatedAt))
                            .map(EventCommentResponseDTO::toDTO)
                            .toList();

                    return EventCommentTreeResponseDTO.toDTO(parent, replies);
                })
                .sorted(Comparator.comparing((EventCommentTreeResponseDTO dto) -> dto.getCreatedAt()).reversed())
                .toList();
    }
}