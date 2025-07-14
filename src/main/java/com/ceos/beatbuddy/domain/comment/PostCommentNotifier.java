package com.ceos.beatbuddy.domain.comment;

import com.ceos.beatbuddy.domain.comment.entity.Comment;
import com.ceos.beatbuddy.domain.firebase.NotificationPayload;
import com.ceos.beatbuddy.domain.firebase.NotificationPayloadFactory;
import com.ceos.beatbuddy.domain.firebase.entity.Notification;
import com.ceos.beatbuddy.domain.firebase.service.NotificationSender;
import com.ceos.beatbuddy.domain.firebase.service.NotificationService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
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
                comment.isAnonymous() ? "익명" : commentWriter.getNickname(),
                comment.getContent()
        );

        if (notificationPayload != null) {
            Notification saved = notificationService.save(postAuthor, notificationPayload);
            notificationPayload.getData().put("notificationId", String.valueOf(saved.getId()));
            notificationSender.send(postAuthor.getFcmToken(), notificationPayload);
        }
    }

    public void notifyParentCommentAuthor(Comment comment, Long writerId) {
        // Changed to debug for normal entry log
        log.debug("notifyParentCommentAuthor 진입: commentId={}, writerId={}", comment.getId(), writerId);

        if (comment.getReply() == null) {
            // Debug is enough when there's no parent reply
            log.debug("대댓글이 아님 (reply가 null), 알림 전송 생략");
            return;
        }

        Member parentWriter = comment.getReply().getMember();

        if (!parentWriter.getId().equals(writerId)) {
            NotificationPayload notificationPayload = notificationPayloadFactory.createReplyCommentPayload(
                    comment.getPost().getId(),
                    comment.getId(),
                    comment.isAnonymous() ? "익명" : comment.getMember().getNickname(),
                    comment.getContent()
            );

            if (notificationPayload != null) {
                Notification saved = notificationService.save(parentWriter, notificationPayload);
                notificationPayload.getData().put("notificationId", String.valueOf(saved.getId()));
                notificationSender.send(parentWriter.getFcmToken(), notificationPayload);
                // Single info log summarizing successful send
                log.info("대댓글 알림 전송 완료: commentId={}, receiverId={}", comment.getId(), parentWriter.getId());
            } else {
                // Elevated to error since payload failure is exceptional
                log.error("알림 payload 생성 실패: commentId={}", comment.getId());
            }
        } else {
            // Self-replies are normal flow, so debug level
            log.debug("본인 댓글에 대한 대댓글 알림 스킵: writerId={}", writerId);
        }
    }


}
