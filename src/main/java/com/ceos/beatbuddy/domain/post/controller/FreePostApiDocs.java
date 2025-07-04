package com.ceos.beatbuddy.domain.post.controller;


import com.ceos.beatbuddy.domain.post.dto.PostListResponseDTO;
import com.ceos.beatbuddy.global.SwaggerExamples;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

public interface FreePostApiDocs {

    /**
     * 검색어로 게시글을 검색합니다.
     *
     * @param keyword 검색어
     * @param page    페이지 번호 (기본값: 1)
     * @param size    페이지당 게시글 수 (기본값: 10)
     * @return 검색된 게시글 목록
     */
    @Operation(summary = "게시글 검색", description = "검색어로 게시글을 검색합니다.")
    @ApiResponse(responseCode = "200", description = "검색된 게시글 목록 반환",
    content = @Content(examples = {@ExampleObject(
            name = "게시글 검색 성공", description = "검색어로 게시글을 성공적으로 검색한 경우",
            value = """
                    {
                      "status": 200,
                      "code": "SUCCESS_POST_SEARCH",
                      "message": "포스트 검색을 성공적으로 했습니다.",
                      "data": {
                        "totalPost": 1,
                        "size": 10,
                        "page": 1,
                        "responseDTOS": [
                          {
                            "id": 535,
                            "title": "string",
                            "content": "string",
                            "role": "USER",
                            "likes": 0,
                            "scraps": 0,
                            "comments": 0,
                            "liked": false,
                            "scrapped": false,
                            "hasCommented": false,
                            "nickname": "BeatBuddy",
                            "createAt": "2025-07-04"
                          }
                        ]
                      }
                    }
                    """),
            @ExampleObject(name = "게시글 검색 성공, 비어있음", description = "비어있는 검색 결과",
            value = SwaggerExamples.SUCCESS_BUT_EMPTY_LIST)
    }))
    @ApiResponse(responseCode = "400", description = "잘못된 요청",
    content = @Content(examples = {@ExampleObject(
            name = "검색어가 2글자 미만인 경우",
            description = "검색어가 2글자 미만인 경우",
            value = SwaggerExamples.KEYWORD_TOO_SHORT),

            @ExampleObject(
            name = "검색어가 비어있는 경우", description = "검색어가 비어있는 경우", value = SwaggerExamples.EMPTY_KEYWORD)}))
    ResponseEntity<ResponseDTO<PostListResponseDTO>> searchPosts(
            @RequestParam (required = false) @NotNull(message = "검색 시, 키워드는 필수입니다.") @Size(min = 2, message = "2글자 이상 입력해야 합니다.") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    );
}
