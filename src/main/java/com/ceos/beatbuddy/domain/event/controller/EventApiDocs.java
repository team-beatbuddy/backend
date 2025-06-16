package com.ceos.beatbuddy.domain.event.controller;

import com.ceos.beatbuddy.domain.event.dto.EventCreateRequestDTO;
import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

public interface EventApiDocs {
    @Operation(
            summary = "이벤트 작성 기능",
            description = """
        admin과 business 멤버에 한해서만 이벤트를 작성할 수 있도록 제한되어 있습니다. 
        데이터 전달은 multipart/form-data이며, 
        'eventCreateRequestDTO'는 JSON 문자열 형태로 전송해야 합니다.
    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "object"),
                            examples = @ExampleObject(value = """
                ----- eventCreateRequestDTO -----
                {
                  "title": "2025 여름 페스티벌",
                  "content": "EDM과 함께하는 신나는 여름 이벤트",
                  "startDate": "2025-07-20",
                  "endDate": "2025-07-20",
                  "location": "서울 마포구 홍대입구역 근처",
                  "receiveInfo": true,
                  "venueId": 2,
                  "depositAccount": "국민 123456-78-901234",
                  "depositAmount": 15000
                }
                ----- image -----
                (Multipart File 업로드)
            """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "이벤트가 성공적으로 작성되었습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 201,
                  "code": "SUCCESS_CREATED_EVENT",
                  "message": "이벤트가 성공적으로 작성되었습니다.",
                  "data": {
                    "eventId": 1,
                    "title": "이벤트 시작",
                    "content": "이게 바로 이트",
                    "image": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ddded007-dGroup%201000003259.png"
                  }
                }
            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 유저",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                {
                  "status": 404,
                  "error": "NOT_FOUND",
                  "code": "MEMBER_NOT_EXIST",
                  "message": "요청한 유저가 존재하지 않습니다."
                }
            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없는 유저",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                {
                  "status": 403,
                  "error": "UNAUTHORIZED",
                  "code": "CANNOT_ADD_MAGAZINE_UNAUTHORIZED_MEMBER",
                  "message": "글을 작성할 수 없는 유저입니다."
                }
            """)
                    )
            )
    })
    ResponseEntity<ResponseDTO<EventResponseDTO>> addEvent(
            @RequestPart("eventCreateRequestDTO") EventCreateRequestDTO eventCreateRequestDTO,
            @RequestPart(value = "image", required = false) MultipartFile image);

}
