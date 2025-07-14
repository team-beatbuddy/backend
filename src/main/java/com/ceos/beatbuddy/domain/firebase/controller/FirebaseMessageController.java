package com.ceos.beatbuddy.domain.firebase.controller;

import com.ceos.beatbuddy.domain.firebase.FirebaseNotificationBroadcaster;
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
public class FirebaseMessageController {
    private final NotificationService notificationService;
    private final NotificationPayloadFactory notificationPayloadFactory;
    private final FirebaseNotificationBroadcaster firebaseNotificationBroadcaster;

    // 전체 알림 (홍보용)
    @PostMapping("/sendNotification")
    public void sendNotificationToRole(@RequestParam String title,
                                       @RequestParam String body,
                                       @RequestParam(required = false) String imageUrl,
                                       @RequestParam(required = false) String type,
                                       @Parameter(
                                               description = "해당 홍보와 관련 있는 id 넣으시면 됩니다. 예시) event 글 아이디 1",
                                               example = "1"
                                       )
                                       @RequestParam(required = false) Long postId,
                                       @RequestParam(defaultValue = "ALL") List<String> targetRole) {
        NotificationPayload payload = notificationPayloadFactory.createNewPostPromotionPayload(title, body, type, postId, imageUrl);
        // targetRole: 알림을 받을 대상의 역할 (USER, BUSINESS, ADMIN 등)
        targetRole.forEach(role -> {
            if (role.equalsIgnoreCase("ALL")) {
                role = "ALL"; // ALL 역할은 모든 사용자에게 알림을 보냄
            }
            firebaseNotificationBroadcaster.broadcast(role, payload);
        });
    }

    // 본인 알림 목록 가져오기
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
