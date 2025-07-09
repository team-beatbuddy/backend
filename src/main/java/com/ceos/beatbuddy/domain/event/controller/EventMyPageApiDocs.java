package com.ceos.beatbuddy.domain.event.controller;

import com.ceos.beatbuddy.domain.event.dto.EventListResponseDTO;
import com.ceos.beatbuddy.global.SwaggerExamples;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

public interface EventMyPageApiDocs {

    @Operation(
            summary = "마이페이지 이벤트 조회 (upcoming)\n",
            description = """
                마이페이지에서 내가 좋아요를 누르거나 참여한 이벤트 중 '예정된 이벤트'를 조회합니다.
                정렬 기준은 아래와 같습니다:
                
                - region(홍대, 이태원, 강남_신사, 압구정_로데오, 기타)로 필터링할 수 있습니다.
                - `latest` (기본): 다가오는 이벤트 순으로 정렬
                
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
                                                      "dday": "D-1",
                                                      "isAuthor": true,
                                                      "region": "홍대"
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
                                                      "dday": "D-60",
                                                      "isAuthor": false,
                                                      "region": "홍대"
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
    ResponseEntity<ResponseDTO<EventListResponseDTO>> getMyPageEventsUpcoming(
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(
            summary = "마이페이지 이벤트 조회 (now)\n",
            description = """
                마이페이지에서 내가 좋아요를 누르거나 참여한 이벤트 중 '진행 중인 이벤트'를 조회합니다.
                정렬 기준은 아래와 같습니다:

                - region(홍대, 이태원, 강남_신사, 압구정_로데오, 기타)로 필터링할 수 있습니다.
                - `latest` (기본): 최근 시작한 이벤트 순으로 정렬
                
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
                                                      "receiveMoney": false,
                                                      "isAuthor": false,
                                                      "region": "홍대"
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

    ResponseEntity<ResponseDTO<EventListResponseDTO>> getMyPageEventsNow(
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(
            summary = "마이페이지 이벤트 조회 (past)\n",
            description = """
                마이페이지에서 내가 좋아요를 누르거나 참여한 이벤트 중 '종료된 이벤트'를 조회합니다.
                정렬 기준은 아래와 같습니다:
                
                - region(홍대, 이태원, 강남_신사, 압구정_로데오, 기타)로 필터링할 수 있습니다.
                - `latest` (기본): 최근에 종료된 이벤트 순으로 정렬
                
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
                                                      "receiveMoney": false,
                                                      "isAuthor": false,
                                                      "region": "홍대"
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
                                                      "receiveMoney": false,
                                                      "isAuthor": false,
                                                      "region": "홍대"
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
    ResponseEntity<ResponseDTO<EventListResponseDTO>> getMyPageEventsPast(
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    );




    @Operation(
            summary = "내가 작성한 이벤트 조회 (upcoming)\n",
            description = """
                내가 작성한 이벤트 중 '예정된 이벤트'를 조회합니다.
                정렬 기준은 아래와 같습니다:
                
                - region(홍대, 이태원, 강남_신사, 압구정_로데오, 기타)로 필터링할 수 있습니다.
                - `latest` (기본): 다가오는 이벤트 순으로 정렬
                
                - 이미지를 등록하지 않으면 ("") 이렇게 나옵니다. 이 값은 null이 아닌 빈 문자열입니다.
                - 예정된 이벤트에는, dday 필드가 존재합니다.
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "내가 작성한 이벤트 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "내가 작성한 이벤트 조회 성공",
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
                                                      "dday": "D-1",
                                                      "isAuthor": true,
                                                      "region": "홍대"
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
                                                      "dday": "D-60",
                                                      "isAuthor": true,
                                                      "region": "홍대"
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
    ResponseEntity<ResponseDTO<EventListResponseDTO>> getMyEventsUpcoming(
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    );


    @Operation(
            summary = "내가 작성한 이벤트 조회 (now)\n",
            description = """
                내가 작성한 이벤트 중 '진행 중인 이벤트'를 조회합니다.
                정렬 기준은 아래와 같습니다:

                - region(홍대, 이태원, 강남_신사, 압구정_로데오, 기타)로 필터링할 수 있습니다.
                - `latest` (기본): 최근 시작한 이벤트 순으로 정렬
                
                - 이미지를 등록하지 않으면 ("") 이렇게 나옵니다. 이 값은 null이 아닌 빈 문자열입니다.
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "내가 작성한 이벤트 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "내가 작성한 이벤트 조회 성공",
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
                                                      "receiveMoney": false,
                                                      "isAuthor": true,
                                                      "region": "홍대"
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
    ResponseEntity<ResponseDTO<EventListResponseDTO>> getMyEventsNow(
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(
            summary = "내가 작성한 이벤트 조회 (past)\n",
            description = """
                내가 작성한 이벤트 중 '종료된 이벤트'를 조회합니다.
                정렬 기준은 아래와 같습니다:
                
                - region(홍대, 이태원, 강남_신사, 압구정_로데오, 기타)로 필터링할 수 있습니다.
                - `latest` (기본): 최근에 종료된 이벤트 순으로 정렬
                
                - 이미지를 등록하지 않으면 ("") 이렇게 나옵니다. 이 값은 null이 아닌 빈 문자열입니다.
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "내가 작성한 이벤트 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "내가 작성한 이벤트 조회 성공",
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
                                                      "receiveMoney": false,
                                                      "isAuthor": true,
                                                      "region": "홍대"
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
                                                      "receiveMoney": false,
                                                      "isAuthor": true,
                                                      "region": "홍대"
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
    ResponseEntity<ResponseDTO<EventListResponseDTO>> getMyEventsPast(
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    );
}
