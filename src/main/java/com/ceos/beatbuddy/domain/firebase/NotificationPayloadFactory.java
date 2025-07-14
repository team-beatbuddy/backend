package com.ceos.beatbuddy.domain.firebase;

import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.ceos.beatbuddy.domain.firebase.entity.FirebaseMessageType.*;

@Component
public class NotificationPayloadFactory {

    // 팔로우 알림
    public NotificationPayload createFollowPayload(Long followerId, String followerName) {
        return NotificationPayload.builder()
                .title(followerName + "님이 나를 팔로우했어요!")
                .data(Map.of(
                        "type", FOLLOW.getType(),
                        "followerId", String.valueOf(followerId),
                        "url", "/profile/" + followerId
                ))
                .build();
    }

    // ============================================== 자유 게시판 내 게시글 댓글 알림
    public NotificationPayload createPostCommentPayload(Long postId, Long commentId, String commenterName, String commentContent) {
        return NotificationPayload.builder()
                .title(commenterName + "님이 내 게시글에 댓글을 남겼어요.")
                .body(commentContent)
                .data(Map.of(
                        "type", POST_COMMENT.getType(),
                        "postId", String.valueOf(postId),
                        "commentId", String.valueOf(commentId),
                        "url", "/post/" + postId + "/comment/" + commentId
                ))
                .build();
    }


    // ================================================ 자유게시판 대댓글 알림
    public NotificationPayload createReplyCommentPayload(Long postId, Long commentId, String replierName, String replyContent) {
        return NotificationPayload.builder()
                .title(replierName + "님이 내 댓글에 대댓글을 남겼어요.")
                .body(replyContent)
                .data(Map.of(
                        "type", POST_COMMENT.getType(),
                        "postId", String.valueOf(postId),
                        "commentId", String.valueOf(commentId),
                        "url", "/post/" + postId + "/comment/" + commentId
                ))
                .build();
    }

    // ==================================================== 이벤트 대댓글 알림 (담당자의 답변만 전송)
    public NotificationPayload createEventReplyCommentPayload(Long eventId, Long commentId, String replyContent, Long replyId) {
        return NotificationPayload.builder()
                .title("내 문의에 답변이 작성되었어요.")
                .body(replyContent)
                .data(Map.of(
                        "type", EVENT_COMMENT.getType(),
                        "eventId", String.valueOf(eventId),
                        "commentId", String.valueOf(commentId),
                        "url", "/event/" + eventId + "/comment/" + commentId + "/reply/" + replyId
                ))
                .build();
    }

    // ===================================================== 이벤트 문의 댓글 알림 (글 작성자만 받음)
    public NotificationPayload createEventCommentNotificationPayload(Long eventId, Long commentId, String commentContent) {
        return NotificationPayload.builder()
                .title("새로운 문의 사항이 접수되었어요.")
                .body(commentContent)
                .data(Map.of(
                        "type", EVENT_COMMENT.getType(),
                        "eventId", String.valueOf(eventId),
                        "commentId", String.valueOf(commentId),
                        "url", "/event/" + eventId + "/comment/" + commentId
                ))
                .build();
    }

    // ======================================================  참석하기로 한 이벤트에 대한 알림
    // 1일 전
    public NotificationPayload createEventAttendanceD1NotificationPayload(Long eventId, String eventTitle, Long notificationId) {
        return NotificationPayload.builder()
                .title(eventTitle + "참석 1일 전이에요!")
                .body("참석 명단을 작성한 이벤트예요.")
                .data(Map.of(
                        "type", EVENT_COMMENT.getType(),
                        "eventId", String.valueOf(eventId),
                        "url", "/event/" + eventId,
                        "notificationId", String.valueOf(notificationId)
                ))
                .build();
    }

    // 당일
    public NotificationPayload createEventAttendanceDDayNotificationPayload(Long eventId, String eventTitle, Long notificationId) {
        return NotificationPayload.builder()
                .title(eventTitle + "참석 당일이에요!")
                .body("참석 명단을 작성한 이벤트예요.")
                .data(Map.of(
                        "type", EVENT_COMMENT.getType(),
                        "eventId", String.valueOf(eventId),
                        "url", "/event/" + eventId,
                        "notificationId", String.valueOf(notificationId)
                ))
                .build();
    }

    // ======================================================== 새로운 게시글 홍보 알림
    public NotificationPayload createNewPostPromotionPayload(String notiTitle, String notiContent, String type, Long postId, String imageUrl) {
        if (type == null || type.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_NOTIFICATION_TYPE);
        }

        String finalType;
        String url;

        switch (type) {
            case "magazine" -> {
                finalType = MAGAZINE.getType();
                url = "/magazine";
            }
            case "event" -> {
                finalType = EVENT.getType();
                url = "/event";
            }
            case "venue" -> {
                finalType = VENUE.getType();
                url = "/venue";
            }
            default -> throw new CustomException(ErrorCode.INVALID_NOTIFICATION_TYPE);
        }

        NotificationPayload.NotificationPayloadBuilder builder = NotificationPayload.builder()
                .title(notiTitle)
                .body(notiContent)
                .data(Map.of(
                        "type", finalType,
                        "url", url + "/" + postId
                ));

        if (imageUrl != null && !imageUrl.isEmpty()) {
            builder.imageUrl(imageUrl);
        }

        return builder.build();
    }

}
