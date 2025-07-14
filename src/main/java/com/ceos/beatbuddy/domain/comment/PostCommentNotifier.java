package com.ceos.beatbuddy.domain.comment;

import com.ceos.beatbuddy.domain.comment.entity.Comment;
import com.ceos.beatbuddy.domain.firebase.NotificationPayload;
import com.ceos.beatbuddy.domain.firebase.NotificationPayloadFactory;
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
            notificationService.save(postAuthor, notificationPayload);
            notificationSender.send(postAuthor.getFcmToken(), notificationPayload);
        }
    }

    public void notifyParentCommentAuthor(Comment comment, Long writerId) {
        log.warn("🔥 notifyParentCommentAuthor 진입: commentId={}, writerId={}", comment.getId(), writerId);
        log.warn("🔥 reply: {}", comment.getReply());

        if (comment.getReply() == null) {
            log.info("🔕 대댓글이 아님 (reply가 null), 알림 전송 생략");
            return;
        }

        Member parentWriter = comment.getReply().getMember();
        log.debug("📨 대댓글 대상 확인: parentWriterId={}, writerId={}", parentWriter.getId(), writerId);

        if (!parentWriter.getId().equals(writerId)) {
            log.info("🔔 대댓글 알림 대상: parentWriterId={} ← from writerId={}", parentWriter.getId(), writerId);

            NotificationPayload notificationPayload = notificationPayloadFactory.createReplyCommentPayload(
                    comment.getPost().getId(),
                    comment.getId(),
                    comment.isAnonymous() ? "익명" : comment.getMember().getNickname(),
                    comment.getContent()
            );

            if (notificationPayload != null) {
                log.info("✅ 알림 payload 생성 완료: title={}, body={}", notificationPayload.getTitle(), notificationPayload.getBody());
                log.debug("📦 payload data: {}", notificationPayload.getData());

                notificationService.save(parentWriter, notificationPayload);
                log.info("💾 알림 DB 저장 완료");

                notificationSender.send(parentWriter.getFcmToken(), notificationPayload);
                log.info("📤 FCM 전송 요청 완료: token={}", parentWriter.getFcmToken());
            } else {
                log.warn("⚠️ 알림 payload 생성 실패 → null 반환");
            }
        } else {
            log.info("🛑 본인이 본인 댓글에 대댓글을 달아서 알림 스킵: writerId={}", writerId);
        }
    }


}
