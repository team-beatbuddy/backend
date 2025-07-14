package com.ceos.beatbuddy.domain.firebase.service;

import com.ceos.beatbuddy.domain.firebase.NotificationPayload;
import com.ceos.beatbuddy.domain.firebase.dto.NotificationListDTO;
import com.ceos.beatbuddy.domain.firebase.dto.NotificationPageDTO;
import com.ceos.beatbuddy.domain.firebase.entity.FirebaseMessageType;
import com.ceos.beatbuddy.domain.firebase.entity.Notification;
import com.ceos.beatbuddy.domain.firebase.repository.NotificationRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.constant.Role;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import com.ceos.beatbuddy.global.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final NotificationSender notificationSender;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification save(Member receiver, NotificationPayload payload) {
        log.info("🔔 save called with receiverId={}, payload={}",
                receiver != null ? receiver.getId() : null,
                payload != null ? payload.getTitle() : "null");

        if (receiver == null || payload == null) {
            log.warn("⚠️ 알림 저장 스킵됨: receiver 또는 payload null");
            return null;
        }

        try {
            String typeStr = payload.getData().get("type");
            if (typeStr == null) {
                log.warn("⚠️ payload 내 'type' 없음: {}", payload.getData());
                return null;
            }

            FirebaseMessageType type = FirebaseMessageType.valueOf(typeStr);

            Notification saved = Notification.builder()
                    .receiver(receiver)
                    .title(payload.getTitle())
                    .message(payload.getBody())
                    .imageUrl(payload.getImageUrl())
                    .type(type)
                    .isRead(false)
                    .readAt(null)
                    .build();
            notificationRepository.save(saved);

            log.info("✅ 알림 저장 완료 (type={}): {}", type, payload.getBody());
            return saved;

        } catch (Exception e) {
            log.error("❌ 알림 저장 중 예외 발생", e);
        }
        return null;
    }


    @Transactional
    public void markAsRead(Long memberId, Long notificationId) {
        com.ceos.beatbuddy.domain.firebase.entity.Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_NOTIFICATION));

        if (!notification.getReceiver().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        notification.markAsRead();
    }


    // 알림창 조회
    @Transactional(readOnly = true)
    public NotificationPageDTO getNotificationList(Long memberId, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);

        // 페이지 유효성 검사
        if (page < 1) {
            throw new CustomException(ErrorCode.PAGE_OUT_OF_BOUNDS);
        }

        Page<Notification> notifications = notificationRepository.findByReceiver(member, PageRequest.of(page - 1, size));
        if (notifications.isEmpty()) {
            throw new CustomException(SuccessCode.SUCCESS_BUT_EMPTY_LIST);
        }

        // 알림 목록 조회
        List<NotificationListDTO> notificationList = notifications.stream()
                .map(NotificationListDTO::from)
                .toList();

        return NotificationPageDTO.builder()
                .page(page)
                .size(size)
                .totalElements((int) notifications.getTotalElements())
                .totalPages(notifications.getTotalPages())
                .content(notificationList)
                .build();
    }


    @Transactional
    public void sendNotificationToRoles(List<String> targetRoles, NotificationPayload basePayload) {
        targetRoles.forEach(role -> {
            List<Member> targetMembers = resolveTargetMembers(role);
            for (Member member : targetMembers) {
                try {
                    NotificationPayload payloadCopy = basePayload.copy();
                    Notification saved = save(member, payloadCopy);
                    payloadCopy.getData().put("notificationId", String.valueOf(saved.getId()));

                    notificationSender.send(member.getFcmToken(), payloadCopy);
                } catch (Exception e) {
                    log.warn("❌ 알림 전송 실패: memberId={}, reason={}", member.getId(), e.getMessage());
                }
            }
        });
    }

    private List<Member> resolveTargetMembers(String role) {
        if (role.equalsIgnoreCase("ALL")) {
            return memberRepository.findAll();
        }
        try {
            return memberRepository.findAllByRole(Role.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            log.warn("❗ 알 수 없는 역할: {}", role);
            return Collections.emptyList();
        }
    }
}
