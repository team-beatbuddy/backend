package com.ceos.beatbuddy.domain.event;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventComment;
import com.ceos.beatbuddy.domain.firebase.NotificationPayload;
import com.ceos.beatbuddy.domain.firebase.NotificationPayloadFactory;
import com.ceos.beatbuddy.domain.firebase.service.NotificationSender;
import com.ceos.beatbuddy.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventCommentNotifier {
    private final NotificationSender notificationSender;
    private final NotificationPayloadFactory notificationPayloadFactory;

    public void notifyParentAuthorIfStaffReply(Event event, Member replier, EventComment parent, EventComment reply) {
        boolean isReplierHost = replier.getId().equals(event.getHost().getId());
        boolean isReplyToOther = !parent.getAuthor().getId().equals(replier.getId());

        if (isReplierHost && isReplyToOther) {
            Member parentAuthor = parent.getAuthor();
            if (parentAuthor.getFcmToken() != null) {
                NotificationPayload payload = notificationPayloadFactory.createEventReplyCommentPayload(
                        event.getId(), parent.getId(), reply.getContent());
                notificationSender.send(parentAuthor.getFcmToken(), payload);
            }
        }
    }

    public void notifyHostIfNewComment(Event event, Member commenter, EventComment comment) {
        if (!event.getHost().getId().equals(commenter.getId())) {
            Member host = event.getHost();
            if (host.getFcmToken() != null) {
                NotificationPayload payload = notificationPayloadFactory.createEventCommentNotificationPayload(
                        event.getId(), comment.getId(), comment.getContent());
                notificationSender.send(host.getFcmToken(), payload);
            }
        }
    }
}
