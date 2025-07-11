package com.ceos.beatbuddy.domain.event.controller;

import com.ceos.beatbuddy.domain.event.application.EventElasticService;
import com.ceos.beatbuddy.domain.event.dto.EventListResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/es/events")
@Tag(name = "Event Search Controller", description = "이벤트 검색 관련 API")
public class EventElasticSearchController implements EventSearchApiDocs {
    private final EventElasticService eventElasticService;
    /**
     * 개발/테스트 목적으로 모든 이벤트를 Elasticsearch에 동기화합니다.
     * 운영 환경에서는 이벤트 저장 시 개별적으로 인덱싱됩니다.
     */
    @PostMapping("/events/sync")
    public ResponseEntity<String> reindexAllEvents() throws IOException {
        eventElasticService.syncAll();
        return ResponseEntity.ok("✅ All events indexed into Elasticsearch.");
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<ResponseDTO<List<EventResponseDTO>>> searchEvents(
            @RequestParam String keyword
    ) throws IOException {
        Long memberId = SecurityUtils.getCurrentMemberId();
        List<EventResponseDTO> results = eventElasticService.search(keyword, memberId);
        return ResponseEntity
                .status(SuccessCode.EVENT_SEARCH_SUCCESS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.EVENT_SEARCH_SUCCESS, results));
    }
}
