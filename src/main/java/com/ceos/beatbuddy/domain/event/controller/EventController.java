package com.ceos.beatbuddy.domain.event.controller;

import com.ceos.beatbuddy.domain.event.application.*;
import com.ceos.beatbuddy.domain.event.scheduler.EventStatusScheduler;
import com.ceos.beatbuddy.domain.event.dto.*;
import com.ceos.beatbuddy.domain.event.exception.EventErrorCode;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/events")
@Tag(name = "Event Controller", description = "이벤트 기능\n")
public class EventController implements EventApiDocs {
    private final EventService eventService;
    private final EventAttendanceService eventAttendanceService;
    private final EventInteractionService eventInteractionService;
    private final EventElasticService eventElasticService;
    private final EventStatusScheduler eventStatusScheduler;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO<EventResponseDTO>> addEvent(
            @Valid @RequestPart("eventCreateRequestDTO") EventCreateRequestDTO eventCreateRequestDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventResponseDTO result = eventService.addEvent(memberId, eventCreateRequestDTO, images);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_CREATED_EVENT.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_CREATED_EVENT, result));
    }

    @Override
    @GetMapping("/{eventId}")
    public ResponseEntity<ResponseDTO<EventResponseDTO>> getEventDetail(@PathVariable Long eventId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventResponseDTO result = eventService.getEventDetail(eventId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_EVENT.getHttpStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_EVENT, result));
    }

    @Override
    @PatchMapping(value = "/{eventId}",
                    consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO<EventResponseDTO>> updateEvent(@PathVariable Long eventId,
                                                                     @RequestPart("eventUpdateRequestDTO") EventUpdateRequestDTO eventUpdateRequestDTO,
                                                                     @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventResponseDTO result = eventService.updateEvent(eventId, eventUpdateRequestDTO, memberId, images);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_UPDATE_EVENT.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_UPDATE_EVENT, result));
    }


    @Override
    @PostMapping("/{eventId}/attendance")
    public ResponseEntity<ResponseDTO<EventAttendanceResponseDTO>> addEventAttendance (@PathVariable Long eventId, @RequestBody EventAttendanceRequestDTO dto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventAttendanceResponseDTO result = eventAttendanceService.addEventAttendance(memberId, dto, eventId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_CREATED_EVENT_ATTENDANCE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_CREATED_EVENT_ATTENDANCE, result));
    }

    // 이벤트 홈에 예정된 이벤트
    @Override
    @GetMapping("/upcoming/{sort}")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getEventUpcomingSorted (        @PathVariable String sort,
                                                                                               @RequestParam(defaultValue = "1") Integer page,
                                                                                               @RequestParam(defaultValue = "10") Integer size,
                                                                                             @RequestParam(required = false) List<String> region) {
        Long memberId = SecurityUtils.getCurrentMemberId();

        EventListResponseDTO result = eventService.getUpcomingEvents(sort, page, size, memberId, region);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_UPCOMING_EVENT.getStatus().value())
            .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_UPCOMING_EVENT, result));
}

    // 이벤트 홈에 진행 중인 이벤트
    @Override
    @GetMapping("/now")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getEventNowSorted (@RequestParam(defaultValue = "1") Integer page,
                                                                                @RequestParam(defaultValue = "10") Integer size,
                                                                                @RequestParam(required = false) List<String> region) {
        Long memberId = SecurityUtils.getCurrentMemberId();

        EventListResponseDTO result = eventService.getNowEvents(page, size, memberId, region);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_NOW_EVENT.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_NOW_EVENT, result));
    }


    // 이벤트 홈에 종료된 이벤트
    @Override
    @GetMapping("/past")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getEventPastSorted(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) List<String> region) {

        Long memberId = SecurityUtils.getCurrentMemberId();

        EventListResponseDTO result = eventService.getPastEvents(page, size, memberId, region);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_PAST_EVENT.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_PAST_EVENT, result));
    }


    @Override
    @GetMapping("/{eventId}/attendance-list")
    public ResponseEntity<ResponseDTO<EventAttendanceExportListDTO>> getEventAttendanceList(@PathVariable Long eventId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventAttendanceExportListDTO result = eventAttendanceService.getAttendanceList(eventId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_EVENT_ATTENDANCE_LIST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_EVENT_ATTENDANCE_LIST, result));
    }

    @Override
    @GetMapping("/{eventId}/attendance-list/excel")
    public void downloadAttendanceExcel(
            @PathVariable Long eventId,
            HttpServletResponse response
    ) throws IOException {
        Long memberId = SecurityUtils.getCurrentMemberId();
        List<EventAttendanceExportDTO> dtoList = eventAttendanceService.getAttendanceListForExcel(eventId, memberId);

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=attendances.xlsx");

        try (Workbook workbook = EventAttendanceExcelExporter.export(dtoList);
             OutputStream os = response.getOutputStream()) {
            workbook.write(os);
        }
    }

    @Override
    @PostMapping("/{eventId}/like")
    public ResponseEntity<ResponseDTO<String>> likeEvent(@PathVariable Long eventId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        eventInteractionService.likeEvent(eventId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_LIKE_EVENT.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_LIKE_EVENT, "좋아요를 눌렀습니다."));
    }

    @Override
    @DeleteMapping("/{eventId}/like")
    public ResponseEntity<ResponseDTO<String>> deleteLikeEvent(@PathVariable Long eventId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        eventInteractionService.deleteLikeEvent(eventId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_DELETE_LIKE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_DELETE_LIKE, "좋아요를 취소했습니다."));
    }

    @Override
    @DeleteMapping("/{eventId}/attendance")
    public ResponseEntity<ResponseDTO<String>> deleteEventAttendance(@PathVariable Long eventId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        eventAttendanceService.deleteAttendance(eventId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_DELETE_ATTENDANCE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_DELETE_ATTENDANCE, "이벤트 참석을 취소했습니다."));
    }

    @Override
    @GetMapping("/{eventId}/attendance")
    public ResponseEntity<ResponseDTO<EventAttendanceResponseDTO>> getEventAttendance(@PathVariable Long eventId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventAttendanceResponseDTO result = eventAttendanceService.getAttendance(eventId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_ATTENDANCE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_ATTENDANCE, result));
    }

    @Override
    @PatchMapping("/{eventId}/attendance")
    public ResponseEntity<ResponseDTO<EventAttendanceResponseDTO>> updateEventAttendance(
            @PathVariable Long eventId,
            @RequestBody EventAttendanceUpdateDTO dto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventAttendanceResponseDTO result = eventAttendanceService.updateAttendance(eventId, memberId, dto);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_UPDATE_ATTENDANCE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_UPDATE_ATTENDANCE, result));
    }

    @Override
    @GetMapping("/search/period")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getSearchEventWithPeriod(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        if (startDate.isAfter(endDate)) {
            throw new CustomException(EventErrorCode.INVALID_DATE_RANGE);
        }

        Long memberId = SecurityUtils.getCurrentMemberId();
        EventListResponseDTO result = eventService.getEventsInPeriod(memberId, startDate, endDate, page, size);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_SEARCH_EVENT_LIST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_SEARCH_EVENT_LIST, result));
    }


    @Override
    @GetMapping("/search")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> searchEvents(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventListResponseDTO results = eventElasticService.search(keyword, memberId, page, size);
        return ResponseEntity
                .status(SuccessCode.EVENT_SEARCH_SUCCESS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.EVENT_SEARCH_SUCCESS, results));
    }

    @Profile("dev")
    @PostMapping("sync")
    public ResponseEntity<Void> sync() throws IOException {
        try {
            eventElasticService.syncAll();
        } catch (IOException e) {
            log.error("Event Elasticsearch sync failed: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok().build();
    }
    
    /**
     * 테스트용: 이벤트 상태 스케줄러 수동 실행
     */
    @Profile("dev")
    @PostMapping("/test/status-update")
    public ResponseEntity<ResponseDTO<String>> testStatusUpdate() {
        log.info("🧪 테스트: 이벤트 상태 업데이트 수동 실행");
        eventStatusScheduler.runManually();
        
        return ResponseEntity.ok()
                .body(new ResponseDTO<>(SuccessCode.SUCCESS, 
                      "이벤트 상태 업데이트 스케줄러가 수동으로 실행되었습니다. 로그를 확인해주세요."));
    }
    
    /**
     * 테스트용: 특정 이벤트 상태 확인
     */
    @Profile("dev")
    @GetMapping("/test/{eventId}/status")
    public ResponseEntity<ResponseDTO<EventStatusDTO>> getEventStatus(@PathVariable Long eventId) {
        EventStatusDTO result = eventService.getEventStatus(eventId);
        
        return ResponseEntity.ok()
                .body(new ResponseDTO<>(SuccessCode.SUCCESS, result));
    }

}