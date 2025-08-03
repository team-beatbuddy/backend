package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.dto.EventCommentCreateRequestDTO;
import com.ceos.beatbuddy.domain.event.dto.EventCommentResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventCommentTreeResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventCommentUpdateDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventComment;
import com.ceos.beatbuddy.domain.event.entity.EventCommentCreatedEvent;
import com.ceos.beatbuddy.domain.event.repository.EventCommentRepository;
import com.ceos.beatbuddy.domain.follow.repository.FollowRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventCommentService {

    private final MemberService memberService;
    private final EventService eventService;
    private final EventCommentRepository eventCommentRepository;
    private final EventValidator eventValidator;
    private final FollowRepository followRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public EventCommentResponseDTO createComment(Long eventId, Long memberId, EventCommentCreateRequestDTO dto, Long parentCommentId) {
        Member member = memberService.validateAndGetMember(memberId);
        Event event = eventService.validateAndGet(eventId);

        EventComment parent = null;
        int level = 0;

        if (parentCommentId != null) {
            parent = eventCommentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COMMENT));
            level = parent.getLevel() + 1;
        }

        EventComment comment = EventComment.builder()
                .event(event)
                .author(member)
                .content(dto.getContent())
                .anonymous(dto.isAnonymous())
                .parentId(parent != null ? parent.getId() : null)
                .level(level)
                .build();

        EventComment saved = eventCommentRepository.save(comment);

        boolean isStaff = event.getHost().getId().equals(memberId);

        // ================ 알림 전송=
        eventPublisher.publishEvent(new EventCommentCreatedEvent(event, member, saved, parent, isStaff));


        return EventCommentResponseDTO.toDTO(saved, true, false, isStaff, false, member.getNickname()); // 본인 차단 불가능
    }


    @Transactional
    public void deleteComment(Long eventId, Long commentId, Long memberId) {
        memberService.validateAndGetMember(memberId);
        eventService.validateAndGet(eventId);

        EventComment target = eventCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COMMENT));

        // 작성자 검증 - 더 자세한 로깅 추가
        Long authorId = target.getAuthor().getId();
        if (!authorId.equals(memberId)) {
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
        Member member = memberService.validateAndGetMember(memberId);
        boolean isAdmin = member.isAdmin(); // admin 여부 판단

        Event event = eventService.validateAndGet(eventId);

        // 댓글 전체 조회 (author를 함께 fetch)
        List<EventComment> all = eventCommentRepository.findAllByEventWithAuthor(event);

        // 차단/팔로우 ID 조회
        Set<Long> blockedMemberIds = memberService.getBlockedMemberIds(memberId);
        Set<Long> followingIds = followRepository.findFollowingMemberIds(memberId);

        // 익명 작성자 ID 추출 및 번호 매핑
        List<Long> anonymousIds = all.stream()
                .filter(EventComment::isAnonymous)
                .map(c -> c.getAuthor().getId())
                .distinct()
                .toList();

        Map<Long, String> anonymousNameMap = new HashMap<>();
        for (int i = 0; i < anonymousIds.size(); i++) {
            anonymousNameMap.put(anonymousIds.get(i), "익명 " + (i + 1));
        }

        // 댓글 트리 구성 (부모 ID 기준 그룹핑)
        Map<Long, List<EventComment>> grouped = all.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? c.getId() : c.getParentId()));

        return grouped.values().stream()
                .map(group -> {
                    EventComment parent = group.stream()
                            .filter(c -> c.getParentId() == null)
                            .findFirst()
                            .orElseThrow();

                    List<EventCommentResponseDTO> replies = group.stream()
                            .filter(c -> c.getParentId() != null)
                            .sorted(Comparator.comparing(EventComment::getCreatedAt))
                            .map(c -> {
                                boolean isEventHost = c.getAuthor().getId().equals(event.getHost().getId());
                                String displayName;
                                if (isEventHost) {
                                    displayName = "담당자";
                                } else if (c.isAnonymous()) {
                                    displayName = anonymousNameMap.get(c.getAuthor().getId());
                                } else {
                                    displayName = c.getAuthor().getNickname();
                                }
                                
                                return EventCommentResponseDTO.toDTO(
                                        c,
                                        c.getAuthor().getId().equals(memberId),
                                        followingIds.contains(c.getAuthor().getId()),
                                        isAdmin || isEventHost,
                                        blockedMemberIds.contains(c.getAuthor().getId()),
                                        displayName
                                );
                            })
                            .toList();

                    boolean isParentEventHost = parent.getAuthor().getId().equals(event.getHost().getId());
                    String parentDisplayName;
                    if (isParentEventHost) {
                        parentDisplayName = "담당자";
                    } else if (parent.isAnonymous()) {
                        parentDisplayName = anonymousNameMap.get(parent.getAuthor().getId());
                    } else {
                        parentDisplayName = parent.getAuthor().getNickname();
                    }
                    
                    return EventCommentTreeResponseDTO.toDTO(
                            parent,
                            replies,
                            parent.getAuthor().getId().equals(memberId),
                            followingIds.contains(parent.getAuthor().getId()),
                            isAdmin || isParentEventHost,
                            blockedMemberIds.contains(parent.getAuthor().getId()),
                            parentDisplayName
                    );
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

        return EventCommentResponseDTO.toDTO(comment, comment.getAuthor().getId().equals(member.getId()), false, isStaff, false, member.getNickname());
    }

    private EventComment validateAndGetComment(Long commentId) {
        return eventCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COMMENT));
    }
}