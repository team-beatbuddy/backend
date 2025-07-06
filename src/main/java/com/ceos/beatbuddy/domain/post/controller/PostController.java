package com.ceos.beatbuddy.domain.post.controller;

import com.ceos.beatbuddy.domain.post.application.PostService;
import com.ceos.beatbuddy.domain.post.dto.*;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    @Operation(summary = "게시물 생성 XXXXXXX 사용 안 함", description = "게시물을 생성합니다 (type: free/piece), 밑의 Post 관련 RequestDto들을 참고해"
            + "타입에 맞는 request를 채워주세요.")
    @Parameter()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시물 생성 성공"),
            @ApiResponse(responseCode = "400", description = "게시물 생성 실패")
    })
    public ResponseEntity<Post> addPost(
            @PathVariable String type,
            @RequestBody PostRequestDto requestDto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(postService.addPost(memberId, type, requestDto));
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
    @Operation(summary = "게시물 조회 XXXXXXXXXXXXXX 사용 안 함", description = "게시물을 조회합니다 (type: free/piece)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시물 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시물이 존재하지 않습니다.")
    })
    public ResponseEntity<Post> readPost(
            @PathVariable String type,
            @PathVariable Long postId) {
        return ResponseEntity.ok(postService.readPost(type, postId));
    }

    @GetMapping("/{type}/{postId}/new")
    public ResponseEntity<ResponseDTO<PostReadDetailDTO>> newReadPost(
            @PathVariable String type,
            @PathVariable Long postId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        PostReadDetailDTO result = postService.newReadPost(type, postId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_POST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_POST, result));
    }

    @GetMapping("/{type}")
    @Operation(summary = "전체 게시물 조회 XXXXXXXXXXXXx 사용 안 함)", description = "전체 게시물을 조회합니다 (type: free/piece)")
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


    @GetMapping("/{type}/sorted")
    public ResponseEntity<ResponseDTO<PostListResponseDTO>> readAllPostsSort(
            @PathVariable String type,
            @Parameter(description = "페이지 번호")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 당 요청할 게시물 개수")
            @RequestParam(defaultValue = "10") int size) {

        Long memberId = SecurityUtils.getCurrentMemberId();
        PostListResponseDTO result = postService.readAllPostsSort(memberId, type, page, size);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_POST_SORT_LIST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_POST_SORT_LIST, result));
    }

    @GetMapping("/hot")
    public ResponseEntity<ResponseDTO<List<PostPageResponseDTO>>> getHotPosts() {
        Long memberId = SecurityUtils.getCurrentMemberId();

        List<PostPageResponseDTO> result = postService.getHotPosts(memberId);

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
    @GetMapping("/my-page/scrap")
    public ResponseEntity<ResponseDTO<PostListResponseDTO>> getScrappedPosts(
            @RequestParam String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long memberId = SecurityUtils.getCurrentMemberId();
        PostListResponseDTO result = postService.getScrappedPostsByType(memberId, type, page, size);
        return ResponseEntity
                .status(SuccessCode.GET_SCRAPPED_POST_LIST.getStatus())
                .body(new ResponseDTO<>(SuccessCode.GET_SCRAPPED_POST_LIST, result));
    }

    @Override
    @GetMapping("/my")
    public ResponseEntity<ResponseDTO<PostListResponseDTO>> getMyPosts(
            @RequestParam String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long memberId = SecurityUtils.getCurrentMemberId();
        PostListResponseDTO result = postService.getMyPostsByType(memberId, type, page, size);

        return ResponseEntity
                .status(SuccessCode.GET_MY_POST_LIST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.GET_MY_POST_LIST, result));
    }

    @Override
    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseDTO<PostListResponseDTO>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size)  {
        Long memberId = SecurityUtils.getCurrentMemberId();
        PostListResponseDTO result = postService.getUserPostsByType(memberId, userId, type, page, size);
        return ResponseEntity
                .status(SuccessCode.GET_USER_POST_LIST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.GET_USER_POST_LIST, result));
    }


    @Operation(summary = "게시글 수정", description = "자유 게시판 또는 조각 모집 게시판의 게시글을 수정합니다.")
    @PatchMapping(
            value = "/{type}/{postId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseDTO<PostReadDetailDTO>> updatePost(
            @PathVariable String type,
            @PathVariable Long postId,
            @RequestPart("updatePostRequestDTO") UpdatePostRequestDTO updatePostRequestDTO,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();

        PostReadDetailDTO result = postService.updatePost(
                type,
                postId, memberId, updatePostRequestDTO, files, updatePostRequestDTO.getDeleteImageUrls());

        return ResponseEntity
                .status(SuccessCode.SUCCESS_UPDATE_POST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_UPDATE_POST, result));
    }

    @Override
    @GetMapping("/hashtags-search")
    public     ResponseEntity<ResponseDTO<PostListResponseDTO>> hashTagPostList(
            @RequestParam List<String> hashtags,
            @Parameter(description = "페이지 번호")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 당 요청할 게시물 개수")
            @RequestParam(defaultValue = "10") int size) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        PostListResponseDTO result = postService.getHashtagPosts(memberId, hashtags, page, size);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_POST_LIST_BY_HASHTAG.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_POST_LIST_BY_HASHTAG, result));
    }
}