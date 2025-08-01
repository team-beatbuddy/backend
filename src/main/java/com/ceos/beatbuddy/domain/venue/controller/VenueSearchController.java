package com.ceos.beatbuddy.domain.venue.controller;

import com.ceos.beatbuddy.domain.venue.application.VenueSearchService;
import com.ceos.beatbuddy.domain.venue.dto.VenueSearchResponseDTO;
import com.ceos.beatbuddy.domain.venue.entity.VenueDocument;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/es/venues")
public class VenueSearchController implements VenueSearchApiDocs {

    private final VenueSearchService venueSearchService;

    @Override
    @PostMapping("/index")
    public ResponseEntity<Void> indexVenue(@RequestBody VenueDocument venue) throws IOException {
        venueSearchService.indexVenue(venue);
        return ResponseEntity.ok().build();
    }
    @Override
    @GetMapping("/search")
    public ResponseEntity<ResponseDTO<List<VenueSearchResponseDTO>>> search(@RequestParam (required = false) @NotBlank(message = "검색 시, 키워드는 필수입니다.") String keyword) throws IOException {
        List<VenueSearchResponseDTO> results = venueSearchService.searchByKeywordForAddress(keyword);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_VENUE_SEARCH.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_VENUE_SEARCH, results));
    }
    @Override
    @PostMapping("/sync")
    public ResponseEntity<String> sync() throws IOException {
        venueSearchService.syncVenueFromDBToES();
        return ResponseEntity.ok("색인 완료");
    }
}
