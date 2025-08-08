package com.ceos.beatbuddy.domain.event.controller;

import com.ceos.beatbuddy.domain.event.application.EventService;
import com.ceos.beatbuddy.domain.event.dto.EventStatusDTO;
import com.ceos.beatbuddy.domain.event.scheduler.EventStatusScheduler;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Profile("dev")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/events/test")
@Tag(name = "Event Test Controller", description = "이벤트 기능\n")
public class EventTestController {
    private final EventService eventService;
    private final EventStatusScheduler eventStatusScheduler;
    /**
     * 테스트용: 이벤트 상태 스케줄러 수동 실행
     */
    @PostMapping("/status-update")
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
    @GetMapping("/{eventId}/status")
    public ResponseEntity<ResponseDTO<EventStatusDTO>> getEventStatus(@PathVariable Long eventId) {
        EventStatusDTO result = eventService.getEventStatus(eventId);

        return ResponseEntity.ok()
                .body(new ResponseDTO<>(SuccessCode.SUCCESS, result));
    }

}
