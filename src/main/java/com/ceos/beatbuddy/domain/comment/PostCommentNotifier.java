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
    
    /**
     * 댓글 작성자의 표시 이름 결정
     */
    private String getDisplayName(Comment comment) {
        if (comment.isAnonymous()) {
            return comment.getAnonymousNickname() != null ? comment.getAnonymousNickname() : "익명";
        } else {
            Member writer = comment.getMember();
            return writer.getPostProfileInfo() != null && writer.getPostProfileInfo().getPostProfileNickname() != null
                    ? writer.getPostProfileInfo().getPostProfileNickname()
                    : writer.getNickname();
        }
    }

    public void notifyPostAuthor(Comment comment, Long writerId) {
        log.info("🔔 게시글 작성자 알림 시작 - postId: {}, commentId: {}", 
                comment.getPost().getId(), comment.getId());
                
        Member postAuthor = comment.getPost().getMember();
        Member commentWriter = comment.getMember();

        log.info("👤 게시글 작성자: {}, 댓글 작성자: {}", postAuthor.getId(), writerId);

        // 자신이 자기 글에 댓글 단 경우 알림 제외
        if (postAuthor.getId().equals(writerId)) {
            log.info("⏭️ 본인이 자기 글에 댓글 - 알림 스킵");
            return;
        }

        String displayName = getDisplayName(comment);
        log.info("📝 표시할 이름: {}", displayName);
                    
        log.info("📝 NotificationPayload 생성 시작");
        NotificationPayload notificationPayload = notificationPayloadFactory.createPostCommentPayload(
                comment.getPost().getId(),
                comment.getId(),
                displayName,
                comment.getContent()
        );
        log.info("📝 NotificationPayload 생성 완료: {}", notificationPayload != null ? "성공" : "null");

        if (notificationPayload != null) {
            log.info("💾 알림 DB 저장 시작 - receiver: {}, payload title: {}", postAuthor.getId(), notificationPayload.getTitle());
            try {
                Notification saved = notificationService.save(postAuthor, notificationPayload);
                log.info("💾 notificationService.save 호출 완료 - saved: {}", saved != null ? saved.getId() : "null");
                if (saved != null) {
                    notificationPayload.getData().put("notificationId", String.valueOf(saved.getId()));
                    log.info("✅ 알림 DB 저장 완료 - notificationId: {}", saved.getId());
                } else {
                    log.error("❌ saved가 null임");
                }
            } catch (Exception e) {
                log.error("❌ notificationService.save 호출 중 예외", e);
                throw e;
            }
            
            // FCM 전송은 별도 처리 (실패해도 DB에는 저장됨)
            try {
                log.info("🚀 FCM 알림 전송 시작 - token: {}", postAuthor.getFcmToken() != null ? "존재함" : "null");
                notificationSender.send(postAuthor.getFcmToken(), notificationPayload);
                log.info("✅ FCM 전송 성공");
            } catch (Exception e) {
                log.warn("⚠️ FCM 전송 실패하지만 알림은 목록에서 확인 가능: {}", e.getMessage());
            }
        } else {
            log.error("❌ NotificationPayload가 null");
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
            String displayName = getDisplayName(comment);
                        
            NotificationPayload notificationPayload = notificationPayloadFactory.createReplyCommentPayload(
                    comment.getPost().getId(),
                    comment.getId(),
                    displayName,
                    comment.getContent()
            );

            if (notificationPayload != null) {
                log.info("💾 대댓글 알림 DB 저장 시작");
                Notification saved = notificationService.save(parentWriter, notificationPayload);
                notificationPayload.getData().put("notificationId", String.valueOf(saved.getId()));
                log.info("✅ 대댓글 알림 DB 저장 완료 - notificationId: {}", saved.getId());
                
                // FCM 전송은 별도 처리 (실패해도 DB에는 저장됨)
                try {
                    notificationSender.send(parentWriter.getFcmToken(), notificationPayload);
                    log.info("✅ 대댓글 FCM 전송 성공: commentId={}, receiverId={}", comment.getId(), parentWriter.getId());
                } catch (Exception e) {
                    log.warn("⚠️ 대댓글 FCM 전송 실패하지만 알림은 목록에서 확인 가능: commentId={}, error={}", comment.getId(), e.getMessage());
                }
            } else {
                log.error("❌ 대댓글 알림 payload 생성 실패: commentId={}", comment.getId());
            }
        } else {
            // Self-replies are normal flow, so debug level
            log.debug("본인 댓글에 대한 대댓글 알림 스킵: writerId={}", writerId);
        }
    }


}
