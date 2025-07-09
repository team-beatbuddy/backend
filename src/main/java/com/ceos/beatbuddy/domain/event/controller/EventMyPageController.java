package com.ceos.beatbuddy.domain.event.controller;

import com.ceos.beatbuddy.domain.event.application.EventMyPageService;
import com.ceos.beatbuddy.domain.event.dto.EventListResponseDTO;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/events/my-page")
@Tag(name = "EventMyPageController", description = "마이페이지 이벤트 관련 API")
public class EventMyPageController implements EventMyPageApiDocs{
    private final EventMyPageService eventMyPageService;

    @Override
    @GetMapping("/upcoming")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getMyPageEventsUpcoming(
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventListResponseDTO result = eventMyPageService.getMyPageEventsUpcoming(memberId, region, page, size);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_MY_PAGE_EVENTS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MY_PAGE_EVENTS, result));
    }

    @Override
    @GetMapping("/now")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getMyPageEventsNow(
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventListResponseDTO result = eventMyPageService.getMyPageEventsNow(memberId, region, page, size);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_MY_PAGE_EVENTS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MY_PAGE_EVENTS, result));
    }

    @Override
    @GetMapping("/past")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getMyPageEventsPast(
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventListResponseDTO result = eventMyPageService.getMyPageEventsPast(memberId, region, page, size);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_MY_PAGE_EVENTS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MY_PAGE_EVENTS, result));
    }


    // 내가 작성한 이벤트 조회
    @Override
    @GetMapping("/my-event/upcoming")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getMyEventsUpcoming(
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventListResponseDTO result = eventMyPageService.getMyUpcomingEvents(memberId, region, page, size);

        if (result.getEventResponseDTOS().isEmpty()) {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_BUT_EMPTY_LIST.getStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_BUT_EMPTY_LIST, result));
        }
        else {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_GET_MY_EVENTS.getStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MY_EVENTS, result));
        }
    }
    @Override
    @GetMapping("/my-event/now")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getMyEventsNow(
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventListResponseDTO result = eventMyPageService.getMyNowEvents(memberId, region, page, size);

        if (result.getEventResponseDTOS().isEmpty()) {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_BUT_EMPTY_LIST.getStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_BUT_EMPTY_LIST, result));
        }
        else {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_GET_MY_EVENTS.getStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MY_EVENTS, result));
        }
    }

    @Override
    @GetMapping("/my-event/past")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getMyEventsPast(
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventListResponseDTO result = eventMyPageService.getMyPastEvents(memberId, region, page, size);

        if (result.getEventResponseDTOS().isEmpty()) {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_BUT_EMPTY_LIST.getStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_BUT_EMPTY_LIST, result));
        }
        else {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_GET_MY_EVENTS.getStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MY_EVENTS, result));
        }
    }

}
