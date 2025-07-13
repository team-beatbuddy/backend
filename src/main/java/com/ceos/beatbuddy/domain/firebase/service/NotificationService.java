package com.ceos.beatbuddy.domain.firebase.service;

import com.ceos.beatbuddy.domain.firebase.FirebaseMessageType;
import com.ceos.beatbuddy.domain.firebase.NotificationPayload;
import com.ceos.beatbuddy.domain.firebase.dto.NotificationListDTO;
import com.ceos.beatbuddy.domain.firebase.dto.NotificationPageDTO;
import com.ceos.beatbuddy.domain.firebase.entity.Notification;
import com.ceos.beatbuddy.domain.firebase.repository.NotificationRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import com.ceos.beatbuddy.global.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final MemberService memberService;

    @Transactional
    public void save(Member receiver, NotificationPayload payload) {
        if (receiver == null || payload == null) return;

        // DB 저장
        notificationRepository.save(
                com.ceos.beatbuddy.domain.firebase.entity.Notification.builder()
                        .receiver(receiver)
                        .title(payload.getTitle())
                        .message(payload.getBody())
                        .imageUrl(payload.getImageUrl())
                        .type(FirebaseMessageType.valueOf(payload.getData().get("type")))
                        .isRead(false)
                        .readAt(null)
                        .build()
        );
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
}
