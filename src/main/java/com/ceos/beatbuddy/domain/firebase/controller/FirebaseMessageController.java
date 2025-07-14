package com.ceos.beatbuddy.domain.firebase.controller;

import com.ceos.beatbuddy.domain.firebase.NotificationPayload;
import com.ceos.beatbuddy.domain.firebase.NotificationPayloadFactory;
import com.ceos.beatbuddy.domain.firebase.dto.NotificationPageDTO;
import com.ceos.beatbuddy.domain.firebase.service.NotificationService;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/firebase")
@Tag(name = "Firebase controller", description = "Firebase 관련 API")
public class FirebaseMessageController implements FirebaseMessageApiDocs {
    private final NotificationService notificationService;
    private final NotificationPayloadFactory notificationPayloadFactory;

    // 전체 알림 (홍보용)
    @Override
    @PostMapping("/sendNotification")
    public ResponseEntity<ResponseDTO<String>> sendNotificationToRole(@RequestParam String title,
                                       @RequestParam String body,
                                       @RequestParam(required = false) String imageUrl,
                                       @RequestParam(required = false) String type,
                                       @Parameter(
                                               description = "해당 홍보와 관련 있는 id 넣으시면 됩니다. 예시) event 글 아이디 1",
                                               example = "1"
                                       )
                                       @RequestParam(required = false) Long postId,
                                       @RequestParam(defaultValue = "ALL") List<String> targetRole) {
        // 기본 payload 생성
        NotificationPayload basePayload = notificationPayloadFactory.createNewPostPromotionPayload(
                title, body, type, postId, imageUrl
        );

        notificationService.sendNotificationToRoles(targetRole, basePayload);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_SEND_NOTIFICATION.getStatus())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_SEND_NOTIFICATION, "알림이 성공적으로 전송되었습니다."));
    }

    // 알림 읽음 처리
    @Override
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ResponseDTO<String>> markAsRead(@PathVariable Long notificationId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        notificationService.markAsRead(memberId, notificationId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_MARK_NOTIFICATION_AS_READ.getStatus())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_MARK_NOTIFICATION_AS_READ, "읽음 처리 되었습니다."));
    }


    // 본인 알림 목록 가져오기
    @Override
    @GetMapping("/notifications")
    public ResponseEntity<ResponseDTO<NotificationPageDTO>> getNotifications(
            @RequestParam(defaultValue = "1")int page,
            @RequestParam(defaultValue = "10") int size) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        NotificationPageDTO result =  notificationService.getNotificationList(memberId, page, size);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_NOTIFICATIONS.getStatus())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_NOTIFICATIONS, result));
    }

}
