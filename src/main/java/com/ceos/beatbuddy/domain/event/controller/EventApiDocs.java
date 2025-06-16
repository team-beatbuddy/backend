package com.ceos.beatbuddy.domain.event.controller;

import com.ceos.beatbuddy.domain.event.dto.*;
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
import org.springframework.web.bind.annotation.RequestParam;
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
                    responseCode = "400",
                    description = "필수 필드 누락 시",
                    content = @Content(
                            mediaType = "application/json",
                            examples =
                            @ExampleObject(
                                    name = "필수 필드 누락 시",
                                    value = """
                                {
                                  "status": 400,
                                  "error": "BAD_REQUEST",
                                  "code": "BAD_REQUEST_VALIDATION",
                                  "message": "요청 값이 유효하지 않습니다.",
                                  "errors": {
                                    "endDate": "종료날짜는 필수입니다.",
                                    "location": "장소는 필수입니다.",
                                    "title": "제목은 필수입니다.",
                                    "content": "본문은 필수입니다.",
                                    "startDate": "시작날짜는 필수입니다."
                                  }
                                }
                            """
                            )
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


    @Operation(summary = "이벤트 참석 신청",
                description = "이벤트에서 필요한 정보 수집들을 제대로 입력하지 않으면 아래와 같은 에러가 발생합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "이벤트 참석 성공", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(name = "참석 성공", value = """
                    {
                      "status": 201,
                      "code": "SUCCESS_CREATED_EVENT_ATTENDANCE",
                      "message": "이벤트 참여 폼이 성공적으로 작성되었습니다.",
                      "data": {
                        "eventId": 2,
                        "memberId": 156,
                        "name": "홍길동",
                        "gender": "None",
                        "snsType": "Instagram",
                        "snsId": "gil_dong",
                        "phoneNumber": "010-0000-0000",
                        "paid": true
                      }
             """)
            )),
            @ApiResponse(responseCode = "400", description = "입력값 누락 또는 잘못된 요청", content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "이름 누락", value = """
            {
              "status": 400,
              "error": "BAD_REQUEST",
              "code": "MISSING_NAME",
              "message": "이름 입력은 필수입니다."
            }
            """),
                            @ExampleObject(name = "성별 누락", value = """
            {
              "status": 400,
              "error": "BAD_REQUEST",
              "code": "MISSING_GENDER",
              "message": "성별 입력은 필수입니다."
            }
            """),
                            @ExampleObject(name = "전화번호 누락", value = """
            {
              "status": 400,
              "error": "BAD_REQUEST",
              "code": "MISSING_PHONE",
              "message": "핸드폰 번호 입력은 필수입니다."
            }
            """),
                            @ExampleObject(name = "동행 인원 누락", value = """
            {
              "status": 400,
              "error": "BAD_REQUEST",
              "code": "MISSING_TOTAL_COUNT",
              "message": "동행인원 입력은 필수입니다."
            }
            """),
                            @ExampleObject(name = "SNS 정보 누락", value = """
            {
              "status": 400,
              "error": "BAD_REQUEST",
              "code": "MISSING_SNS_ID_OR_TYPE",
              "message": "SNS ID 또는 TYPE 입력은 필수입니다."
            }
            """),
                            @ExampleObject(name = "지불 여부 누락", value = """
            {
              "status": 400,
              "error": "BAD_REQUEST",
              "code": "MISSING_PAYMENT",
              "message": "지불 완료 입력은 필수입니다."
            }
            """),
                            @ExampleObject(name = "성별 입력 오류", value = """
            {
              "status": 400,
              "error": "BAD_REQUEST",
              "code": "INVALID_GENDER",
              "message": "성별 값이 올바르지 않습니다. (MALE, FEMALE 중 하나여야 합니다.)"
            }
            """)
                    }
            )),
            @ApiResponse(responseCode = "404", description = "이벤트 또는 유저 정보 없음", content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "이벤트 없음", value = """
            {
              "status": 404,
              "error": "NOT_FOUND",
              "code": "NOT_FOUND_EVENT",
              "message": "존재하지 않는 이벤트입니다."
            }
            """),
                            @ExampleObject(name = "유저 없음", value = """
            {
              "status": 404,
              "error": "NOT_FOUND",
              "code": "MEMBER_NOT_EXIST",
              "message": "요청한 유저가 존재하지 않습니다."
            }
            """)
                    }
            )),
            @ApiResponse(responseCode = "409", description = "이미 참석한 경우", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(name = "중복 참석", value = """
            {
              "status": 409,
              "error": "CONFLICT",
              "code": "ALREADY_ATTENDANCE_EVENT",
              "message": "이미 참여 신청한 이벤트입니다."
            }
        """)
            ))
    })
    ResponseEntity<ResponseDTO<EventAttendanceResponseDTO>> addEventAttendance (@PathVariable Long eventId, @RequestBody EventAttendanceRequestDTO dto);

    @Operation(summary = "곧 진행될 이벤트",
            description = "오늘부터 앞으로의 이벤트를 보여줍니다. sort 에는 popular / latest / region을 넣을 수 있으나 현재는 region은 구현되어있지 않습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "곧 다가올 이벤트", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(name = "곧 다가올 이벤트 조회 성공", value = """
                    {
                      "status": 200,
                      "code": "SUCCESS_GET_UPCOMING_EVENT",
                      "message": "이벤트가 성공적으로 조회되었습니다.",
                      "data": {
                        "sort": "popular",
                        "page": 1,
                        "size": 10,
                        "totalSize": 2,
                        "eventResponseDTOS": [
                          {
                            "eventId": 1,
                            "title": "이벤트 시작",
                            "content": "이게 바로 이트",
                            "image": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ddded007-dGroup%201000003259.png",
                            "likes": 5,
                            "views": 0,
                            "scraps": 0,
                            "startDate": "2025-06-17",
                            "endDate": "2025-06-17",
                            "dday": "D-0",
                            "location": "경기도 파주"
                          },
                          {
                            "eventId": 2,
                            "title": "string",
                            "content": "string",
                            "image": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png",
                            "likes": 1,
                            "views": 0,
                            "scraps": 0,
                            "startDate": "2025-06-18",
                            "endDate": "2025-06-23",
                            "dday": "D-1",
                            "location": "서울시"
                            
                          }
                        ]
                      }
                    }
             """)
            )),
            @ApiResponse(responseCode = "404", description = "이벤트 또는 유저 정보 없음", content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "유저 없음", value = """
                        {
                          "status": 404,
                          "error": "NOT_FOUND",
                          "code": "MEMBER_NOT_EXIST",
                          "message": "요청한 유저가 존재하지 않습니다."
                        }
                        """)
                    }
            ))
    })
    ResponseEntity<ResponseDTO<EventListResponseDTO>> getEventUpcomingPopular (@PathVariable String sort,
                                                                                @RequestParam(defaultValue = "1") Integer page,
                                                                                @RequestParam(defaultValue = "10") Integer size);
}
