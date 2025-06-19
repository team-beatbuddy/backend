package com.ceos.beatbuddy.domain.post.controller;

import com.ceos.beatbuddy.domain.magazine.dto.MagazineRequestDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineResponseDTO;
import com.ceos.beatbuddy.domain.post.dto.*;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ceos.beatbuddy.domain.post.application.PostService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Post Controller", description = "게시물 컨트롤러\n"
        + "사용자가 전반적인 게시물들을 추가, 조회, 삭제하는 로직이 있습니다.")
@RequestMapping("/post")

public class PostController implements PostApiDocs {
    private final PostService postService;

    @PostMapping("/{type}")
    @Operation(summary = "게시물 생성", description = "게시물을 생성합니다 (type: free/piece), 밑의 Post 관련 RequestDto들을 참고해"
            + "타입에 맞는 request를 채워주세요.")
    @Parameter()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시물 생성 성공"),
            @ApiResponse(responseCode = "400", description = "게시물 생성 실패")
    })
    public ResponseEntity<Post> addPost(
            @PathVariable String type,
            @RequestBody PostRequestDto requestDto) {
        return ResponseEntity.ok(postService.addPost(type, requestDto));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/new/{type}")
    public ResponseEntity<ResponseDTO<ResponsePostDto>> addNewPost(
            @PathVariable String type,
            @Valid @RequestPart("postCreateRequestDTO") PostCreateRequestDTO postCreateRequestDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images){

        Long memberId = SecurityUtils.getCurrentMemberId();
        ResponsePostDto result = postService.addNewPost(type, postCreateRequestDTO, memberId, images);


        return ResponseEntity
                .status(SuccessCode.SUCCESS_CREATE_POST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_CREATE_POST, result));
    }

    @GetMapping("/{type}/{postId}")
    @Operation(summary = "게시물 조회", description = "게시물을 조회합니다 (type: free/piece)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시물 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시물이 존재하지 않습니다.")
    })
    public ResponseEntity<Post> readPost(
            @PathVariable String type,
            @PathVariable Long postId) {
        return ResponseEntity.ok(postService.readPost(type, postId));
    }

    @GetMapping("/{type}")
    @Operation(summary = "전체 게시물 조회 (role이 추가되었습니다.)", description = "전체 게시물을 조회합니다 (type: free/piece)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 게시물 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시물이 존재하지 않습니다.")
    })
    public ResponseEntity<Page<ResponsePostDto>> readAllPosts(
            @PathVariable String type,
            @Parameter(description = "페이지 번호")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 당 요청할 게시물 개수")
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.readAllPosts(type, page, size));
    }

    @Operation(summary = "전체 게시물 조회, 최신순 / 인기순 정렬이 추가되었습니다.)", description = "전체 게시물을 조회합니다 (type: free/piece), (sort: latest/popular)")
    @ApiResponse(
            responseCode = "200",
            description = "게시글 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class),
                    examples = @ExampleObject(value = """
            {
              "status": 200,
              "code": "SUCCESS_GET_POST_SORT_LIST",
              "message": "게시글 목록이 성공적으로 조회되었습니다.",
              "data": {
                "totalPost": 42,
                "size": 10,
                "page": 0,
                "responseDTOS": [
                  {
                    "id": 1,
                    "title": "게시글 제목",
                    "content": "게시글 내용입니다.",
                    "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/post/thumbnail.jpg",
                    "role": "USER",
                    "likes": 12,
                    "scraps": 5,
                    "comments": 3,
                    "nickname": "홍길동",
                    "createAt": "2024-06-17"
                  },
                  {
                    "id": 2,
                    "title": "다른 게시글",
                    "content": "내용입니다.",
                    "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/post/another-thumb.jpg",
                    "role": "BUSINESS",
                    "likes": 8,
                    "scraps": 2,
                    "comments": 1,
                    "nickname": "비즈회원",
                    "createAt": "2024-06-16"
                  }
                ]
              }
            }
        """)
            )
    )
    @GetMapping("/{type}/sort/{sort}")
    public ResponseEntity<ResponseDTO<PostListResponseDTO>> readAllPosts(
            @PathVariable String type,
            @PathVariable String sort,
            @Parameter(description = "페이지 번호")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 당 요청할 게시물 개수")
            @RequestParam(defaultValue = "10") int size) {
        PostListResponseDTO result = postService.readAllPostsSort(type, sort, page, size);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_POST_SORT_LIST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_POST_SORT_LIST, result));
    }

    @GetMapping("/posts/hot")
    public ResponseEntity<ResponseDTO<List<PostPageResponseDTO>>> getHotPosts() {
        List<PostPageResponseDTO> result = postService.getHotPosts();

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_HOT_POSTS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_HOT_POSTS, result));
    }

    @DeleteMapping("/{type}/{postId}")
    @Operation(summary = "게시물 삭제", description = "게시물을 삭제합니다 (type: free/piece)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시물 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "게시물이 존재하지 않습니다.")
    })
    public ResponseEntity<Void> deletePost(
            @PathVariable String type,
            @PathVariable Long postId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        postService.deletePost(type, postId, memberId);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/{postId}/like")
    public ResponseEntity<ResponseDTO<String>> addPostLike(@PathVariable Long postId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        postService.likePost(postId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_LIKE_POST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_LIKE_POST, "좋아요를 눌렀습니다."));
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<ResponseDTO<String>> deletePostLike(@PathVariable Long postId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        postService.deletePostLike(postId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_DELETE_LIKE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_DELETE_LIKE, "좋아요를 취소했습니다."));
    }

    @Override
    @PostMapping("/{postId}/scrap")
    public ResponseEntity<ResponseDTO<String>> scrapPost(@PathVariable Long postId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        postService.scrapPost(postId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_SCRAP_POST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_SCRAP_POST, "스크랩을 완료했습니다."));
    }

    @Override
    @DeleteMapping("/{postId}/scrap")
    public ResponseEntity<ResponseDTO<String>> deleteScrapPost(@PathVariable Long postId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        postService.deletePostScrap(postId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_DELETE_SCRAP.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_DELETE_SCRAP, "스크랩을 취소했습니다."));
    }


    @Override
    @GetMapping("/my-page")
    public ResponseEntity<ResponseDTO<PostListResponseDTO>> getScrappedPosts(
            @RequestParam String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long memberId = SecurityUtils.getCurrentMemberId();
        PostListResponseDTO result = postService.getScrappedPostsByType(memberId, type, page, size);
        return ResponseEntity
                .status(SuccessCode.GET_SCRAPPED_POST_LIST.getStatus())
                .body(new ResponseDTO<>(SuccessCode.GET_SCRAPPED_POST_LIST, result));
    }

    @GetMapping("/my")
    public ResponseEntity<ResponseDTO<PostListResponseDTO>> getMyPosts(
            @RequestParam String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long memberId = SecurityUtils.getCurrentMemberId();
        PostListResponseDTO result = postService.getMyPostsByType(memberId, type, page, size);

        return ResponseEntity
                .status(SuccessCode.GET_MY_POST_LIST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.GET_MY_POST_LIST, result));
    }
}