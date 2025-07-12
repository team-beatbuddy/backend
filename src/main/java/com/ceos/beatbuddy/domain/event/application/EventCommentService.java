package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.dto.EventCommentCreateRequestDTO;
import com.ceos.beatbuddy.domain.event.dto.EventCommentResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventCommentTreeResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventCommentUpdateDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventComment;
import com.ceos.beatbuddy.domain.event.repository.EventCommentRepository;
import com.ceos.beatbuddy.domain.follow.repository.FollowRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventCommentService {

    private final MemberService memberService;
    private final EventService eventService;
    private final EventCommentRepository eventCommentRepository;
    private final EventValidator eventValidator;
    private final FollowRepository followRepository;

    @Transactional
    public EventCommentResponseDTO createComment(Long eventId, Long memberId, EventCommentCreateRequestDTO dto, Long parentCommentId) {
        Member member = memberService.validateAndGetMember(memberId);
        Event event = eventService.validateAndGet(eventId);

        Long parentId = null;
        int level = 0;

        if (parentCommentId != null) {
            EventComment parent = eventCommentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COMMENT));
            parentId = parent.getId();
            level = parent.getLevel() + 1;
        }

        EventComment comment = EventComment.builder()
                .event(event)
                .author(member)
                .content(dto.getContent())
                .anonymous(dto.isAnonymous())
                .parentId(parentId)
                .level(level)
                .build();

        EventComment saved = eventCommentRepository.save(comment);

        boolean isStaff = event.getHost().getId().equals(memberId);

        return EventCommentResponseDTO.toDTO(saved, true, false, isStaff);
    }


    @Transactional
    public void deleteComment(Long eventId, Long commentId, Long memberId) {
        memberService.validateAndGetMember(memberId);
        eventService.validateAndGet(eventId);

        EventComment target = eventCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COMMENT));

        if (!target.getAuthor().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        if (target.getLevel() == 0) {
            // 원댓글이면 대댓글까지 삭제
            eventCommentRepository.deleteAllByParentIdOrId(commentId);
        } else {
            eventCommentRepository.deleteById(commentId);
        }
    }

    @Transactional(readOnly = true)
    public List<EventCommentTreeResponseDTO> getSortedEventComments(Long memberId, Long eventId) {
        memberService.validateAndGetMember(memberId);
        Event event = eventService.validateAndGet(eventId);

        List<EventComment> all = eventCommentRepository.findAllByEvent(event);
        Set<Long> followingIds = followRepository.findFollowingMemberIds(memberId);

        Map<Long, List<EventComment>> grouped = all.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? c.getId() : c.getParentId()));

        return grouped.values().stream()
                .map(group -> {
                    EventComment parent = group.stream().filter(c -> c.getParentId() == null).findFirst().orElseThrow();
                    List<EventCommentResponseDTO> replies = group.stream()
                            .filter(c -> c.getParentId() != null)
                            .sorted(Comparator.comparing(EventComment::getCreatedAt))
                            .map(c -> EventCommentResponseDTO.toDTO(c, c.getAuthor().getId().equals(memberId),
                                    followingIds.contains(c.getAuthor().getId()), c.getAuthor().equals(event.getHost())))
                            .toList();

                    return EventCommentTreeResponseDTO.toDTO(parent, replies,
                            parent.getAuthor().getId().equals(memberId),
                            followingIds.contains(parent.getAuthor().getId()), parent.getAuthor().equals(event.getHost()));
                })
                .sorted(Comparator.comparing(EventCommentTreeResponseDTO::getCreatedAt).reversed())
                .toList();
    }

    // 이벤트 댓글 수정 구현
    @Transactional
    public EventCommentResponseDTO updateComment(Long eventId, Long commentId, Long memberId, EventCommentUpdateDTO dto) {
        Member member = memberService.validateAndGetMember(memberId);
        Event event = eventService.validateAndGet(eventId);
        EventComment comment = eventCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COMMENT));

        eventValidator.validateCommentAuthor(comment.getAuthor().getId(), member.getId());
        eventValidator.validateCommentBelongsToEvent(comment, eventId);

        if (dto.getContent() != null && !dto.getContent().isBlank()) {
            comment.updateContent(dto.getContent());
        }

        if (dto.getAnonymous() != null) {
            comment.updateAnonymous(dto.getAnonymous());
        }

        boolean isStaff = comment.getAuthor().equals(event.getHost());

        return EventCommentResponseDTO.toDTO(comment, comment.getAuthor().getId().equals(member.getId()), false, isStaff);
    }

    private EventComment validateAndGetComment(Long commentId) {
        return eventCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COMMENT));
    }
}