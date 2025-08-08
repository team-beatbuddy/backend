package com.ceos.beatbuddy.domain.comment.application;

import com.ceos.beatbuddy.domain.comment.entity.Comment;
import com.ceos.beatbuddy.domain.comment.repository.CommentRepository;
import com.ceos.beatbuddy.domain.event.entity.EventComment;
import com.ceos.beatbuddy.domain.event.repository.EventCommentRepository;
import com.ceos.beatbuddy.domain.event.repository.EventRepository;
import com.ceos.beatbuddy.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AnonymousNicknameService {
    
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final EventCommentRepository eventCommentRepository;
    private final EventRepository eventRepository;
    private static final Pattern ANONYMOUS_PATTERN = Pattern.compile("^익명 (\\d+)$");
    
    /**
     * 특정 포스트에서 특정 멤버의 익명 닉네임을 가져오거나 새로 생성
     * PESSIMISTIC_WRITE 락으로 동시성 문제 완전 해결
     */
    @Transactional
    public String getOrCreateAnonymousNickname(Long postId, Long memberId, Long postWriterId, boolean isPostAnonymous) {
        // 글 작성자인 경우
        if (memberId.equals(postWriterId)) {
            if (isPostAnonymous) {
                // 익명 게시물 작성자 → 번호 없이 "익명"
                return "익명";
            }
            // 실명 게시물 작성자 → 번호 있는 익명 닉네임 생성 (fall-through)
        }
        
        // 1차: 기존 닉네임 존재 시 즉시 반환
        Optional<Comment> existing = commentRepository
                .findTopByPost_IdAndMember_IdAndIsAnonymousTrueAndAnonymousNicknameIsNotNullOrderByCreatedAtAsc(postId, memberId);
        if (existing.isPresent()) {
            return existing.get().getAnonymousNickname();
        }
        
        // DB 락: 동일 post 단위로 직렬화 (멀티 인스턴스 환경에서도 안전)
        postRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
        
        // 2차: 잠금 후 재확인 (경쟁 상황에서 선행 트랜잭션 반영 확인)
        existing = commentRepository
                .findTopByPost_IdAndMember_IdAndIsAnonymousTrueAndAnonymousNicknameIsNotNullOrderByCreatedAtAsc(postId, memberId);
        if (existing.isPresent()) {
            return existing.get().getAnonymousNickname();
        }
        
        // 번호 생성 (락 보호 하에서 안전하게 실행)
        return generateNewAnonymousNickname(postId);
    }
    
    /**
     * 해당 포스트의 다음 익명 번호 생성
     */
    private String generateNewAnonymousNickname(Long postId) {
        List<String> allAnonymousNicknames = commentRepository.findDistinctAnonymousNicknamesByPostId(postId);
        
        int maxNumber = 0;
        for (String nickname : allAnonymousNicknames) {
            Matcher matcher = ANONYMOUS_PATTERN.matcher(nickname);
            if (matcher.matches()) {
                int number = Integer.parseInt(matcher.group(1));
                maxNumber = Math.max(maxNumber, number);
            }
        }
        
        return "익명 " + (maxNumber + 1);
    }
    
    /**
     * 이벤트 댓글용 익명 닉네임 생성 또는 조회
     * 모든 댓글 작성자는 강제로 익명 처리 (호스트/관리자 제외)
     */
    @Transactional
    public String getOrCreateEventAnonymousNickname(Long eventId, Long memberId) {
        // 1차: 기존 닉네임 존재 시 즉시 반환
        Optional<EventComment> existing = eventCommentRepository
                .findTopByEvent_IdAndAuthor_IdAndAnonymousNicknameIsNotNullOrderByCreatedAtAsc(eventId, memberId);
        if (existing.isPresent()) {
            return existing.get().getAnonymousNickname();
        }
        
        // DB 락: 동일 event 단위로 직렬화
        eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
        
        // 2차: 잠금 후 재확인
        existing = eventCommentRepository
                .findTopByEvent_IdAndAuthor_IdAndAnonymousNicknameIsNotNullOrderByCreatedAtAsc(eventId, memberId);
        if (existing.isPresent()) {
            return existing.get().getAnonymousNickname();
        }
        
        // 번호 생성
        return generateNewEventAnonymousNickname(eventId);
    }
    
    /**
     * 해당 이벤트의 다음 익명 번호 생성
     */
    private String generateNewEventAnonymousNickname(Long eventId) {
        List<String> allAnonymousNicknames = eventCommentRepository.findDistinctAnonymousNicknamesByEventId(eventId);
        
        int maxNumber = 0;
        for (String nickname : allAnonymousNicknames) {
            Matcher matcher = ANONYMOUS_PATTERN.matcher(nickname);
            if (matcher.matches()) {
                int number = Integer.parseInt(matcher.group(1));
                maxNumber = Math.max(maxNumber, number);
            }
        }
        
        return "익명 " + (maxNumber + 1);
    }
}