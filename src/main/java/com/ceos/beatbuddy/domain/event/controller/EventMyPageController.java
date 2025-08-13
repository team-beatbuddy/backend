package com.ceos.beatbuddy.domain.event.controller;

import com.ceos.beatbuddy.domain.event.application.EventMyPageService;
import com.ceos.beatbuddy.domain.event.dto.EventListResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.domain.event.entity.EventStatus;
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

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/events/my-page")
@Tag(name = "EventMyPageController", description = "마이페이지 이벤트 관련 API")
public class EventMyPageController implements EventMyPageApiDocs{
    private final EventMyPageService eventMyPageService;

    @Override
    @GetMapping("/upcoming-now/attendance")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getMyPageEventsNowAndUpcomingAttendance(
            @RequestParam(required = false) List<String> region,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventListResponseDTO result = eventMyPageService.getMyPageEventsNowAndUpcomingAttendance(memberId, region, page, size);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_MY_PAGE_EVENTS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MY_PAGE_EVENTS, result));
    }

    @Override
    @GetMapping("/upcoming-now/liked")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getMyPageEventsNowAndUpcomingLiked(
            @RequestParam(required = false) List<String> region,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventListResponseDTO result = eventMyPageService.getMyPageEventsNowAndUpcomingLiked(memberId, region, page, size);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_MY_PAGE_EVENTS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MY_PAGE_EVENTS, result));
    }

    @Override
    @GetMapping("/past")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getMyPageEventsPast(
            @RequestParam(required = false) List<String> region,
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
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getMyEventsNowAndUpcoming(
            @RequestParam(required = false) List<String> region,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        // 현재 진행 중이거나 예정인 이벤트를 조회
        Set<EventStatus> statuses = Set.of(EventStatus.UPCOMING, EventStatus.NOW);
        EventListResponseDTO result = eventMyPageService.getMyPageEventsByStatuses(memberId, region, statuses, page, size);

        return buildEventResponse(result);
    }

    @Override
    @GetMapping("/my-event/past")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getMyEventsPast(
            @RequestParam(required = false) List<String> region,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventListResponseDTO result = eventMyPageService.getMyPageEventsByStatus(memberId, region, EventStatus.PAST, page, size);

        return buildEventResponse(result);
    }

    @Override
    @GetMapping("/my-event/top3")
    public ResponseEntity<ResponseDTO<List<EventResponseDTO>>> getMyEventsUpcomingTop3() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        List<EventResponseDTO> result = eventMyPageService.getMyUpcomingTop3(memberId);

        if (result.isEmpty()) {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_BUT_EMPTY_LIST.getHttpStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_BUT_EMPTY_LIST, result));
        } else {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_GET_MY_EVENTS.getHttpStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MY_EVENTS, result));
        }
    }



    private ResponseEntity<ResponseDTO<EventListResponseDTO>> buildEventResponse(
            EventListResponseDTO result) {
        if (result.getEventResponseDTOS().isEmpty()) {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_BUT_EMPTY_LIST.getHttpStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_BUT_EMPTY_LIST, result));
        } else {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_GET_MY_EVENTS.getHttpStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MY_EVENTS, result));
        }
    }
}
