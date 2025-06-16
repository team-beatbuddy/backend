package com.ceos.beatbuddy.domain.event.controller;

import com.ceos.beatbuddy.domain.event.dto.EventAttendanceRequestDTO;
import com.ceos.beatbuddy.domain.event.dto.EventAttendanceResponseDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

public interface EventApiDocs {
    @Operation(summary = "이벤트 작성 기능\n",
            description = "admin과 business 멤버에 한해서만 이벤트를 작성할 수 있도록 해두었습니다. (추후 변경 가능), 데이터 전달은 multipart/form-data이며 'eventCreateRequestDTO'는 JSON 문자열 형태로 전송해야 합니다.")

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "이벤트가 성공적으로 작성되었습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 201,
                                  "code": "SUCCESS_CREATED_EVENT",
                                  "message": "이벤트가 성공적으로 작성되었습니다.",
                                  "data": {
                                    "eventId": 2,
                                    "title": "string",
                                    "content": "string",
                                    "image": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png",
                                    "receiveInfo": true,
                                    "receiveName": true,
                                    "receiveGender": true,
                                    "receivePhoneNumber": true,
                                    "receiveTotalCount": false,
                                    "receiveSNSId": true,
                                    "receiveMoney": true
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
                            examples =
                            @ExampleObject(
                                    name = "존재하지 않는 유저",
                                    value = """
                                {
                                  "status": 404,
                                  "error": "NOT_FOUND",
                                  "code": "MEMBER_NOT_EXIST",
                                  "message": "요청한 유저가 존재하지 않습니다."
                                }
                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "잘못된 요청 (권한이 없는 일반 유저)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "권한 없는 유저",
                                            value = """
                                {
                                  "status": 403,
                                  "error": "UNAUTHORIZED",
                                  "code": "CANNOT_ADD_MAGAZINE_UNAUTHORIZED_MEMBER",
                                  "message": "글을 작성할 수 없는 유저입니다."
                                }
                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<EventResponseDTO>> addEvent(
            @Valid @RequestPart("eventCreateRequestDTO") EventCreateRequestDTO eventCreateRequestDTO,
            @RequestPart(value = "image", required = false) MultipartFile image);


    @Operation(summary = "이벤트 작성 기능\n",
            description = "admin과 business 멤버에 한해서만 이벤트를 작성할 수 있도록 해두었습니다. (추후 변경 가능), 데이터 전달은 multipart/form-data이며 'eventCreateRequestDTO'는 JSON 문자열 형태로 전송해야 합니다.")

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "이벤트가 성공적으로 작성되었습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 201,
                                  "code": "SUCCESS_CREATED_EVENT",
                                  "message": "이벤트가 성공적으로 작성되었습니다.",
                                  "data": {
                                    "eventId": 2,
                                    "title": "string",
                                    "content": "string",
                                    "image": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png",
                                    "receiveInfo": true,
                                    "receiveName": true,
                                    "receiveGender": true,
                                    "receivePhoneNumber": true,
                                    "receiveTotalCount": false,
                                    "receiveSNSId": true,
                                    "receiveMoney": true
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
                            examples =
                            @ExampleObject(
                                    name = "존재하지 않는 유저",
                                    value = """
                                {
                                  "status": 404,
                                  "error": "NOT_FOUND",
                                  "code": "MEMBER_NOT_EXIST",
                                  "message": "요청한 유저가 존재하지 않습니다."
                                }
                            """
                            )
                    )
            )
    })
    ResponseEntity<ResponseDTO<EventAttendanceResponseDTO>> addEventAttendance (@PathVariable Long eventId, @RequestBody EventAttendanceRequestDTO dto);
}
