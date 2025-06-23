package com.ceos.beatbuddy.domain.event.controller;

import com.ceos.beatbuddy.domain.event.dto.*;
import com.ceos.beatbuddy.global.SwaggerExamples;
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
    @Operation(
            summary = "이벤트 작성 기능",
            description = """
                    admin과 business 멤버에 한해서만 이벤트를 작성할 수 있도록 해두었습니다. (추후 변경 가능)\n
                    데이터 전달은 multipart/form-data이며 'eventCreateRequestDTO'는 JSON 문자열 형태로 전송해야 합니다.\n
                    - receiveInfo: 참석자 정보 수집 여부\n
                    - receiveName: 참석자 이름 수집 여부\n
                    - receiveGender: 참석자 성별 수집 여부\n
                    - receivePhoneNumber: 참석자 전화번호 수집 여부\n
                    - receiveTotalCount: 참석자 본인 포함 동행인원 수집 여부\n
                    - receiveSNSId: 참석자 SNS ID 수집 여부\n
                    - receiveMoney: 예약금 설정 여부
                    """
                )

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
                                    "images": ["https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png"],
                                    "likes": 1,
                                    "views": 1,
                                    "liked": true,
                                    "startDate": "2025-08-22",
                                    "endDate": "2025-09-21",
                                    "receiveInfo": true,
                                    "receiveName": true,
                                    "receiveGender": true,
                                    "receivePhoneNumber": true,
                                    "receiveTotalCount": false,
                                    "receiveSNSId": true,
                                    "receiveMoney": false,
                                    "depositAccount": "",
                                    "depositAmount": 0
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
                                    {@ExampleObject(
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
                                    ),
                                    @ExampleObject(
                                            name = "예약금을 받으면서 정보가 없는 경우",
                                            value = """
                                                    {
                                                      "status": 400,
                                                      "error": "BAD_REQUEST",
                                                      "code": "NEED_DEPOSIT_INFO",
                                                      "message": "예약금에 관련된 정보가 필요합니다."
                                                    }
                                                    """
                                    )}
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 유저",
                    content = @Content(
                            mediaType = "application/json",
                            examples =
                            @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "잘못된 요청 (권한이 없는 일반 유저)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "권한 없는 유저", value = SwaggerExamples.CANNOT_ADD_EVENT_UNAUTHORIZED_MEMBER)
                            }
                    )
            ),
            @ApiResponse(
            responseCode = "500",
            description = "S3에 이미지 등록 실패했을 경우",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "s3에 이미지 등록을 실패했을 경우", value = SwaggerExamples.IMAGE_UPLOAD_FAILED)
                    }
            )
    )
    })
    ResponseEntity<ResponseDTO<EventResponseDTO>> addEvent(
            @Valid @Schema(implementation = EventCreateRequestDTO.class) @RequestPart("eventCreateRequestDTO") EventCreateRequestDTO eventCreateRequestDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException;



    @Operation(
            summary = "이벤트 수정 기능",
            description = """
                    수정하고자 하는 필드만 넣으면 됩니다. \n
                    데이터 전달은 multipart/form-data이며 'eventUpdateRequestDTO'는 JSON 문자열 형태로 전송해야 합니다.\n
                    - receiveInfo: 참석자 정보 수집 여부\n
                    - receiveName: 참석자 이름 수집 여부\n
                    - receiveGender: 참석자 성별 수집 여부\n
                    - receivePhoneNumber: 참석자 전화번호 수집 여부\n
                    - receiveTotalCount: 참석자 본인 포함 동행인원 수집 여부\n
                    - receiveSNSId: 참석자 SNS ID 수집 여부\n
                    - receiveMoney: 예약금 설정 여부 \n
                    
                    예시)\n
                    ``` json
                    {
                      "receiveInfo": true,
                      "receiveGender": true
                    } 
                    ```
                    \n
                    → 참석자 정보 수집을 활성화하고, 성별만 수집하도록 설정됩니다.\n
                    → 나머지 receive 항목은 명시하지 않으면 기존 값이 유지됩니다.
                    """
    )

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이벤트가 수정되었습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "SUCCESS_UPDATE_EVENT",
                              "message": "이벤트를 수정했습니다.",
                              "data": {
                                "eventId": 8,
                                "title": "이벤트 제목",
                                "content": "내용입니다.",
                                "images": [
                                  "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/event/20250622_224204_e8799c68-8ea2-4dea-bbd0-4e1922604b7e.jpg",
                                  "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/event/20250623_011130_b39d5833-6793-4fa9-a3b4-a7174a86e6c9.png",
                                  "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/event/20250623_011130_9dacc431-7b82-4c65-a582-870ab237f3f0.png"
                                ],
                                "liked": false,
                                "likes": 0,
                                "views": 2,
                                "startDate": "2025-06-23",
                                "endDate": "2025-06-24",
                                "receiveInfo": true,
                                "receiveName": true,
                                "receiveGender": true,
                                "receivePhoneNumber": true,
                                "receiveTotalCount": false,
                                "receiveSNSId": true,
                                "receiveMoney": false,
                                "depositAccount": "",
                                "depositAmount": 0
                              }
                            }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "예약금에 관련된 정보 누락",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "예약금에 관련된 정보 누락", value = SwaggerExamples.NEED_DEPOSIT_INFO)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 유저",
                    content = @Content(
                            mediaType = "application/json",
                            examples =
                            @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "잘못된 요청 (권한이 없는 일반 유저)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "권한 없는 유저", value = SwaggerExamples.CANNOT_ADD_EVENT_UNAUTHORIZED_MEMBER)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "S3에 이미지 등록 실패했을 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "s3에 이미지 등록을 실패했을 경우", value = SwaggerExamples.IMAGE_UPLOAD_FAILED),
                                    @ExampleObject(name = "s3에서 이미지 삭제를 실패한 경우", value = SwaggerExamples.IMAGE_DELETE_FAILED)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<EventResponseDTO>> updateEvent(@PathVariable Long eventId,
                                                              @RequestPart("eventUpdateRequestDTO") EventUpdateRequestDTO eventUpdateRequestDTO,
                                                              @RequestPart(value = "images", required = false) List<MultipartFile> images
    );

    @Operation(summary = "이벤트 참석 신청",
                description = """
                이벤트에서 필요한 정보 수집들을 제대로 입력하지 않으면 아래와 같은 에러가 발생합니다.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "이벤트 참석 성공", content = @Content(
                    mediaType = "application/json",
                    examples = {@ExampleObject(name = "참석 성공", value = """
                        {
                          "status": 201,
                          "code": "SUCCESS_CREATED_EVENT_ATTENDANCE",
                          "message": "이벤트 참여 폼이 성공적으로 작성되었습니다.",
                          "data": {
                            "eventId": 8,
                            "memberId": 156,
                            "name": "string",
                            "gender": "MALE",
                            "snsType": "string",
                            "snsId": "string",
                            "phoneNumber": "string",
                            "isPaid": true,
                            "totalMember": 0,
                            "createdAt": "2025-06-23T21:19:03.5783187"
                          }
                        }
                            """),
                            @ExampleObject(name = "입력할 필요가 없는 정보", value = """
                        {
                          "status": 201,
                          "code": "SUCCESS_CREATED_EVENT_ATTENDANCE",
                          "message": "이벤트 참여 폼이 성공적으로 작성되었습니다.",
                          "data": {
                            "eventId": 13,
                            "memberId": 156,
                            "name": null,
                            "gender": null,
                            "snsType": null,
                            "snsId": null,
                            "phoneNumber": null,
                            "isPaid": null,
                            "totalMember": null,
                            "createdAt": "2025-06-23T21:21:17.2462147"
                          }
                        }
                                    """)
                    }
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
            @ApiResponse(
                    responseCode = "404",
                    description = "이벤트 또는 유저 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 없음", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "이벤트 없음", value = SwaggerExamples.NOT_FOUND_EVENT)
                            }
                    )
            ),
            @ApiResponse(responseCode = "409", description = "이미 참석한 경우", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(name = "중복 참석", value = SwaggerExamples.ALREADY_ATTENDANCE_EVENT)
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
                            "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ddded007-dGroup%201000003259.png",
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
                            "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png",
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
            @ApiResponse(
                    responseCode = "404",
                    description = "이벤트 또는 유저 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 없음", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "이벤트 없음", value = SwaggerExamples.NOT_FOUND_EVENT)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<EventListResponseDTO>> getEventUpcomingSorted (@PathVariable String sort,
                                                                                @RequestParam(defaultValue = "0") Integer page,
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
                            "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ddded007-dGroup%201000003259.png",
                            "location": "경기도 파주",
                            "likes": 5,
                            "views": 0,
                            "startDate": "2025-06-17",
                            "endDate": "2025-06-17"
                          },
                          { ... }
                        ]
                      }
                    }
             """)
            )),
            @ApiResponse(
                    responseCode = "404",
                    description = "이벤트 또는 유저 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 없음", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "이벤트 없음", value = SwaggerExamples.NOT_FOUND_EVENT)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<EventListResponseDTO>> getEventNowSorted (        @PathVariable String sort,
                                                                                 @RequestParam(defaultValue = "0") Integer page,
                                                                                 @RequestParam(defaultValue = "10") Integer size);


    @Operation(summary = "종료된 이벤트",
            description = """
                    
                    (종료 날짜 기준 < 오늘) 종료가 된 이벤트를 보여줍니다. \n
                    sort 에는 popular / latest / region을 넣을 수 있으나 현재는 region은 구현되어있지 않습니다. \n
                    - 종료된 이벤트의 최신순과 인기순의 응답이 다릅니다. \n
                    - 종료된 이벤트 (인기순)의 경우에는 작년의 모든 이벤트를 불러옵니다. \n
                    
                    ✅ `sort=popular` \n
                    - 지난 1년간의 이벤트를 **월 단위로 그룹핑**하여, 각 월 내에서 좋아요 순으로 정렬합니다. \n
                    - `page`, `size`는 **월 그룹 단위 페이징**입니다. (예: `size=2`면 2개월치 그룹 반환) \n
                    - 예: `GET /events/past?sort=popular&page=1&size=2` \n
                    \n
                    ⚠️ 응답 구조 주의: \n
                    - `sort=latest` → `eventResponseDTOS` 필드 포함 (단일 이벤트 리스트) \n
                    - `sort=popular` → `groupedByMonth` 필드 포함 (월별 이벤트 그룹 리스트) \n
                    \n
                    응답에는 `page`, `size`, `totalSize`가 항상 포함됩니다.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "종료된 이벤트", content = @Content(
                    mediaType = "application/json",
                    examples = {@ExampleObject(name = "종료된 이벤트 (최신순)", value = """
                    {
                      "status": 200,
                      "code": "SUCCESS_GET_PAST_EVENT",
                      "message": "성공적으로 과거 이벤트를 조회했습니다.",
                      "data": {
                        "sort": "latest",
                        "page": 1,
                        "size": 10,
                        "totalSize": 3,
                        "eventResponseDTOS": [
                          {
                            "eventId": 7,
                            "title": "이벤트 제목",
                            "content": "내용입니다.",
                            "thumbImage": "",
                            "liked": false,
                            "location": "아직 정해지지 않음... 여기 elastic search 쓸 것 같음",
                            "likes": 0,
                            "views": 0,
                            "startDate": "2025-06-20",
                            "endDate": "2025-06-22",
                            "receiveInfo": false,
                            "receiveName": false,
                            "receiveGender": false,
                            "receivePhoneNumber": false,
                            "receiveTotalCount": false,
                            "receiveSNSId": false,
                            "receiveMoney": false
                          },
                          {
                            "eventId": 1,
                            "title": "이벤트 시작",
                            "content": "이게 바로 이트",
                            "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ddded007-dGroup%201000003259.png",
                            "liked": false,
                            "location": "경기도 파주",
                            "likes": 5,
                            "views": 0,
                            "startDate": "2025-06-17",
                            "endDate": "2025-06-17",
                            "receiveInfo": false,
                            "receiveName": false,
                            "receiveGender": false,
                            "receivePhoneNumber": false,
                            "receiveTotalCount": false,
                            "receiveSNSId": false,
                            "receiveMoney": false
                          }
                        ]
                      }
                    }
                    """),
                            @ExampleObject(name = "종료된 이벤트 (인기순)", value = """
                            {
                              "status": 200,
                              "code": "SUCCESS_GET_PAST_EVENT",
                              "message": "성공적으로 과거 이벤트를 조회했습니다.",
                              "data": {
                                "sort": "popular",
                                "page": 1,
                                "size": 10,
                                "totalSize": 2,
                                "groupedByMonth": [
                                  {
                                    "yearMonth": "2025-06",
                                    "events": [
                                      {
                                        "eventId": 1,
                                        "title": "이벤트 시작",
                                        "content": "이게 바로 이트",
                                        "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ddded007-dGroup%201000003259.png",
                                        "liked": false,
                                        "location": "경기도 파주",
                                        "likes": 5,
                                        "views": 0,
                                        "startDate": "2025-06-17",
                                        "endDate": "2025-06-17",
                                        "receiveInfo": false,
                                        "receiveName": false,
                                        "receiveGender": false,
                                        "receivePhoneNumber": false,
                                        "receiveTotalCount": false,
                                        "receiveSNSId": false,
                                        "receiveMoney": false
                                      },
                                      {
                                        "eventId": 7,
                                        "title": "이벤트 제목",
                                        "content": "내용입니다.",
                                        "thumbImage": "",
                                        "liked": false,
                                        "location": "아직 정해지지 않음... 여기 elastic search 쓸 것 같음",
                                        "likes": 0,
                                        "views": 0,
                                        "startDate": "2025-06-20",
                                        "endDate": "2025-06-22",
                                        "receiveInfo": false,
                                        "receiveName": false,
                                        "receiveGender": false,
                                        "receivePhoneNumber": false,
                                        "receiveTotalCount": false,
                                        "receiveSNSId": false,
                                        "receiveMoney": false
                                      }
                                    ]
                                  },
                                  {
                                    "yearMonth": "2025-04",
                                    "events": [
                                      {
                                        "eventId": 5,
                                        "title": "이벤트 제목",
                                        "content": "내용입니다.",
                                        "thumbImage": "",
                                        "liked": false,
                                        "location": "아직 정해지지 않음... 여기 elastic search 쓸 것 같음",
                                        "likes": 1,
                                        "views": 0,
                                        "startDate": "2025-04-20",
                                        "endDate": "2025-04-21",
                                        "receiveInfo": false,
                                        "receiveName": false,
                                        "receiveGender": false,
                                        "receivePhoneNumber": false,
                                        "receiveTotalCount": false,
                                        "receiveSNSId": false,
                                        "receiveMoney": false
                                      }
                                    ]
                                  }
                                ]
                              }
                            }
                    """

                   )}
            )),
            @ApiResponse(
                    responseCode = "404",
                    description = "이벤트 또는 유저 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 없음", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "이벤트 없음", value = SwaggerExamples.NOT_FOUND_EVENT)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<EventListResponseDTO>> getEventPastSorted(
            @PathVariable String sort,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size);




    @Operation(summary = "이벤트 신청자 명단 조회", description = "이벤트 신청자 명단을 조회합니다.\n" +
            "- null이면 - 로 표시됩니다. (null / false / true)\n" +
            "- 0이 아니라 null이어도 -로 표시됩니다.")
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
                                    "eventId": 2,
                                    "totalMember": 2,
                                    "eventAttendanceExportDTOS": [
                                      {
                                        "name": "이름1",
                                        "gender": "FEMALE",
                                        "phoneNumber": "-"
                                      },
                                      {
                                        "name": "이름이름",
                                        "gender": "None",
                                        "phoneNumber": "010-0000-0000"
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
                                    @ExampleObject(name = "유저 없음", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "이벤트 없음", value = SwaggerExamples.NOT_FOUND_EVENT)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "이벤트를 작성한 유저가 아님",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 권한 없음", value = SwaggerExamples.FORBIDDEN_EVENT_ACCESS)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<EventAttendanceExportListDTO>> getEventAttendanceList(@PathVariable Long eventId);

    @Operation(summary = "이벤트 신청자 명단 엑셀 다운로드",
            description = "신청자 명단이 엑셀 다운받을 수 잇는 링크로 반환됩니다. Download file을 누르면 됩니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "이벤트 신청자 명단 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "이벤트 신청자 명단 조회 성공", value = """
                        링크가 나옵니다.""")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "이벤트 또는 유저 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 없음", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "이벤트 없음", value = SwaggerExamples.NOT_FOUND_EVENT)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "이벤트를 작성한 유저가 아님",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 권한 없음", value = SwaggerExamples.FORBIDDEN_EVENT_ACCESS)
                            }
                    )
            )
    })
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
                          "data": "좋아요를 눌렀습니다"
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
                                    @ExampleObject(name = "유저 없음", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "이벤트 없음", value = SwaggerExamples.NOT_FOUND_EVENT)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 좋아요한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "이미 좋아요한 경우", value = SwaggerExamples.ALREADY_LIKED)
                    )
            )
    })
    ResponseEntity<ResponseDTO<String>> likeEvent(@PathVariable Long eventId);

    @Operation(summary = "이벤트 좋아요 취소\n",
            description = "이벤트에 좋아요를 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이벤트에 좋아요를 취소합니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS_DELETE_LIKE",
                                      "message": "좋아요를 취소했습니다.",
                                      "data": "좋아요를 취소했습니다"
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
                                    @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "존재하지 않는 이벤트", value = SwaggerExamples.NOT_FOUND_EVENT),
                                    @ExampleObject(name = "기존에 좋아요를 누르지 않았던 경우", value = SwaggerExamples.NOT_FOUND_LIKE)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<String>> deleteLikeEvent(@PathVariable Long eventId);

    @Operation(
            summary = "이벤트 댓글 작성\n",
            description = """
        이벤트에 댓글을 작성합니다.  
        최상위 댓글 작성 시 `parentCommentId`는 빈 문자열("")로 전달합니다.

        예시:
        ```json
        {
          "content": "string",
          "anonymous": true,
          "parentCommentId": ""
        }
        ```
        """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공적으로 댓글을 작성했습니다.\n" +
                    "- 댓글 레벨이 0이면 본인 글입니다. 1부터 대댓글",
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
                    description = "이벤트 / 댓글 / 유저 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 없음", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "이벤트 없음", value = SwaggerExamples.NOT_FOUND_EVENT),
                                    @ExampleObject(name = "댓글이 존재하지 않는 경우", value = SwaggerExamples.NOT_FOUND_COMMENT)
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
                    description = "이벤트 / 댓글 / 유저 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 없음", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "이벤트 없음", value = SwaggerExamples.NOT_FOUND_EVENT),
                                    @ExampleObject(name = "댓글이 존재하지 않는 경우", value = SwaggerExamples.NOT_FOUND_COMMENT)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "댓글을 작성한 유저가 아닙니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 권한 없음", value = SwaggerExamples.UNAUTHORIZED_MEMBER)
                            }
                    )
            )

    })
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
                                "images": ["https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png"],
                                "liked": true,
                                "likes": 2,
                                "views": 1,
                                "startDate": "2025-08-22",
                                "endDate": "2025-09-21",
                                "receiveInfo": true,
                                "receiveName": true,
                                "receiveGender": true,
                                "receivePhoneNumber": true,
                                "receiveSNSId": true,
                                "receiveMoney": true,
                                "depositAccount": "국민 XXXXXXXX",
                                "depositAmount": 10000
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
                                    @ExampleObject(name = "유저 없음", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "이벤트 없음", value = SwaggerExamples.NOT_FOUND_EVENT)
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
                                            value = SwaggerExamples.SUCCESS_BUT_EMPTY_LIST)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "이벤트 또는 유저 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 없음", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "이벤트 없음", value = SwaggerExamples.NOT_FOUND_EVENT)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<List<EventCommentTreeResponseDTO>>> getEventComments(@PathVariable Long eventId);




    ResponseEntity<ResponseDTO<Map<String, List<EventResponseDTO>>>> getMyEvents();

    @Operation(
            summary = "마이페이지 이벤트 조회 (upcoming)\n",
            description = """
                마이페이지에서 내가 좋아요를 누르거나 참여한 이벤트 중 '예정된 이벤트'를 조회합니다.
                정렬 기준은 아래와 같습니다:
                - `latest` (기본): 다가오는 이벤트 순으로 정렬
                - `oldest`: 먼 미래의 이벤트부터 정렬
                
                - 이미지를 등록하지 않으면 ("") 이렇게 나옵니다. 이 값은 null이 아닌 빈 문자열입니다.
                - 예정된 이벤트에는, dday 필드가 존재합니다.
                """
    )
    @ApiResponses( value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "마이페이지 이벤트 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "마이페이지 이벤트 조회 성공",
                                            description = """
                                                            이미지를 등록하지 않으면 ("") 이렇게 나옵니다.
                                                            이 값은 null이 아닌 빈 문자열입니다.
                                                            """,
                                            value = """
                                                {
                                                  "status": 200,
                                                  "code": "SUCCESS_GET_MY_PAGE_EVENTS",
                                                  "message": "마이페이지의 이벤트를 성공적으로 조회했습니다",
                                                  "data": [
                                                    {
                                                      "eventId": 4,
                                                      "title": "이벤트 제목",
                                                      "content": "내용입니다.",
                                                      "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png",
                                                      "liked": false,
                                                      "location": "아직 정해지지 않음... 여기 elastic search 쓸 것 같음",
                                                      "likes": 1,
                                                      "views": 0,
                                                      "startDate": "2025-06-24",
                                                      "endDate": "2025-07-21",
                                                      "receiveInfo": false,
                                                      "receiveName": false,
                                                      "receiveGender": false,
                                                      "receivePhoneNumber": false,
                                                      "receiveTotalCount": false,
                                                      "receiveSNSId": false,
                                                      "receiveMoney": false,
                                                      "dday": "D-1"
                                                    },
                                                    {
                                                      "eventId": 12,
                                                      "title": "이벤트 제목",
                                                      "content": "내용입니다.",
                                                      "thumbImage": "",
                                                      "liked": false,
                                                      "location": "아직 정해지지 않음... 여기 elastic search 쓸 것 같음",
                                                      "likes": 0,
                                                      "views": 1,
                                                      "startDate": "2025-08-22",
                                                      "endDate": "2025-09-21",
                                                      "receiveInfo": false,
                                                      "receiveName": false,
                                                      "receiveGender": false,
                                                      "receivePhoneNumber": false,
                                                      "receiveTotalCount": false,
                                                      "receiveSNSId": false,
                                                      "receiveMoney": false,
                                                      "dday": "D-60"
                                                    }
                                                  ]
                                                }
                                    """),
                                    @ExampleObject(
                                            name = "빈 이벤트 마이페이지 글",
                                            value = SwaggerExamples.SUCCESS_BUT_EMPTY_LIST)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "이벤트 또는 유저 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 없음", value = SwaggerExamples.MEMBER_NOT_EXIST)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<List<EventResponseDTO>>> getMyPageEventsUpcoming(
            @PathVariable String sort
    );

    @Operation(
            summary = "마이페이지 이벤트 조회 (now)\n",
            description = """
                마이페이지에서 내가 좋아요를 누르거나 참여한 이벤트 중 '진행 중인 이벤트'를 조회합니다.
                정렬 기준은 아래와 같습니다:

                - `latest` (기본): 최근 시작한 이벤트 순으로 정렬
                - `oldest`: 오래 전에 시작된 이벤트 순으로 정렬
                
                - 이미지를 등록하지 않으면 ("") 이렇게 나옵니다. 이 값은 null이 아닌 빈 문자열입니다.
                """
    )
    @ApiResponses( value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "마이페이지 이벤트 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "마이페이지 이벤트 조회 성공",
                                            description = """
                                                            이미지를 등록하지 않으면 ("") 이렇게 나옵니다.
                                                            이 값은 null이 아닌 빈 문자열입니다.
                                                            """,
                                            value = """
                                                {
                                                  "status": 200,
                                                  "code": "SUCCESS_GET_MY_PAGE_EVENTS",
                                                  "message": "마이페이지의 이벤트를 성공적으로 조회했습니다",
                                                  "data": [
                                                    {
                                                      "eventId": 2,
                                                      "title": "string",
                                                      "content": "string",
                                                      "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png",
                                                      "liked": false,
                                                      "location": "string",
                                                      "likes": 2,
                                                      "views": 0,
                                                      "startDate": "2025-06-18",
                                                      "endDate": "2025-06-23",
                                                      "receiveInfo": false,
                                                      "receiveName": false,
                                                      "receiveGender": false,
                                                      "receivePhoneNumber": false,
                                                      "receiveTotalCount": false,
                                                      "receiveSNSId": false,
                                                      "receiveMoney": false
                                                    }
                                                  ]
                                                }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "빈 이벤트 마이페이지 글",
                                            value = SwaggerExamples.SUCCESS_BUT_EMPTY_LIST)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "이벤트 또는 유저 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 없음", value = SwaggerExamples.MEMBER_NOT_EXIST)
                            }
                    )
            )
    })

    ResponseEntity<ResponseDTO<List<EventResponseDTO>>> getMyPageEventsNow(
            @PathVariable String sort
    );

    @Operation(
            summary = "마이페이지 이벤트 조회 (past)\n",
            description = """
                마이페이지에서 내가 좋아요를 누르거나 참여한 이벤트 중 '종료된 이벤트'를 조회합니다.
                정렬 기준은 아래와 같습니다:

                - `latest` (기본): 최근에 종료된 이벤트 순으로 정렬
                - `oldest`: 오래 전에 종료된 이벤트 순으로 정렬
                
               
                - 이미지를 등록하지 않으면 ("") 이렇게 나옵니다. 이 값은 null이 아닌 빈 문자열입니다.
                """
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
                                            description = """
                                                            이미지를 등록하지 않으면 ("") 이렇게 나옵니다.
                                                            이 값은 null이 아닌 빈 문자열입니다.
                                                            """,
                                            value = """
                                                {
                                                  "status": 200,
                                                  "code": "SUCCESS_GET_MY_PAGE_EVENTS",
                                                  "message": "마이페이지의 이벤트를 성공적으로 조회했습니다",
                                                  "data": [
                                                    {
                                                      "eventId": 1,
                                                      "title": "이벤트 시작",
                                                      "content": "이게 바로 이트",
                                                      "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ddded007-dGroup%201000003259.png",
                                                      "liked": false,
                                                      "location": "경기도 파주",
                                                      "likes": 5,
                                                      "views": 0,
                                                      "startDate": "2025-06-17",
                                                      "endDate": "2025-06-17",
                                                      "receiveInfo": false,
                                                      "receiveName": false,
                                                      "receiveGender": false,
                                                      "receivePhoneNumber": false,
                                                      "receiveTotalCount": false,
                                                      "receiveSNSId": false,
                                                      "receiveMoney": false
                                                    },
                                                    {
                                                      "eventId": 5,
                                                      "title": "이벤트 제목",
                                                      "content": "내용입니다.",
                                                      "thumbImage": "",
                                                      "liked": false,
                                                      "location": "아직 정해지지 않음... 여기 elastic search 쓸 것 같음",
                                                      "likes": 1,
                                                      "views": 0,
                                                      "startDate": "2025-04-20",
                                                      "endDate": "2025-04-21",
                                                      "receiveInfo": false,
                                                      "receiveName": false,
                                                      "receiveGender": false,
                                                      "receivePhoneNumber": false,
                                                      "receiveTotalCount": false,
                                                      "receiveSNSId": false,
                                                      "receiveMoney": false
                                                    }
                                                  ]
                                                }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "빈 이벤트 마이페이지 글",
                                            value = SwaggerExamples.SUCCESS_BUT_EMPTY_LIST)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "이벤트 또는 유저 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 없음", value = SwaggerExamples.MEMBER_NOT_EXIST)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<List<EventResponseDTO>>> getMyPageEventsPast(
            @PathVariable String sort
    );

}
