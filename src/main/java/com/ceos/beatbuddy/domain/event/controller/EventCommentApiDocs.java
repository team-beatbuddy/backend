package com.ceos.beatbuddy.domain.event.controller;

import com.ceos.beatbuddy.domain.event.dto.EventCommentCreateRequestDTO;
import com.ceos.beatbuddy.domain.event.dto.EventCommentResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventCommentTreeResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventCommentUpdateDTO;
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
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface EventCommentApiDocs {
    @Operation(
            summary = "이벤트 문의 댓글 작성\n",
            description = """
            이벤트에 댓글을 작성합니다.
            최상위 댓글 작성 시 parentCommentId를 생략하거나 빈 문자열("")로 전달하세요.

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
                                "createdAt": "2025-06-18T02:08:31.4185432",
                                "isAuthor": true,
                                "isFollowing": false,
                                "writerId": 156,
                                "isStaff": true,
                                "isBlockedMember": false
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


    @Operation(summary = "이벤트 문의 댓글 수정\n",
            description = """
                이벤트에 댓글을 수정합니다.
                - 댓글은 댓글 ID(commentId)만으로 구분됩니다.
                - 댓글 계층은 자동으로 처리됩니다.
                - commentLevel: 계층 표현을 위한 필드입니다. 0은 최상위 댓글, 1부터 대댓글입니다. 실제 로직에는 영향을 주지 않습니다.
                - `content`와 `anonymous`을 수정할 수 있습니다.
                - 수정하고자 하는 필드만 입력하면 됩니다.
                """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 댓글을 수정했습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "SUCCESS_UPDATE_COMMENT",
                              "message": "성공적으로 댓글을 수정했습니다.",
                              "data": {
                                "commentId": 1,
                                "commentLevel": 1,
                                "content": "댓글 수정해봄",
                                "authorNickname": "익명",
                                "anonymous": true,
                                "createdAt": "2025-06-18T02:56:10.818788",
                                "isAuthor": true,
                                "isFollowing": false,
                                "writerId": 156,
                                "isStaff": true,
                                "isBlockedMember": false
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
                                    @ExampleObject(name = "댓글이 존재하지 않는 경우", value = SwaggerExamples.NOT_FOUND_COMMENT),
                                    @ExampleObject(name = "댓글이 이벤트에 속하지 않는 경우", value = SwaggerExamples.NOT_FOUND_COMMENT_IN_EVENT)

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
    ResponseEntity<ResponseDTO<EventCommentResponseDTO>> updateComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId,
            @Valid @RequestBody EventCommentUpdateDTO dto);

    @Operation(summary = "이벤트 문의 댓글 삭제\n",
            description = """
            이벤트에 댓글을 삭제합니다.
            - 댓글은 댓글 ID(commentId)만으로 구분됩니다.
            - 댓글 계층은 자동으로 처리됩니다.
            - commentLevel: 계층 표현을 위한 필드입니다. 0은 최상위 댓글, 1부터 대댓글입니다. 실제 로직에는 영향을 주지 않습니다.
            """)
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
            @PathVariable Long commentId);

    @Operation(
            summary = "이벤트 문의 댓글 전체 조회\n",
            description = """
                    각 댓글은 commentId 기준으로 계층 구조를 형성합니다. 대댓글은 parentCommentId를 통해 연결됩니다.
                    - commentLevel: 계층 표현을 위한 필드입니다. 0은 최상위 댓글, 1부터 대댓글입니다. 실제 로직에는 영향을 주지 않습니다.
                    """
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
                                          "content": "차단한 멤버의 댓글입니다.",
                                          "authorNickname": "익명 1",
                                          "anonymous": false,
                                          "createdAt": "2025-06-18T02:28:19.423835",
                                          "isAuthor": true,
                                          "isFollowing": false,
                                          "writerId": 156,
                                          "replies": [],
                                          "isStaff": true,
                                          "isBlockedMember": true
                                        },
                                        {
                                          "commentId": 1,
                                          "commentLevel": 0,
                                          "content": "차단한 멤버의 댓글입니다.",
                                          "authorNickname": "익명 2",
                                          "anonymous": true,
                                          "createdAt": "2025-06-18T02:08:31.418543",
                                          "isAuthor": false,
                                          "isFollowing": false,
                                          "writerId": 156,
                                          "isStaff": true,
                                          "isBlockedMember": true,
                                          "replies": [
                                            {
                                              "commentId": 1,
                                              "commentLevel": 1,
                                              "content": "대댓",
                                              "authorNickname": "익명 3",
                                              "anonymous": true,
                                              "isAuthor": false,
                                              "isFollowing": false,
                                              "writerId": 156,
                                              "isStaff": true,
                                              "isBlockedMember": false,
                                              "createdAt": "2025-06-18T02:56:10.818788"
                                            },
                                            {
                                              "commentId": 1,
                                              "commentLevel": 2,
                                              "content": "string",
                                              "authorNickname": "익명 4",
                                              "anonymous": true,
                                              "isAuthor": false,
                                              "isFollowing": false,
                                              "writerId": 156,
                                              "isStaff": true,
                                              "isBlockedMember": false,
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

}
