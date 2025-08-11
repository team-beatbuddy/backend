package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.comment.application.AnonymousNicknameService;
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
    private final ApplicationEventPublisher eventPublisher;
    private final AnonymousNicknameService anonymousNicknameService;

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

        boolean isEventHost = event.getHost().getId().equals(memberId);
        boolean isAdmin = member.isAdmin();
        boolean isStaff = isEventHost || isAdmin;
        
        // 이벤트 댓글은 강제 익명 처리 (호스트/관리자 제외)
        boolean forceAnonymous = !isStaff;
        String anonymousNickname = null;
        
        if (forceAnonymous) {
            anonymousNickname = anonymousNicknameService.getOrCreateEventAnonymousNickname(eventId, memberId);
            System.out.println("DEBUG: Generated anonymous nickname: " + anonymousNickname + " for eventId: " + eventId + ", memberId: " + memberId);
        }

        EventComment comment = EventComment.builder()
                .event(event)
                .author(member)
                .content(dto.getContent())
                .anonymous(forceAnonymous) // 강제 익명 설정
                .anonymousNickname(anonymousNickname) // 익명 닉네임 저장
                .parentId(parent != null ? parent.getId() : null)
                .level(level)
                .build();

        // 혹시 빌더에서 설정되지 않은 경우를 대비해 명시적으로 설정
        if (forceAnonymous && anonymousNickname != null) {
            comment.updateAnonymousNickname(anonymousNickname);
        }

        EventComment saved = eventCommentRepository.save(comment);
        System.out.println("DEBUG: Saved EventComment with anonymousNickname: " + saved.getAnonymousNickname() + ", id: " + saved.getId());

        // ================ 알림 전송=
        eventPublisher.publishEvent(new EventCommentCreatedEvent(event, member, saved, parent, isStaff));

        String displayName = isStaff ? "담당자" : anonymousNickname;
        return EventCommentResponseDTO.toDTO(saved, true, false, isStaff, false, displayName);
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
                                boolean isAuthorAdmin = c.getAuthor().isAdmin();
                                boolean isStaff = isEventHost || isAuthorAdmin;
                                
                                String displayName = getDisplayName(c, isStaff);
                                
                                return EventCommentResponseDTO.toDTO(
                                        c,
                                        c.getAuthor().getId().equals(memberId),
                                        followingIds.contains(c.getAuthor().getId()),
                                        isAdmin || isStaff,
                                        blockedMemberIds.contains(c.getAuthor().getId()),
                                        displayName
                                );
                            })
                            .toList();

                    boolean isParentEventHost = parent.getAuthor().getId().equals(event.getHost().getId());
                    boolean isParentAdmin = parent.getAuthor().isAdmin();
                    boolean isParentStaff = isParentEventHost || isParentAdmin;
                    
                    String parentDisplayName = getDisplayName(parent, isParentStaff);
                    
                    return EventCommentTreeResponseDTO.toDTO(
                            parent,
                            replies,
                            parent.getAuthor().getId().equals(memberId),
                            followingIds.contains(parent.getAuthor().getId()),
                            isAdmin || isParentStaff,
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

        boolean isEventHost = event.getHost().getId().equals(memberId);
        boolean isAdmin = member.isAdmin();
        boolean isStaff = isEventHost || isAdmin;
        
        // 익명 설정은 무시 - 강제 익명 처리
        if (!isStaff) {
            comment.updateAnonymous(true); // 강제 익명 유지
            if (comment.getAnonymousNickname() == null || comment.getAnonymousNickname().isBlank()) {
                String nickname = anonymousNicknameService.getOrCreateEventAnonymousNickname(eventId, memberId);
                comment.updateAnonymousNickname(nickname);
            }
        }

        String displayName = getDisplayName(comment, isStaff);
        return EventCommentResponseDTO.toDTO(comment, comment.getAuthor().getId().equals(member.getId()), false, isStaff, false, displayName);
    }

    private EventComment validateAndGetComment(Long commentId) {
        return eventCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COMMENT));
    }
    
    /**
     * 이벤트 댓글 표시명 결정
     * - 담당자/관리자: "담당자"
     * - 일반 사용자: 저장된 anonymousNickname (강제 익명)
     */
    private String getDisplayName(EventComment comment, boolean isStaff) {
        if (isStaff) {
            return "담당자";
        }
        // 강제 익명이므로 저장된 anonymousNickname 사용
        return comment.getAnonymousNickname();
    }
}