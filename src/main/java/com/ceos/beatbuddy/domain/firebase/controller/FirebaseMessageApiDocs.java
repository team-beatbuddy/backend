package com.ceos.beatbuddy.domain.firebase.controller;

import com.ceos.beatbuddy.domain.firebase.dto.NotificationPageDTO;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface FirebaseMessageApiDocs {
    @Operation(
            summary = "전체 알림 (홍보용) CRM, 홍보 포함",
            description = "모든 사용자에게 홍보용 알림을 전송합니다. " +
                    "title, body, imageUrl, type, postId를 통해 알림의 내용을 설정할 수 있습니다. " +
                    "targetRole을 통해 알림을 받을 대상의 역할을 지정할 수 있습니다."
    )
    @Parameter(name = "title", description = "알림 제목", example = "새로운 이벤트가 시작되었습니다!")
    @Parameter(name = "body", description = "알림 내용", example = "우리의 새로운 이벤트에 참여해보세요!")
    @Parameter(name = "imageUrl", description = "알림에 포함될 이미지 URL", example = "https://example.com/image.jpg")
    @Parameter(name = "type", description = "알림 유형 (예: EVENT, MAGAZINE, VENUE)", example = "EVENT")
    @Parameter(name = "postId", description = "해당 홍보와 관련 있는 id (예: event 글 아이디 1)", example = "1")
    @Parameter(name = "targetRole", description = "알림을 받을 대상의 역할 (예: USER, BUSINESS, ADMIN 등)", example = "ALL")
    void sendNotificationToRole(@RequestParam String title,
                                @RequestParam String body,
                                @RequestParam(required = false) String imageUrl,
                                @RequestParam(required = false) String type,
                                @Parameter(
                                        description = "해당 홍보와 관련 있는 id 넣으시면 됩니다. 예시) event 글 아이디 1",
                                        example = "1"
                                )
                                @RequestParam(required = false) Long postId,
                                @RequestParam(defaultValue = "ALL") List<String> targetRole);


    @Operation(
            summary = "본인 알림 목록 가져오기",
            description = "현재 로그인한 사용자의 알림 목록을 가져옵니다. " +
                    "페이지네이션을 지원하며, 기본값은 1페이지에 10개의 알림입니다."
    )
    @Parameter(name = "page", description = "페이지 번호 (기본값: 1)", example = "1")
    @Parameter(name = "size", description = "페이지당 알림 개수 (기본값: 10)", example = "10")
    ResponseEntity<ResponseDTO<NotificationPageDTO>> getNotifications(
            @RequestParam(defaultValue = "1")int page,
            @RequestParam(defaultValue = "10") int size);
}
