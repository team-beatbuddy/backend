package com.ceos.beatbuddy.domain.magazine.controller;

import com.ceos.beatbuddy.domain.magazine.dto.*;
import com.ceos.beatbuddy.global.SwaggerExamples;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MagazineApiDocs {
    @Operation(summary = "홈에 보이는 매거진, 작성 기능\n",
            description = """
                    admin과 business 멤버에 한해서만 매거진을 작성할 수 있도록 해두었습니다. (추후 변경 가능)
                    - 데이터 전달은 multipart/form-data이며, 'magazineRequestDTO'는 JSON 문자열 형태로 전송해야 합니다.
                    - pinned: 홈에 고정된 매거진 (null 이면 false)
                    - visible: 매거진이 보이는지 여부 (null 이면 false)
                    - sponsored: 스폰서 매거진인지 여부 (null 이면 false)
                    - picked: 비트버디 픽된 매거진인지 여부 (null 이면 false)
                    - orderInHome: 홈에서의 순서 (1부터 시작)
                        - pinned (고정된 매거진)인 경우, orderInHome(홈에서의 순서)가 1 이상이어야 합니다.\
                        - 만약 pinned 가 false 인데 orderInHome 을 넣었다면 자동으로 0으로 저장됩니다.
                    - eventId: 이 매거진이 속한 이벤트의 ID (선택 사항)
                    - thumbnailImage: 매거진의 썸네일 이미지 (현재까지는 선택 사항...)
                    - venueIds: 매거진에 포함된 장소들의 ID 리스트 (선택 사항)
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "매거진이 성공적으로 작성되었습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 201,
                              "code": "SUCCESS_CREATED_MAGAZINE",
                              "message": "매거진이 성공적으로 작성되었습니다.",
                              "data": {
                                "magazineId": 5,
                                "title": "string",
                                "content": "string",
                                "writerId": 156,
                                "imageUrls": [
                                  "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/magazine/20250702_035225_b7c88a0b-f4e0-490a-a318-fd29380b12c1.png"
                                ],
                                "views": 0,
                                "likes": 0,
                                "createdAt": "2025-07-02T03:52:26.5691433",
                                "isLiked": false
                              }
                            }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            examples = {@ExampleObject(name = "홈에서의 순서가 잘못되었습니다.", value = SwaggerExamples.INVALID_ORDER_IN_HOME),
                                    @ExampleObject(name = "홈에서의 순서가 중복되었습니다.", value = SwaggerExamples.DUPLICATE_ORDER_IN_HOME) }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "리소스 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples =
                                    {@ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                     @ExampleObject(name = "존재하지 않는 이벤트", value = SwaggerExamples.NOT_FOUND_EVENT) }
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
                                            value = SwaggerExamples.CANNOT_ADD_MAGAZINE_UNAUTHORIZED_MEMBER)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<MagazineDetailDTO>> addMagazine(
            @Valid @RequestPart("magazineRequestDTO") MagazineRequestDTO magazineRequestDTO,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage,
            @RequestPart(value = "images", required = false) List<MultipartFile> images);

    @Operation(summary = "홈에 보이는 매거진, 조회 기능\n",
            description = "매거진 리스트 조회 기능, 어드민의 요청 순서대로 정렬해서 전달합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "매거진 리스트를 성공적으로 조회했습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "매거진 리스트 조회 성공",
                                            value = """
                                            {
                                              "status": 200,
                                              "code": "SUCCESS_GET_MAGAZINE_LIST",
                                              "message": "매거진을 성공적으로 불러왔습니다.",
                                              "data": [
                                                {
                                                  "magazineId": 6,
                                                  "thumbImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/magazine/20250704_223455_99e8329b-486b-4aea-ae1b-e83b5c55e895.png",
                                                  "title": "매거진 1 작성작성성",
                                                  "content": "매거진 1 1111111",
                                                  "liked": false,
                                                  "sponsored": false,
                                                  "orderInHome": 1,
                                                  "picked": false,
                                                  "isAuthor": false
                                                },
                                                {
                                                  "magazineId": 1,
                                                  "thumbImageUrl": "",
                                                  "title": "제목",
                                                  "content": "내용",
                                                  "liked": false,
                                                  "orderInHome": 2,
                                                  "sponsored": false,
                                                  "picked": false,
                                                  "isAuthor": false
                                                }
                                              ]
                                            }
                                            """
                                    ),
                                    @ExampleObject(name = "빈 매거진 리스트", value = SwaggerExamples.SUCCESS_BUT_EMPTY_LIST)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "리소스를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "존재하지 않는 매거진", value = SwaggerExamples.NOT_FOUND_MAGAZINE)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<List<MagazineHomeResponseDTO>>> readMagazineList();

    @Operation(summary = "매거진 전체 조회\n",
            description = "매거진 전체 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매거진 리스트를 성공적으로 조회했습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "매거진 전체 조회 성공",
                                            value = """
                                                    {
                                                      "status": 200,
                                                      "code": "SUCCESS_GET_MAGAZINE_LIST",
                                                      "message": "매거진을 성공적으로 불러왔습니다.",
                                                      "data": {
                                                        "page": 1,
                                                        "size": 10,
                                                        "totalCount": 10,
                                                        "magazines": [
                                                          {
                                                            "magazineId": 22,
                                                            "title": "string",
                                                            "content": "string",
                                                            "writerId": 168,
                                                            "imageUrls": [],
                                                            "thumbImage": "",
                                                            "views": 0,
                                                            "likes": 0,
                                                            "visible": true,
                                                            "pinned": true,
                                                            "orderInHome": 2,
                                                            "createdAt": "2025-07-06T20:32:38.659096",
                                                            "liked": false,
                                                            "sponsored": true,
                                                            "picked": true,
                                                            "eventSimpleDTO": {
                                                              "eventId": 1,
                                                              "title": "이벤트 시작"
                                                            },
                                                            "venueSimpleDTOS": [
                                                              {
                                                                "venueId": 1,
                                                                "koreanName": "클럽 트립",
                                                                "englishName": "CLUB TRIP"
                                                              },
                                                              {
                                                                "venueId": 2,
                                                                "koreanName": "클럽 어스",
                                                                "englishName": "CLUB US"
                                                              },
                                                              {
                                                                "venueId": 3,
                                                                "koreanName": "플러스82",
                                                                "englishName": "PLUS82SEOUL"
                                                              }
                                                            ],
                                                            "isAuthor": false
                                                          },
                                                          {
                                                            "magazineId": 21,
                                                            "title": "string",
                                                            "content": "string",
                                                            "writerId": 168,
                                                            "imageUrls": [],
                                                            "thumbImage": "",
                                                            "views": 0,
                                                            "likes": 0,
                                                            "visible": true,
                                                            "pinned": true,
                                                            "orderInHome": 2,
                                                            "createdAt": "2025-07-06T20:19:15.398591",
                                                            "liked": false,
                                                            "sponsored": false,
                                                            "picked": false,
                                                            "eventSimpleDTO": null,
                                                            "venueSimpleDTOS": [],
                                                            "isAuthor": false
                                                          },
                                                          {
                                                            "magazineId": 7,
                                                            "title": "매거진 222222 작성작성성",
                                                            "content": "매거진 222222222222222",
                                                            "writerId": 168,
                                                            "imageUrls": [
                                                              "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/magazine/20250704_223543_68e20252-bce3-4b1d-892b-69f420c0cfa5.jpg",
                                                              "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/magazine/20250704_223543_16c0661f-9cea-46f6-a275-3d0d518f5ee2.jpg"
                                                            ],
                                                            "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/magazine/20250704_223543_68e20252-bce3-4b1d-892b-69f420c0cfa5.jpg",
                                                            "views": 0,
                                                            "likes": 0,
                                                            "visible": true,
                                                            "pinned": true,
                                                            "orderInHome": 2,
                                                            "createdAt": "2025-07-04T22:35:43.57882",
                                                            "liked": false,
                                                            "sponsored": false,
                                                            "picked": false,
                                                            "eventSimpleDTO": null,
                                                            "venueSimpleDTOS": [],
                                                            "isAuthor": false
                                                          }
                                                        ]
                                                      }
                                                    }
                                            """
                                    ),
                                    @ExampleObject(name = "빈 매거진 리스트", value = SwaggerExamples.SUCCESS_BUT_EMPTY_LIST)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (페이지 번호가 0 이하인 경우)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "잘못된 페이지 요청", value = SwaggerExamples.PAGE_OUT_OF_BOUNDS)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "리소스를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "존재하지 않는 매거진", value = SwaggerExamples.NOT_FOUND_MAGAZINE)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<MagazinePageResponseDTO>> readAllMagazines(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );

    @Operation(summary = "매거진 상세 조회\n",
            description = "매거진 상세 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매거진 리스트를 성공적으로 조회했습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "SUCCESS_GET_MAGAZINE_LIST",
                              "message": "매거진을 성공적으로 불러왔습니다.",
                              "data": {
                                "magazineId": 22,
                                "title": "string",
                                "content": "string",
                                "writerId": 168,
                                "imageUrls": [],
                                "thumbImage": "",
                                "views": 1,
                                "likes": 0,
                                "visible": true,
                                "pinned": true,
                                "orderInHome": 2,
                                "createdAt": "2025-07-06T20:32:38.659096",
                                "liked": false,
                                "sponsored": true,
                                "picked": true,
                                "eventSimpleDTO": {
                                  "eventId": 1,
                                  "title": "이벤트 시작"
                                },
                                "venueSimpleDTOS": [
                                  {
                                    "venueId": 1,
                                    "koreanName": "클럽 트립",
                                    "englishName": "CLUB TRIP"
                                  },
                                  {
                                    "venueId": 2,
                                    "koreanName": "클럽 어스",
                                    "englishName": "CLUB US"
                                  },
                                  {
                                    "venueId": 3,
                                    "koreanName": "플러스82",
                                    "englishName": "PLUS82SEOUL"
                                  }
                                ],
                                "isAuthor": false
                              }
                            }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "리소스를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "존재하지 않는 매거진", value = SwaggerExamples.NOT_FOUND_MAGAZINE)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<MagazineDetailDTO>> readDetailMagazine(@PathVariable Long magazineId);



    @Operation(summary = "매거진 수정\n",
            description = """
                    매거진 수정 기능입니다. admin과 business 멤버에 한해서만 매거진을 수정할 수 있습니다.
                    - 데이터 전달은 multipart/form-data이며, 'magazineRequestDTO'는 JSON 문자열 형태로 전송해야 합니다.
                    - thumbnailImage: 매거진의 썸네일 이미지 (선택 사항)
                    - images: 매거진의 이미지 리스트 (선택 사항)
                    - ⚠️ 관련 venues는 해당 리스트로 전체 교체됩니다.
                    - magazine의 총 이미지는 20장입니다.
                    """)
    @ApiResponse(
            responseCode = "200",
            description = "매거진이 성공적으로 수정되었습니다.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class),
                    examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "code": "SUCCESS_UPDATE_MAGAZINE",
                      "message": "매거진을 성공적으로 수정했습니다.",
                      "data": {
                        "magazineId": 3,
                        "title": "string",
                        "content": "string",
                        "writerId": 156,
                        "imageUrls": [
                          "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/magazine/20250708_164100_799d623c-c00e-46af-8f95-17ec994ec9f9.jpg",
                          "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/magazine/20250708_164100_3780705b-9378-4efb-a079-c9e58700a820.jpg",
                          "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/magazine/20250708_164442_dec25cc8-c31d-495d-9f64-ac34299d6c93.jpg",
                          "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/magazine/20250708_164443_9d9ec8e4-d0cb-40e8-9fdc-ae1893d4e5a6.jpg"
                        ],
                        "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/magazine/20250708_163759_0cf4e15b-dbd0-473e-a30f-0bb206d9f623.png",
                        "views": 0,
                        "likes": 0,
                        "createdAt": "2025-07-02T03:35:20.952583",
                        "liked": false,
                        "sponsored": false,
                        "picked": false,
                        "eventSimpleDTO": null,
                        "venueSimpleDTOS": [
                          {
                            "venueId": 1,
                            "koreanName": "클럽 트립",
                            "englishName": "CLUB TRIP"
                          },
                          {
                            "venueId": 2,
                            "koreanName": "클럽 어스",
                            "englishName": "CLUB US"
                          },
                          {
                            "venueId": 3,
                            "koreanName": "플러스82",
                            "englishName": "PLUS82SEOUL"
                          }
                        ],
                        "isAuthor": true
                      }
                    }
                                    """)
            )
    )
    ResponseEntity<ResponseDTO<MagazineDetailDTO>> updateMagazine(
            @PathVariable Long magazineId,
            @RequestPart("magazineRequestDTO") MagazineUpdateRequestDTO magazineRequestDTO,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage,
            @RequestPart(value = "images", required = false) List<MultipartFile> images);
}
