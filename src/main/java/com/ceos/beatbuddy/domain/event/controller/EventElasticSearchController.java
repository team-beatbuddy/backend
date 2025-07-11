package com.ceos.beatbuddy.domain.event.controller;

import com.ceos.beatbuddy.domain.event.application.EventElasticService;
import com.ceos.beatbuddy.domain.event.dto.EventSearchListResponseDTO;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/es/events")
@Tag(name = "Event Search Controller", description = "이벤트 검색 관련 API")
public class EventElasticSearchController implements EventSearchApiDocs {
    private final EventElasticService eventElasticService;

    @PostMapping("/events/sync")
    public ResponseEntity<String> reindexAllEvents() throws IOException {
        eventElasticService.syncAll();
        return ResponseEntity.ok("✅ All events indexed into Elasticsearch.");
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<ResponseDTO<EventSearchListResponseDTO>> searchEvents(
            @RequestParam String keyword
    ) throws IOException {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventSearchListResponseDTO results = eventElasticService.searchCategorized(keyword, memberId);
        return ResponseEntity
                .status(SuccessCode.EVENT_SEARCH_SUCCESS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.EVENT_SEARCH_SUCCESS, results));
    }
}
