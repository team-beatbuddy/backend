package com.ceos.beatbuddy.domain.event.controller;

import com.ceos.beatbuddy.domain.event.dto.*;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
                                    "likes": 1,
                                    "views": 1,
                                    "liked": true,
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
                                  "error": "FORBIDDEN",
                                  "code": "CANNOT_ADD_EVENT_UNAUTHORIZED_MEMBER",
                                  "message": "글을 작성할 수 없는 유저입니다."
                                }
                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
            responseCode = "500",
            description = "S3에 이미지 등록 실패했을 경우",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "s3에 이미지 등록을 실패했을 경우",
                                    value = """
                                        {
                                          "status": 500,
                                          "error": "INTERNAL_SERVER_ERROR",
                                          "code": "IMAGE_UPLOAD_FAILED",
                                          "message": "이미지 업로드에 실패했습니다."
                                        }
                                            """
                            )
                    }
            )
    )
    })
    ResponseEntity<ResponseDTO<EventResponseDTO>> addEvent(
            @Valid @RequestPart("eventCreateRequestDTO") EventCreateRequestDTO eventCreateRequestDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException;


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
            description = "오늘 포함 XXXX 앞으로의 이벤트를 보여줍니다. sort 에는 popular / latest / region을 넣을 수 있으나 현재는 region은 구현되어있지 않습니다.")
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
                        """),
                            @ExampleObject(name = "이벤트 없음", value = """
            {
              "status": 404,
              "error": "NOT_FOUND",
              "code": "NOT_FOUND_EVENT",
              "message": "존재하지 않는 이벤트입니다."
            }
            """)
                    }
            ))
    })
    ResponseEntity<ResponseDTO<EventListResponseDTO>> getEventUpcomingSorted (@PathVariable String sort,
                                                                                @RequestParam(defaultValue = "1") Integer page,
                                                                                @RequestParam(defaultValue = "10") Integer size);



    @Operation(summary = "진행되는 이벤트",
            description = "(시작 날짜 기준 <= 오늘 ,종료 날짜 기준 >= 오늘) 진행 중인 이벤트를 보여줍니다. sort 에는 popular / latest / region을 넣을 수 있으나 현재는 region은 구현되어있지 않습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "진행 중인 이벤트", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(name = "진행 중인 이벤트 조회 성공", value = """
                    {
                      "status": 200,
                      "code": "SUCCESS_GET_NOW_EVENT",
                      "message": "성공적으로 과거 이벤트를 조회했습니다.",
                      "data": {
                        "sort": "latest",
                        "page": 1,
                        "size": 10,
                        "totalSize": 1,
                        "eventResponseDTOS": [
                          {
                            "eventId": 1,
                            "title": "이벤트 시작",
                            "content": "이게 바로 이트",
                            "image": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ddded007-dGroup%201000003259.png",
                            "location": "경기도 파주",
                            "likes": 5,
                            "views": 0,
                            "startDate": "2025-06-17",
                            "endDate": "2025-06-17"
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
                        """),
                            @ExampleObject(name = "이벤트 없음", value = """
                        {
                          "status": 404,
                          "error": "NOT_FOUND",
                          "code": "NOT_FOUND_EVENT",
                          "message": "존재하지 않는 이벤트입니다."
                        }
                        """)
                    }
            ))
    })
    ResponseEntity<ResponseDTO<EventListResponseDTO>> getEventNowSorted (        @PathVariable String sort,
                                                                                 @RequestParam(defaultValue = "1") Integer page,
                                                                                 @RequestParam(defaultValue = "10") Integer size);


    @Operation(summary = "종료된 이벤트",
            description = "(종료 날짜 기준 < 오늘) 종료가 된 이벤트를 보여줍니다. sort 에는 popular / latest / region을 넣을 수 있으나 현재는 region은 구현되어있지 않습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "종료된 이벤트", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(name = "이미 종료된 이벤트 조회 성공", value = """
                    {
                      "status": 200,
                      "code": "SUCCESS_GET_PAST_EVENT",
                      "message": "성공적으로 과거 이벤트를 조회했습니다.",
                      "data": {
                        "sort": "latest",
                        "page": 1,
                        "size": 10,
                        "totalSize": 1,
                        "eventResponseDTOS": [
                          {
                            "eventId": 1,
                            "title": "이벤트 시작",
                            "content": "이게 바로 이트",
                            "image": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ddded007-dGroup%201000003259.png",
                            "location": "경기도 파주",
                            "likes": 5,
                            "views": 0,
                            "startDate": "2025-06-17",
                            "endDate": "2025-06-17"
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
                        """),
                            @ExampleObject(name = "이벤트 없음", value = """
            {
              "status": 404,
              "error": "NOT_FOUND",
              "code": "NOT_FOUND_EVENT",
              "message": "존재하지 않는 이벤트입니다."
            }
            """)
                    }
            ))
    })
    ResponseEntity<ResponseDTO<EventListResponseDTO>> getEventPastSorted(
            @PathVariable String sort,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size);




    @Operation(summary = "이벤트 신청자 명단 조회", description = "이벤트 신청자 명단을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "이벤트 신청자 명단 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "이벤트 신청자 명단 조화 성공", value = """
                        {
                          "status": 200,
                          "code": "SUCCESS_GET_EVENT_ATTENDANCE_LIST",
                          "message": "이벤트 참여자 명단을 조회했습니다.",
                          "data": {
                            "eventId": null,
                            "totalMember": 2,
                            "eventAttendanceExportDTOS": [
                              {
                                "name": "string",
                                "gender": "None",
                                "phoneNumber": null
                              },
                              {
                                "name": "string",
                                "gender": "None",
                                "phoneNumber": "string"
                              }
                            ]
                          }
                        }
                        """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "이벤트 또는 유저 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 없음", value = """
                                {
                                  "status": 404,
                                  "error": "NOT_FOUND",
                                  "code": "MEMBER_NOT_EXIST",
                                  "message": "요청한 유저가 존재하지 않습니다."
                                }
                                """),
                                    @ExampleObject(name = "이벤트 없음", value = """
                                {
                                  "status": 404,
                                  "error": "NOT_FOUND",
                                  "code": "NOT_FOUND_EVENT",
                                  "message": "존재하지 않는 이벤트입니다."
                                }
                                """)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "이벤트를 작성한 유저가 아님",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 권한 없음", value = """
                                {
                                  "status": 403,
                                  "error": "FORBIDDEN",
                                  "code": "FORBIDDEN_EVENT_ACCESS",
                                  "message": "해당 이벤트에 대한 접근 권한이 없습니다."
                                }
                                """)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<EventAttendanceExportListDTO>> getEventAttendanceList(@PathVariable Long eventId);

    @Operation(summary = "이벤트 신청자 명단 엑셀 다운로드",
            description = "신청자 명단이 엑셀 다운받을 수 잇는 링크로 반환됩니다. Download file을 누르면 됩니다.")
    void downloadAttendanceExcel(
            @PathVariable Long eventId,
            HttpServletResponse response
    ) throws IOException;

    @Operation(summary = "이벤트 좋아요", description = "이벤트에 좋아요를 표시합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "이벤트 좋아요 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "이벤트 좋아요", value = """
                        {
                          "status": 201,
                          "code": "SUCCESS_LIKE_EVENT",
                          "message": "이벤트에 성공적으로 좋아요 표시하였습니다.",
                          "data": {
                            "eventId": 2,
                            "title": "string",
                            "content": "string",
                            "image": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png",
                            "likes": 1,
                            "views": 1,
                            "liked": true,
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
                    description = "유저 또는 이벤트가 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 없음", value = """
                                {
                                  "status": 404,
                                  "error": "NOT_FOUND",
                                  "code": "MEMBER_NOT_EXIST",
                                  "message": "요청한 유저가 존재하지 않습니다."
                                }
                                """),
                                    @ExampleObject(name = "이벤트 없음", value = """
                                {
                                  "status": 404,
                                  "error": "NOT_FOUND",
                                  "code": "NOT_FOUND_EVENT",
                                  "message": "존재하지 않는 이벤트입니다."
                                }
                                """)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 좋아요한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "이미 좋아요한 경우",
                                    value = """
                                {
                                  "status": 409,
                                  "error": "CONFLICT",
                                  "code": "ALREADY_LIKE_EVENT",
                                  "message": "해당 이벤트는 이미 좋아요를 표시하였습니다."
                                }
                                """
                            )
                    )
            )
    })
    ResponseEntity<ResponseDTO<EventResponseDTO>> likeEvent(@PathVariable Long eventId);

    @Operation(summary = "이벤트 좋아요 취소\n",
            description = "이벤트에 좋아요를 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이벤트에 좋아요를 취소합니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 201,
                                      "code": "SUCCESS_DELETE_LIKE",
                                      "message": "좋아요를 취소했습니다.",
                                      "data": {
                                        "eventId": 2,
                                        "title": "string",
                                        "content": "string",
                                        "image": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png",
                                        "likes": 1,
                                        "views": 1,
                                        "liked": true,
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
                    description = "유저 또는 이벤트가 존재하지 않거나, 좋아요 정보가 없습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
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
                                    ),
                                    @ExampleObject(
                                            name = "존재하지 않는 이벤트",
                                            value = """
                                                    {
                                                      "status": 404,
                                                      "error": "NOT_FOUND",
                                                      "code": "NOT_FOUND_EVENT",
                                                      "message": "존재하지 않는 이벤트입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "기존에 좋아요를 누르지 않았던 경우",
                                            value = """
                                                    {
                                                      "status": 404,
                                                      "error": "NOT_FOUND",
                                                      "code": "NOT_FOUND_LIKE",
                                                      "message": "기존에 좋아요를 누르지 않았습니다. 좋아요를 취소할 수 없습니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<EventResponseDTO>> deleteLikeEvent(@PathVariable Long eventId);

    @Operation(summary = "이벤트 댓글 작성\n",
            description = "이벤트에 댓글을 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공적으로 댓글을 작성했습니다. ******부모 댓글이 없는 경우에는 해당 필드를 지우고 작성해주세요. 댓글 레벨이 0이면 본인 글입니다. 1부터 대댓글",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 201,
                              "code": "SUCCESS_CREATED_COMMENT",
                              "message": "성공적으로 댓글을 작성했습니다.",
                              "data": {
                                "commentId": 1,
                                "commentLevel": 0,
                                "content": "댓글 써봄",
                                "authorNickname": "익명",
                                "anonymous": true,
                                "createdAt": "2025-06-18T02:08:31.4185432"
                              }
                            }
                                        """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "유저 또는 이벤트가 존재하지 않거나, 좋아요 정보가 없습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
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
                                    ),
                                    @ExampleObject(
                                            name = "존재하지 않는 이벤트",
                                            value = """
                                                    {
                                                      "status": 404,
                                                      "error": "NOT_FOUND",
                                                      "code": "NOT_FOUND_EVENT",
                                                      "message": "존재하지 않는 이벤트입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "부모 댓글이 존재하지 않는 경우",
                                            value = """
                                            {
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "code": "NOT_FOUND_COMMENT",
                                              "message": "해당 댓글을 찾을 수 없습니다."
                                            }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "내용을 작성하지 않은 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "댓글 내용 X", value = """
                                    {
                                      "status": 400,
                                      "error": "BAD_REQUEST",
                                      "code": "BAD_REQUEST_VALIDATION",
                                      "message": "요청 값이 유효하지 않습니다.",
                                      "errors": {
                                        "content": "내용은 필수입니다."
                                      }
                                    }
                                """)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<EventCommentResponseDTO>> createComment(
            @PathVariable Long eventId,
            @Valid @RequestBody EventCommentCreateRequestDTO dto);


    @Operation(summary = "이벤트 댓글 삭제\n",
            description = "이벤트에 댓글을 삭제합니다. level 이 0이면 원댓글부터 대댓글 전체 삭제됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 댓글을 삭제했습니다. level 이 0이면 원댓글부터 대댓글 전체 삭제됩니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "SUCCESS_DELETE_COMMENT",
                              "message": "성공적으로 댓글을 삭제했습니다.",
                              "data": "댓글 삭제 완료"
                            }
                                        """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "유저 또는 이벤트가 존재하지 않거나, 좋아요 정보가 없습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
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
                                    ),
                                    @ExampleObject(
                                            name = "존재하지 않는 이벤트",
                                            value = """
                                                    {
                                                      "status": 404,
                                                      "error": "NOT_FOUND",
                                                      "code": "NOT_FOUND_EVENT",
                                                      "message": "존재하지 않는 이벤트입니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "댓글이 존재하지 않는 경우",
                                            value = """
                                            {
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "code": "NOT_FOUND_COMMENT",
                                              "message": "해당 댓글을 찾을 수 없습니다."
                                            }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "댓글을 작성한 유저가 아닙니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 권한 없음", value = """
                                {
                                  "status": 403,
                                  "error": "FORBIDDEN",
                                  "code": "UNAUTHORIZED_MEMBER",
                                  "message": "글의 작성자가 아닙니다."
                                }
                                """)
                            }
                    )
            )

    })
    @DeleteMapping("/{eventId}/comments/{commentId}/levels/{commentLevel}")
    ResponseEntity<ResponseDTO<String>> deleteComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId,
            @PathVariable Integer commentLevel);




    @Operation(summary = "이벤트 상세 조회\n",
            description = "이벤트 상세 조회입니다. liked는 좋아요를 눌렀는지에 대한 여부입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 이벤트를 조회했습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "SUCCESS_GET_EVENT",
                              "message": "성공적으로 이벤트를 조회했습니다.",
                              "data": {
                                "eventId": 2,
                                "title": "string",
                                "content": "string",
                                "image": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png",
                                "liked": true,
                                "likes": 2,
                                "views": 1,
                                "receiveInfo": true,
                                "receiveName": true,
                                "receiveGender": true,
                                "receivePhoneNumber": true,
                                "receiveSNSId": true,
                                "receiveMoney": true
                              }
                            }
                                        """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "유저 또는 이벤트가 존재하지 않거나, 좋아요 정보가 없습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
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
                                    ),
                                    @ExampleObject(
                                            name = "존재하지 않는 이벤트",
                                            value = """
                                                    {
                                                      "status": 404,
                                                      "error": "NOT_FOUND",
                                                      "code": "NOT_FOUND_EVENT",
                                                      "message": "존재하지 않는 이벤트입니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            )

    })
    ResponseEntity<ResponseDTO<EventResponseDTO>> getEventDetail(@PathVariable Long eventId);

    @Operation(
            summary = "이벤트 댓글 전체 조회\n",
            description = "이벤트 댓글 전체 조회입니다. id 0 - level 0 은 본문, id 0 - level - 1 이면 id 0 에 대한 댓글의 대댓글"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "이벤트 댓글 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "이벤트 댓글 조회 성공",
                                            value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS_GET_EVENT_COMMENTS",
                                      "message": "성공적으로 댓글을 조회했습니다.",
                                      "data": [
                                        {
                                          "commentId": 2,
                                          "commentLevel": 0,
                                          "content": "string",
                                          "authorNickname": "BeatBuddy",
                                          "anonymous": false,
                                          "createdAt": "2025-06-18T02:28:19.423835",
                                          "replies": []
                                        },
                                        {
                                          "commentId": 1,
                                          "commentLevel": 0,
                                          "content": "댓글 써봄",
                                          "authorNickname": "익명",
                                          "anonymous": true,
                                          "createdAt": "2025-06-18T02:08:31.418543",
                                          "replies": [
                                            {
                                              "commentId": 1,
                                              "commentLevel": 1,
                                              "content": "대댓",
                                              "authorNickname": "익명",
                                              "anonymous": true,
                                              "createdAt": "2025-06-18T02:56:10.818788"
                                            },
                                            {
                                              "commentId": 1,
                                              "commentLevel": 2,
                                              "content": "string",
                                              "authorNickname": "익명",
                                              "anonymous": true,
                                              "createdAt": "2025-06-18T03:03:16.206686"
                                            }
                                          ]
                                        }
                                      ]
                                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "빈 이벤트 댓글",
                                            value = """
                                        {
                                          "status": 200,
                                          "code": "SUCCESS_BUT_EMPTY_LIST",
                                          "message": "성공적으로 조회했으나 리스트가 비었습니다.",
                                          "data": []
                                        }
                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "유저 또는 이벤트가 존재하지 않거나, 좋아요 정보가 없습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
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
                                    ),
                                    @ExampleObject(
                                            name = "존재하지 않는 이벤트",
                                            value = """
                    {
                      "status": 404,
                      "error": "NOT_FOUND",
                      "code": "NOT_FOUND_EVENT",
                      "message": "존재하지 않는 이벤트입니다."
                    }
                    """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<List<EventCommentTreeResponseDTO>>> getEventComments(@PathVariable Long eventId);


    @Operation(
            summary = "마이페이지 이벤트 조회\n",
            description = "마이페이지에서 이벤트를 조회합니다. 비즈니스 회원인 경우 본인이 작성한 글, 일반 회원의 경우 좋아요 누르거나 참여한 글"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "마이페이지 이벤트 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "마이페이지 이벤트 조회 성공",
                                            value = """
                                        {
                                          "status": 200,
                                          "code": "SUCCESS_GET_MY_EVENTS",
                                          "message": "마이페이지의 이벤트를 성공적으로 조회했습니다",
                                          "data": {
                                            "past": [
                                              {
                                                "eventId": 1,
                                                "title": "이벤트 시작",
                                                "content": "이게 바로 이트",
                                                "image": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ddded007-dGroup%201000003259.png",
                                                "location": "경기도 파주",
                                                "likes": 5,
                                                "views": 0,
                                                "startDate": "2025-06-17",
                                                "endDate": "2025-06-17"
                                              }
                                            ],
                                            "upcoming": [
                                              {
                                                "eventId": 2,
                                                "title": "string",
                                                "content": "string",
                                                "image": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png",
                                                "location": "string",
                                                "likes": 2,
                                                "views": 0,
                                                "startDate": "2025-06-18",
                                                "endDate": "2025-06-23",
                                                "dday": "D-0"
                                              }
                                            ]
                                          }
                                        }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "빈 이벤트 리스트",
                                            value = """
                                        {
                                          "status": 200,
                                          "code": "SUCCESS_BUT_EMPTY_LIST",
                                          "message": "성공적으로 조회했으나 리스트가 비었습니다.",
                                          "data": []
                                        }
                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "유저 정보가 없습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
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
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<Map<String, List<EventResponseDTO>>>> getMyEvents();
}
