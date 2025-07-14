package com.ceos.beatbuddy.domain.event.listener;

import com.ceos.beatbuddy.domain.event.EventCommentNotifier;
import com.ceos.beatbuddy.domain.event.entity.EventCommentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventCommentNotificationListener {
    private final EventCommentNotifier eventCommentNotifier;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEventCommentCreated(EventCommentCreatedEvent event) {
        try {
            // 스태프가 다른 유저 댓글에 답글 달았을 때 부모 작성자 알림
            if (event.parent() != null && event.isStaff()) {
                eventCommentNotifier.notifyParentAuthorIfStaffReply(
                        event.event(), event.member(), event.parent(), event.comment());
            }

            // 이벤트 주최자에게 알림
            eventCommentNotifier.notifyHostIfNewComment(
                    event.event(), event.member(), event.comment());

        } catch (Exception e) {
            log.error("💥 댓글 알림 전송 실패 - 내부적으로 재시도 큐에 저장됨", e);
        }
    }
}
