package com.ceos.beatbuddy.domain.event;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventComment;
import com.ceos.beatbuddy.domain.firebase.NotificationPayload;
import com.ceos.beatbuddy.domain.firebase.NotificationPayloadFactory;
import com.ceos.beatbuddy.domain.firebase.entity.Notification;
import com.ceos.beatbuddy.domain.firebase.service.NotificationSender;
import com.ceos.beatbuddy.domain.firebase.service.NotificationService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventCommentNotifier {
    private final NotificationSender notificationSender;
    private final NotificationService notificationService;
    private final NotificationPayloadFactory notificationPayloadFactory;

    public void notifyParentAuthorIfStaffReply(Event event, Member replier, EventComment parent, EventComment reply) {
        boolean isReplierHost = replier.getId().equals(event.getHost().getId());
        boolean isReplyToOther = !parent.getAuthor().getId().equals(replier.getId());

        if (isReplierHost && isReplyToOther) {
            Member parentAuthor = parent.getAuthor();
            NotificationPayload payload = notificationPayloadFactory.createEventReplyCommentPayload(
                    event.getId(), parent.getId(), reply.getContent(), reply.getId());
            
            // DB에 먼저 저장 (무조건 성공)
            Notification saved = notificationService.save(parentAuthor, payload);
            payload.getData().put("notificationId", String.valueOf(saved.getId()));

            // FCM 전송은 별도 처리 (실패해도 DB에는 저장됨)
            try {
                notificationSender.send(parentAuthor.getFcmToken(), payload);
            } catch (Exception e) {
                // 로그는 NotificationSender에서 처리
            }
        }
    }

    public void notifyHostIfNewComment(Event event, Member commenter, EventComment comment) {
        if (!event.getHost().getId().equals(commenter.getId())) {
            Member host = event.getHost();
            NotificationPayload payload = notificationPayloadFactory.createEventCommentNotificationPayload(
                    event.getId(), comment.getId(), comment.getContent());
            
            // DB에 먼저 저장 (무조건 성공)
            Notification saved = notificationService.save(host, payload);
            payload.getData().put("notificationId", String.valueOf(saved.getId()));
            
            // FCM 전송은 별도 처리 (실패해도 DB에는 저장됨)
            try {
                notificationSender.send(host.getFcmToken(), payload);
            } catch (Exception e) {
                // 로그는 NotificationSender에서 처리
            }
        }
    }
}
