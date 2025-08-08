package com.ceos.beatbuddy.domain.comment.application;

import com.ceos.beatbuddy.domain.comment.repository.CommentRepository;
import com.ceos.beatbuddy.domain.post.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AnonymousNicknameService {
    
    private final CommentRepository commentRepository;
    private static final Pattern ANONYMOUS_PATTERN = Pattern.compile("^익명 (\\d+)$");
    
    /**
     * 특정 포스트에서 특정 멤버의 익명 닉네임을 가져오거나 새로 생성
     */
    public String getOrCreateAnonymousNickname(Long postId, Long memberId, Long postWriterId, boolean isPostAnonymous) {
        // 글 작성자인 경우
        if (memberId.equals(postWriterId)) {
            if (isPostAnonymous) {
                // 익명 게시물 작성자 → 번호 없이 "익명"
                return "익명";
            } else {
                // 실명 게시물 작성자 → 번호 있는 익명 닉네임 생성
                // 일반 사용자와 동일한 로직 적용 (fall-through)
            }
        }
        
        // 기존에 부여된 익명 닉네임 확인
        List<String> existingNicknames = commentRepository.findAnonymousNicknameByPostIdAndMemberId(postId, memberId);
        if (!existingNicknames.isEmpty()) {
            return existingNicknames.get(0); // 첫 번째 결과 반환
        }
        
        // 새로운 익명 닉네임 생성
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
}