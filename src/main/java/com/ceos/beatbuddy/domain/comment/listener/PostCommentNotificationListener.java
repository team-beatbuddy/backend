package com.ceos.beatbuddy.domain.comment.listener;

import com.ceos.beatbuddy.domain.comment.PostCommentNotifier;
import com.ceos.beatbuddy.domain.comment.entity.PostCommentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostCommentNotificationListener {

    private final PostCommentNotifier postCommentNotifier;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostCommentCreated(PostCommentCreatedEvent event) {
        log.info("🎯 댓글 이벤트 리스너 진입 - commentId: {}, writerId: {}", 
                event.comment().getId(), event.writer().getId());
        
        try {
            log.info("📮 게시글 작성자에게 알림 전송 시작");
            postCommentNotifier.notifyPostAuthor(event.comment(), event.writer().getId());

            if (event.comment().getReply() != null) {
                log.info("📮 부모 댓글 작성자에게 알림 전송 시작");
                postCommentNotifier.notifyParentCommentAuthor(event.comment(), event.writer().getId());
            }
            
            log.info("✅ 댓글 알림 전송 완료 - commentId: {}", event.comment().getId());
        } catch (Exception e) {
            log.error("💥 댓글 알림 전송 실패 - 내부적으로 재시도 큐에 저장됨", e);
        }
    }
}