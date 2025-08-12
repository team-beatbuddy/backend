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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface EventApiDocs {
    @Operation(
            summary = "이벤트 작성 기능 (admin/business)",
            description = """
                    admin 과 business 멤버에 한해서만 이벤트를 작성할 수 있도록 해두었습니다. (추후 변경 가능)
                    데이터 전달은 multipart/form-data이며 'eventCreateRequestDTO'는 JSON 문자열 형태로 전송해야 합니다.
                    - receiveInfo: 참석자 정보 수집 여부
                    - receiveName: 참석자 이름 수집 여부
                    - receiveGender: 참석자 성별 수집 여부
                    - receivePhoneNumber: 참석자 전화번호 수집 여부
                    - receiveTotalCount: 참석자 본인 포함 동행인원 수집 여부
                    - receiveSNSId: 참석자 SNS ID 수집 여부
                    - receiveMoney: 예약금 설정 여부
                    - region: 홍대, 강남_신사, 압구정_로데오, 이태원, 기타
                    - entranceFee: 티켓 비용
                    - entranceNotice: 입장료 공지사항
                    - notice: 이벤트 관련 공지사항 (티켓 발급 날짜)
                    - isFreeEntrance: 무료 입장 여부
                    ```
                    isFreeEntrance == true이면 항상 entranceFee == 0 이어야 한다.
                    유료 전환 시(isFreeEntrance == false)에만 entranceFee 반영을 허용한다.
                    entranceFee만 단독으로 왔을 경우, 기존 isFreeEntrance == true 상태에서는 무시해야 한다.
                    ```
                    - location: 서울시 강남구 테헤란로 123, venue 를 고르셨다면 해당 장소의 주소를 넣어주세요.
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
                                    "startDate": "2025-08-22T00:00:00",
                                    "endDate": "2025-09-21T00:00:00",
                                    "receiveInfo": true,
                                    "receiveName": true,
                                    "receiveGender": true,
                                    "receivePhoneNumber": true,
                                    "receiveTotalCount": false,
                                    "receiveSNSId": true,
                                    "receiveMoney": false,
                                    "depositAccount": "",
                                    "depositAmount": 0,
                                    "isAuthor": true,
                                    "entranceFee": 20000,
                                    "entranceNotice": "입장료는 현장에서 결제해주세요.",
                                    "notice": "이벤트 관련 공지사항입니다.",
                                    "isFreeEntrance": false,
                                    "region": "홍대",
                                    "isAttending": false
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
                                            value = SwaggerExamples.NEED_DEPOSIT_INFO),
                                    @ExampleObject(
                                            name = "이미지 5장 초과",
                                            value = SwaggerExamples.TOO_MANY_IMAGES_5_EXAMPLE)
                                    }
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
                    수정하고자 하는 필드만 넣으면 됩니다.
                    데이터 전달은 multipart/form-data이며 'eventUpdateRequestDTO'는 JSON 문자열 형태로 전송해야 합니다
                    - receiveInfo: 참석자 정보 수집 여부
                    - receiveName: 참석자 이름 수집 여부
                    - receiveGender: 참석자 성별 수집 여부
                    - receivePhoneNumber: 참석자 전화번호 수집 여부
                    - receiveTotalCount: 참석자 본인 포함 동행인원 수집 여부
                    - receiveSNSId: 참석자 SNS ID 수집 여부
                    - receiveMoney: 예약금 설정 여부
                    - region: 홍대, 강남_신사, 압구정_로데오, 이태원, 기타
                    - entranceFee: 티켓 비용
                    - entranceNotice: 입장료 공지사항
                    - notice: 이벤트 관련 공지사항 (티켓 발급 날짜)
                    - isFreeEntrance: 무료 입장 여부
                    ```
                    isFreeEntrance == true이면 항상 entranceFee == 0 이어야 한다.
                    유료 전환 시(isFreeEntrance == false)에만 entranceFee 반영을 허용한다.
                    entranceFee만 단독으로 왔을 경우, 기존 isFreeEntrance == true 상태에서는 무시해야 한다.
                    ```
                    - location: 서울시 강남구 테헤란로 123, venue 를 고르셨다면 해당 장소의 주소를 넣어주세요.
                    예시)
                    ``` json
                    {
                      "receiveInfo": true,
                      "receiveGender": true
                    }
                    ```
                    
                    → 참석자 정보 수집을 활성화하고, 성별만 수집하도록 설정됩니다.
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
                                "startDate": "2025-06-23T00:00:00",
                                "endDate": "2025-06-24T00:00:00",
                                "receiveInfo": true,
                                "receiveName": true,
                                "receiveGender": true,
                                "receivePhoneNumber": true,
                                "receiveTotalCount": false,
                                "receiveSNSId": true,
                                "receiveMoney": false,
                                "depositAccount": "",
                                "depositAmount": 0,
                                "isAuthor": true,
                                "region": "홍대",
                                "isAttending": false,
                                "entranceFee": 20000,
                                "entranceNotice": "입장료는 현장에서 결제해주세요.",
                                "notice": "이벤트 관련 공지사항입니다.",
                                "isFreeEntrance": false
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
                            examples = {@ExampleObject(name = "예약금에 관련된 정보 누락", value = SwaggerExamples.NEED_DEPOSIT_INFO),
                                    @ExampleObject(name = "이미지 5장 초과", value = SwaggerExamples.TOO_MANY_IMAGES_5_EXAMPLE)}
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
                            @ExampleObject(name = "이름 누락", value = SwaggerExamples.MISSING_NAME_EXAMPLE),
                            @ExampleObject(name = "성별 누락", value = SwaggerExamples.MISSING_GENDER_EXAMPLE),
                            @ExampleObject(name = "전화번호 누락", value = SwaggerExamples.MISSING_PHONE_EXAMPLE),
                            @ExampleObject(name = "동행 인원 누락", value = SwaggerExamples.MISSING_TOTAL_COUNT_EXAMPLE),
                            @ExampleObject(name = "SNS 정보 누락", value = SwaggerExamples.MISSING_SNS_INFO_EXAMPLE),
                            @ExampleObject(name = "지불 여부 누락", value = SwaggerExamples.MISSING_PAYMENT_EXAMPLE),
                            @ExampleObject(name = "성별 입력 오류", value = SwaggerExamples.INVALID_GENDER_EXAMPLE)
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
            description = """
                앞으로의 이벤트를 보여줍니다.
                - sort 에는 popular / latest
                - region에는 (홍대, 이태원, 강남_신사, 압구정_로데오, 기타)를 넣을 수 있습니다.
                """)
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
                            "startDate": "2025-06-17T00:00:00",
                            "endDate": "2025-06-17T00:00:00",
                            "location": "경기도 파주",
                            "isAuthor": false,
                            "region": "홍대",
                            "isAttending": false,
                            "liked": false,
                            "isFreeEntrance": false
                          },
                          {
                            "eventId": 2,
                            "title": "string",
                            "content": "string",
                            "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png",
                            "likes": 1,
                            "views": 0,
                            "startDate": "2025-06-18T00:00:00",
                            "endDate": "2025-06-23T00:00:00",
                            "location": "서울시",
                            "isAuthor": false,
                            "region": "홍대",
                            "isAttending": true,
                            "liked": false,
                            "isFreeEntrance": false
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
    ResponseEntity<ResponseDTO<EventListResponseDTO>> getEventUpcomingSorted (        @PathVariable String sort,
                                                                                      @RequestParam(defaultValue = "1") Integer page,
                                                                                      @RequestParam(defaultValue = "10") Integer size,
                                                                                      @RequestParam(required = false) List<String> region);

    @Operation(summary = "진행되는 이벤트",
            description = """
                    시작 날짜 기준 <= 오늘 ,종료 날짜 기준 >= 오늘) 진행 중인 이벤트를 보여줍니다.
                    - sort는 기본적으로 latest입니다.
                    - region에는 (홍대, 이태원, 강남_신사, 압구정_로데오, 기타)를 넣을 수 있습니다.
                    """)
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
                            "startDate": "2025-06-17T00:00:00",
                            "endDate": "2025-06-17T00:00:00",
                            "isAuthor": false,
                            "region": "홍대",
                            "isAttending": false,
                            "liked": false,
                            "isFreeEntrance": false
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
    ResponseEntity<ResponseDTO<EventListResponseDTO>> getEventNowSorted (@RequestParam(defaultValue = "1") Integer page,
                                                                         @RequestParam(defaultValue = "10") Integer size,
                                                                         @RequestParam(required = false) List<String> region);
    @Operation(summary = "종료된 이벤트",
            description = """
                    (종료 날짜 기준 < 오늘) 종료가 된 이벤트를 보여줍니다.
                    - sort는 latest 입니다.
                    - region에는 (홍대, 이태원, 강남_신사, 압구정_로데오, 기타)를 넣을 수 있습니다.
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
                        "totalSize": 2,
                        "eventResponseDTOS": [
                          {
                            "eventId": 2,
                            "title": "string",
                            "content": "string",
                            "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png",
                            "liked": true,
                            "location": "string",
                            "likes": 2,
                            "views": 12,
                            "startDate": "2025-06-18T00:00:00",
                            "endDate": "2025-06-23T00:00:00",
                            "region": "홍대",
                            "isFreeEntrance": false,
                            "isAttending": true,
                            "isAuthor": true
                          },
                          {
                            "eventId": 1,
                            "title": "이벤트 시작",
                            "content": "이게 바로 이트",
                            "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ddded007-dGroup%201000003259.png",
                            "liked": false,
                            "location": "경기도 파주",
                            "likes": 5,
                            "views": 29,
                            "startDate": "2025-06-17T00:00:00",
                            "endDate": "2025-06-17T00:00:00",
                            "region": "강남_신사",
                            "isFreeEntrance": false,
                            "isAttending": true,
                            "isAuthor": true
                          }
                        ]
                      }
                    }
                    """)}
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
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) List<String> region);




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
                                        "eventId": 1,
                                        "title": "이벤트 시작",
                                        "content": "이게 바로 이트",
                                        "images": [],
                                        "liked": false,
                                        "location": "경기도 파주",
                                        "likes": 5,
                                        "views": 25,
                                        "startDate": "2025-06-17T00:00:00",
                                        "endDate": "2025-06-17T00:00:00",
                                        "receiveInfo": true,
                                        "receiveName": false,
                                        "receiveGender": false,
                                        "receivePhoneNumber": false,
                                        "receiveTotalCount": false,
                                        "receiveSNSId": false,
                                        "receiveMoney": false,
                                        "depositAccount": "국민 123-456-789",
                                        "depositAmount": 20000,
                                        "entranceFee": 20000,
                                        "entranceNotice": "입장료는 현장에서 결제해주세요.",
                                        "notice": "이벤트 관련 공지사항입니다.",
                                        "isFreeEntrance": false,
                                        "region": "강남_신사",
                                        "isAttending": true,
                                        "isAuthor": true
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
            summary = "이벤트 참석 삭제\n",
            description = """
                내가 참석한 이벤트 정보를 삭제합니다.
                해당 이벤트에 참석한 이력이 있는 경우에만 삭제가 가능합니다.
        
                ⚠️ 주의: 삭제 시, 해당 이벤트에 대한 내 참석 정보가 완전히 제거되며 복구할 수 없습니다.
        
                요청 경로: `DELETE /events/{eventId}/`
        
                예시:
                - DELETE /events/5/attendance → ID가 5인 이벤트 참석 정보 삭제
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "이벤트 참석 취소 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(
                                            name = "성공 예시",
                                            value = """
                                            {
                                              "status": 200,
                                              "code": "SUCCESS_DELETE_ATTENDANCE",
                                              "message": "이벤트 참석을 취소했습니다.",
                                              "data": "이벤트 참석을 취소했습니다."
                                            }
                                            """
                                    )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "리소스 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 없음", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "이벤트 없음", value = SwaggerExamples.NOT_FOUND_EVENT),
                                    @ExampleObject(name = "이벤트 참석 정보 없음", value = SwaggerExamples.ATTENDANCE_NOT_FOUND)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<String>> deleteEventAttendance(@PathVariable Long eventId);


    @Operation(
            summary = "이벤트 참석 정보 조회",
            description = "특정 이벤트에 대한 참석 정보를 조회합니다. 이 API는 이벤트 ID를 기반으로 해당 이벤트에 참석한 유저의 정보를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이벤트 참석 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 200,
                                  "code": "SUCCESS_GET_EVENT_ATTENDANCE",
                                  "message": "이벤트 참석 정보를 조회했습니다.",
                                  "data": {
                                    "eventId": 1,
                                    "memberId": 123,
                                    "name": "홍길동",
                                    "gender": "MALE",
                                    "phoneNumber": "010-1234-5678",
                                    "isPaid": true,
                                    "totalMember": 2,
                                    "createdAt": "2025-06-23T12:00:00"
                                  }
                                }
                                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "이벤트 또는 참석 정보가 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 없음", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "이벤트 없음", value = SwaggerExamples.NOT_FOUND_EVENT),
                                    @ExampleObject(name = "참석 정보 없음", value = SwaggerExamples.ATTENDANCE_NOT_FOUND)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<EventAttendanceResponseDTO>> getEventAttendance(@PathVariable Long eventId);

    @Operation(
            summary = "이벤트 참석 정보 수정 (사용하지 않음)\n",
            description = """
        내가 참석한 이벤트 정보를 수정합니다.
        수정 가능한 항목은 이벤트에서 설정한 수집 항목(receive 설정)에 따라 다르며,
        수정하지 않을 필드는 요청 본문에서 생략하면 됩니다.

        ⚠️ 주의: 해당 이벤트에 참석한 이력이 있는 경우에만 수정할 수 있으며, 
        이벤트에서 특정 정보를 수집하지 않는다면 해당 필드를 수정할 수 없습니다.

        요청 방식: `PATCH /events/{eventId}/attendance`

        요청 형식:
        - Content-Type: application/json
        - 예시:
        ```json
        {
          "name": "홍길동",
          "gender": "MALE",
          "phoneNumber": "010-1234-5678",
          "totalMember": 2,
          "snsType": "instagram",
          "snsId": "gildong_hong",
          "hasPaid": true
        }
        ```

        위 필드 중 일부만 보내 수정할 수도 있습니다.
        예: `{ "phoneNumber": "010-0000-0000" }`
        → 전화번호만 수정됨
        """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "이벤트 참석 정보 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                        {
                                          "status": 200,
                                          "code": "SUCCESS_UPDATE_ATTENDANCE",
                                          "message": "이벤트 참석 정보를 수정했습니다.",
                                          "data": {
                                            "eventId": 12,
                                            "memberId": 156,
                                            "name": "dlrbals",
                                            "gender": "MALE",
                                            "snsType": "Insta",
                                            "snsId": "@123",
                                            "phoneNumber": "010-2222-2222",
                                            "isPaid": true,
                                            "totalMember": 3,
                                            "createdAt": "2025-06-22T23:12:01.92143"
                                          }
                                        }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "입력값 누락 또는 잘못된 요청", content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "이름 누락", value = SwaggerExamples.MISSING_NAME_EXAMPLE),
                            @ExampleObject(name = "성별 누락", value = SwaggerExamples.MISSING_GENDER_EXAMPLE),
                            @ExampleObject(name = "전화번호 누락", value = SwaggerExamples.MISSING_PHONE_EXAMPLE),
                            @ExampleObject(name = "동행 인원 누락", value = SwaggerExamples.MISSING_TOTAL_COUNT_EXAMPLE),
                            @ExampleObject(name = "SNS 정보 누락", value = SwaggerExamples.MISSING_SNS_INFO_EXAMPLE),
                            @ExampleObject(name = "지불 여부 누락", value = SwaggerExamples.MISSING_PAYMENT_EXAMPLE),
                            @ExampleObject(name = "성별 입력 오류", value = SwaggerExamples.INVALID_GENDER_EXAMPLE)
                    }
            )),
            @ApiResponse(
                    responseCode = "404",
                    description = "리소스 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "유저 없음", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "이벤트 없음", value = SwaggerExamples.NOT_FOUND_EVENT),
                                    @ExampleObject(name = "이벤트 참석 정보 없음", value = SwaggerExamples.ATTENDANCE_NOT_FOUND)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<EventAttendanceResponseDTO>> updateEventAttendance(
            @PathVariable Long eventId,
            @RequestBody EventAttendanceUpdateDTO dto);

    @Operation(
            summary = "특정 기간 내 진행되는 이벤트 조회",
            description = """
                특정 기간 내에 진행되는 이벤트를 조회합니다.
                - 시작 날짜와 종료 날짜를 기준으로 이벤트를 필터링합니다.
                - 페이지네이션을 지원합니다.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "특정 기간 내 진행되는 이벤트 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class),
                    examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "code": "SUCCESS_GET_SEARCH_EVENT_LIST",
                      "message": "이벤트 검색을 성공적으로 했습니다.",
                      "data": {
                        "sort": "period",
                        "page": 1,
                        "size": 10,
                        "totalSize": 5,
                        "eventResponseDTOS": [
                          {
                            "eventId": 1,
                            "title": "이벤트 시작",
                            "content": "이게 바로 이트",
                            "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ddded007-dGroup%201000003259.png",
                            "liked": true,
                            "location": "경기도 파주",
                            "likes": 5,
                            "views": 29,
                            "startDate": "2025-06-17T00:00:00",
                            "endDate": "2025-06-17T00:00:00",
                            "region": "강남_신사",
                            "isAttending": true,
                            "isAuthor": false,
                            "isFreeEntrance": false
                          },
                          {
                            "eventId": 2,
                            "title": "string",
                            "content": "string",
                            "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png",
                            "liked": true,
                            "location": "string",
                            "likes": 2,
                            "views": 12,
                            "startDate": "2025-06-18T00:00:00",
                            "endDate": "2025-06-23T00:00:00",
                            "region": "홍대",
                            "isAttending": true,
                            "isAuthor": true,
                            "isFreeEntrance": true
                          }
                        ]
                      }
                    }
                        """)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 또는 입력값 누락",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "잘못된 날짜 형식", value = SwaggerExamples.INVALID_PARAMETER_DATE_TYPE),
                            @ExampleObject(name = "시작 날짜가 종료 날짜보다 늦은 경우", value = SwaggerExamples.INVALID_DATE_RANGE)
                    }
            )
    )
    ResponseEntity<ResponseDTO<EventListResponseDTO>> getSearchEventWithPeriod(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size);


    @Operation(summary = "이벤트 검색", description = """
            키워드를 통해 검색할 수 있습니다.
            - admin은 visible 이 False 인 것까지 확인 가능합니다.
            - 일반 유저는 visible 이 True 인 것만 확인 가능합니다.
            - 유료입장/무료입장 도 검색 가능합니다.
            - upcoming/now/past 의 카테고리로 분리되어 한번에 불러와집니다.
            """)
    @ApiResponse(
            responseCode = "200",
            description = "검색 성공",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                {
                  "status": 200,
                  "code": "EVENT_SEARCH_SUCCESS",
                  "message": "이벤트 검색을 성공적으로 했습니다.",
                  "data": {
                    "sort": "search",
                    "page": 1,
                    "size": 10,
                    "totalSize": 3,
                    "eventResponseDTOS": [
                      {
                        "eventId": 2,
                        "title": "string",
                        "content": "string",
                        "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png",
                        "liked": true,
                        "location": "string",
                        "likes": 2,
                        "views": 12,
                        "startDate": "2025-06-18T00:00:00",
                        "endDate": "2025-06-23T00:00:00",
                        "region": "홍대",
                        "isFreeEntrance": false,
                        "isAttending": true,
                        "isAuthor": true
                      },
                      {
                        "eventId": 12,
                        "title": "이벤트 제목",
                        "content": "내용입니다.",
                        "thumbImage": "",
                        "liked": false,
                        "location": "아직 정해지지 않음... 여기 elastic search 쓸 것 같음",
                        "likes": 1,
                        "views": 6,
                        "startDate": "2025-08-22T00:00:00",
                        "endDate": "2025-09-21T10:00:00",
                        "region": "홍대",
                        "isFreeEntrance": false,
                        "isAttending": true,
                        "isAuthor": true
                      },
                      {
                        "eventId": 14,
                        "title": "이벤트 제목",
                        "content": "내용입니다.",
                        "thumbImage": "",
                        "liked": false,
                        "location": "서울시 강남구 테헤란로 123, venue 를 고르셨다면 해당 장소의 주소를 넣어주세요.",
                        "likes": 0,
                        "views": 0,
                        "startDate": "2025-08-22T00:00:00",
                        "endDate": "2025-09-21T00:00:00",
                        "region": "홍대",
                        "isFreeEntrance": false,
                        "isAttending": false,
                        "isAuthor": true
                      }
                    ]
                  }
                }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "리소스 없음",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = SwaggerExamples.MEMBER_NOT_EXIST)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "검색 실패",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = SwaggerExamples.ELASTICSEARCH_SEARCH_FAILED)
            )
    )
    ResponseEntity<ResponseDTO<EventListResponseDTO>> searchEvents(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) java.time.LocalDateTime startDate,
            @RequestParam(required = false) java.time.LocalDateTime endDate
    );
}
