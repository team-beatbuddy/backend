package com.ceos.beatbuddy.domain.venue.controller;

import com.ceos.beatbuddy.domain.venue.dto.VenueSearchResponseDTO;
import com.ceos.beatbuddy.domain.venue.entity.VenueDocument;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

public interface VenueSearchApiDocs {
    // 여기에 VenueSearchController의 API 문서화 관련 메서드를 정의합니다.
    // 예: @ApiOperation, @ApiResponses 등
    // 예시:
    // @ApiOperation(value = "장소 색인", notes = "장소 정보를 Elasticsearch에 색인합니다.")
    // @ApiResponses(value = {
    //     @ApiResponse(code = 200, message = "색인 성공"),
    //     @ApiResponse(code = 500, message = "서버 오류")
    // })

    /**
     * 장소 정보를 Elasticsearch에 색인합니다.
     *
     * @param venue 색인할 장소 정보
     * @return 색인 성공 여부
     * @throws IOException Elasticsearch 인덱싱 중 오류 발생 시
     */
    @Operation(summary = "장소 색인", description = "장소 정보를 Elasticsearch에 색인합니다.")
    @ApiResponse(responseCode = "200", description = "색인 성공")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<Void> indexVenue(@RequestBody VenueDocument venue) throws IOException;

    /**
     * 키워드로 장소를 검색합니다.
     *
     * @param keyword 검색할 키워드
     * @return 검색 결과 목록
     * @throws IOException Elasticsearch 검색 중 오류 발생 시
     */
    @Operation(summary = "장소 검색", description = "키워드로 장소를 검색합니다. 검색 결과가 없을 경우 빈 리스트를 반환합니다. 해당 API만 이용하시면 됩니다.")
    @ApiResponse(responseCode = "200", description = "검색 결과 반환",
           content = @Content(mediaType = "application/json",
                   examples = {@ExampleObject(
                           name = "검색 결과 예시",
                           value = """
                                   {
                                       "status": 200,
                                       "code": "SUCCESS_VENUE_SEARCH",
                                       "message": "장소 검색 성공",
                                       "data": [
                                           {
                                               "id": 1,
                                               "englishName": "Venue One",
                                               "koreanName": "장소 하나",
                                               "address": "123 Main St"
                                           },
                                           {
                                               "id": 2,
                                               "englishName": "Venue Two",
                                               "koreanName": "장소 둘",
                                               "address": "456 Elm St"
                                           }
                                       ]
                                   }
                                   """
                   ),
                           @ExampleObject(
                                   name = "검색 결과가 없을 때",
                                   value = """
                                           {
                                               "status": 204,
                                               "code": "SUCCESS_BUT_EMPTY_LIST",
                                               "message": "검색 결과가 없습니다.",
                                               "data": []
                                           }
                                           """
                           )
                     }
           )

    )
    ResponseEntity<ResponseDTO<List<VenueSearchResponseDTO>>> search(@RequestParam String keyword) throws IOException;

    /**
     * 데이터베이스의 장소 정보를 Elasticsearch에 동기화합니다.
     *
     * @return 동기화 완료 메시지
     * @throws IOException Elasticsearch 동기화 중 오류 발생 시
     */
    @Operation(summary = "장소 동기화", description = "데이터베이스의 장소 정보를 Elasticsearch에 동기화합니다.")
    @ApiResponse(responseCode = "200", description = "동기화 완료")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<String> sync() throws IOException;
}
