package com.ceos.beatbuddy.domain.comment;

import com.ceos.beatbuddy.domain.comment.entity.Comment;
import com.ceos.beatbuddy.domain.firebase.NotificationPayload;
import com.ceos.beatbuddy.domain.firebase.NotificationPayloadFactory;
import com.ceos.beatbuddy.domain.firebase.service.NotificationSender;
import com.ceos.beatbuddy.domain.firebase.service.NotificationService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostCommentNotifier {
    private final NotificationService notificationService;
    private final NotificationPayloadFactory notificationPayloadFactory;
    private final NotificationSender notificationSender;

    public void notifyPostAuthor(Comment comment, Long writerId) {
        Member postAuthor = comment.getPost().getMember();
        Member commentWriter = comment.getMember();

        // 자신이 자기 글에 댓글 단 경우 알림 제외
        if (postAuthor.getId().equals(writerId)) return;

        NotificationPayload notificationPayload = notificationPayloadFactory.createPostCommentPayload(
                comment.getPost().getId(),
                comment.getId(),
                commentWriter.getNickname(),
                comment.getContent()
        );

        if (notificationPayload != null) {
            notificationService.save(postAuthor, notificationPayload);
            notificationSender.send(postAuthor.getFcmToken(), notificationPayload);
        }
    }

    public void notifyParentCommentAuthor(Comment comment, Long writerId) {
        if (comment.getReply() == null) return;

        Member parentWriter = comment.getReply().getMember();
        if (!parentWriter.getId().equals(writerId)) {
            NotificationPayload notificationPayload = notificationPayloadFactory.createReplyCommentPayload(
                    comment.getPost().getId(),
                    comment.getId(),
                    comment.getMember().getNickname(),
                    comment.getContent()
            );

            if (notificationPayload != null) {
                notificationService.save(parentWriter, notificationPayload);
                notificationSender.send(parentWriter.getFcmToken(), notificationPayload);
            }
        }
    }

}
