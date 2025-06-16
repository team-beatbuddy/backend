package com.ceos.beatbuddy.domain.event.controller;

import com.ceos.beatbuddy.domain.event.application.EventAttendanceService;
import com.ceos.beatbuddy.domain.event.application.EventService;
import com.ceos.beatbuddy.domain.event.dto.EventAttendanceRequestDTO;
import com.ceos.beatbuddy.domain.event.dto.EventAttendanceResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventCreateRequestDTO;
import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.domain.magazine.controller.MagazineApiDocs;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineRequestDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineResponseDTO;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@Tag(name = "Event Controller", description = "이벤트 기능\n")
public class EventController implements EventApiDocs {
    private final EventService eventService;
    private final EventAttendanceService eventAttendanceService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO<EventResponseDTO>> addEvent(
            @Valid @RequestPart("eventCreateRequestDTO") EventCreateRequestDTO eventCreateRequestDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) {
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


}
