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
        try {
            postCommentNotifier.notifyPostAuthor(event.comment(), event.writer().getId());

            if (event.comment().getReply() != null) {
                postCommentNotifier.notifyParentCommentAuthor(event.comment(), event.writer().getId());
            }
        } catch (Exception e) {
            log.error("💥 게시글 댓글 알림 전송 실패", e);
        }
    }
}