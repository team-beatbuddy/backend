package com.ceos.beatbuddy.domain.event.controller;

import com.ceos.beatbuddy.domain.event.application.EventAttendanceExcelExporter;
import com.ceos.beatbuddy.domain.event.application.EventAttendanceService;
import com.ceos.beatbuddy.domain.event.application.EventCommentService;
import com.ceos.beatbuddy.domain.event.application.EventService;
import com.ceos.beatbuddy.domain.event.dto.*;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@Tag(name = "Event Controller", description = "이벤트 기능\n")
public class EventController implements EventApiDocs {
    private final EventService eventService;
    private final EventAttendanceService eventAttendanceService;
    private final EventCommentService eventCommentService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO<EventResponseDTO>> addEvent(
            @Valid @RequestPart("eventCreateRequestDTO") EventCreateRequestDTO eventCreateRequestDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventResponseDTO result = eventService.addEvent(memberId, eventCreateRequestDTO, image);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_CREATED_EVENT.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_CREATED_EVENT, result));
    }


    @Override
    @PostMapping("/{eventId}")
    public ResponseEntity<ResponseDTO<EventAttendanceResponseDTO>> addEventAttendance (@PathVariable Long eventId, @RequestBody EventAttendanceRequestDTO dto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventAttendanceResponseDTO result = eventAttendanceService.addEventAttendance(memberId, dto, eventId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_CREATED_EVENT_ATTENDANCE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_CREATED_EVENT_ATTENDANCE, result));
    }

    @Override
    @GetMapping("/upcoming/{sort}")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getEventUpcomingSorted (        @PathVariable String sort,
                                                                                               @RequestParam(defaultValue = "1") Integer page,
                                                                                               @RequestParam(defaultValue = "10") Integer size) {
        Long memberId = SecurityUtils.getCurrentMemberId();

        EventListResponseDTO result = eventService.getUpcomingEvents(sort, page, size, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_UPCOMING_EVENT.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_UPCOMING_EVENT, result));
    }

    @Override
    @GetMapping("/now/{sort}")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getEventNowSorted (        @PathVariable String sort,
                                                                                              @RequestParam(defaultValue = "1") Integer page,
                                                                                              @RequestParam(defaultValue = "10") Integer size) {
        Long memberId = SecurityUtils.getCurrentMemberId();

        EventListResponseDTO result = eventService.getNowEvents(sort, page, size, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_NOW_EVENT.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_NOW_EVENT, result));
    }


    @Override
    @GetMapping("/past/{sort}")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getEventPastSorted(
            @PathVariable String sort,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Long memberId = SecurityUtils.getCurrentMemberId();

        EventListResponseDTO result = eventService.getPastEvents(sort, page, size, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_PAST_EVENT.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_PAST_EVENT, result));
    }


    @Override
    @GetMapping("/{eventId}/attendances")
    public ResponseEntity<ResponseDTO<EventAttendanceExportListDTO>> getEventAttendanceList(@PathVariable Long eventId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventAttendanceExportListDTO result = eventAttendanceService.getAttendanceList(eventId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_EVENT_ATTENDANCE_LIST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_EVENT_ATTENDANCE_LIST, result));
    }

    @Override
    @GetMapping("/{eventId}/attendances/excel")
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

    private String nullToHyphen(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    @Override
    @PostMapping("/{eventId}/like")
    public ResponseEntity<ResponseDTO<EventResponseDTO>> likeEvent(@PathVariable Long eventId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventResponseDTO result = eventService.likeEvent(eventId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_LIKE_EVENT.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_LIKE_EVENT, result));
    }

    @Override
    @DeleteMapping("/{eventId}/like")
    public ResponseEntity<ResponseDTO<EventResponseDTO>> deleteLikeEvent(@PathVariable Long eventId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventResponseDTO result = eventService.deleteLikeEvent(eventId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_DELETE_LIKE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_DELETE_LIKE, result));
    }

    @Override
    @PostMapping("/{eventId}/comments")
    public ResponseEntity<ResponseDTO<EventCommentResponseDTO>> createComment(
            @PathVariable Long eventId,
            @Valid @RequestBody EventCommentCreateRequestDTO dto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventCommentResponseDTO result = eventCommentService.createComment(eventId, memberId, dto, dto.getParentCommentId());

        return ResponseEntity
                .status(SuccessCode.SUCCESS_CREATED_COMMENT.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_CREATED_COMMENT, result));
    }

    @Override
    @DeleteMapping("/{eventId}/comments/{commentId}/levels/{commentLevel}")
    public ResponseEntity<ResponseDTO<String>> deleteComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId,
            @PathVariable Integer commentLevel) {

        Long memberId = SecurityUtils.getCurrentMemberId();
        eventCommentService.deleteComment(eventId, commentId, commentLevel, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_DELETE_COMMENT.getHttpStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_DELETE_COMMENT, "댓글 삭제 완료"));
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
    @GetMapping("/{eventId}/comments")
    public ResponseEntity<ResponseDTO<List<EventCommentTreeResponseDTO>>> getEventComments(@PathVariable Long eventId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        List<EventCommentTreeResponseDTO> result = eventCommentService.getSortedEventComments(memberId, eventId);

        if (result.isEmpty()) {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_BUT_EMPTY_LIST.getHttpStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_BUT_EMPTY_LIST, result));
        }
        else {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_GET_EVENT_COMMENTS.getHttpStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_EVENT_COMMENTS, result));
        }
    }


    @Override
    @GetMapping("/my-page")
    public ResponseEntity<ResponseDTO<Map<String, List<EventResponseDTO>>>> getMyEvents() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        Map<String, List<EventResponseDTO>> result = eventService.getMyPageEvents(memberId);

        if (result.isEmpty()) {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_BUT_EMPTY_LIST.getHttpStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MY_EVENTS, result));
        }
        else {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_GET_MY_EVENTS.getHttpStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MY_EVENTS, result));
        }
    }
}
