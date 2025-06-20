package com.ceos.beatbuddy.domain.member.controller;

import com.ceos.beatbuddy.domain.member.dto.MemberProfileSummaryDTO;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MemberApiDocs {
    @Operation(
            summary = "멤버 프로필 사진 업로드\n",
            description = "멤버 프로필 사진을 업로드합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "프로필 사진 업로드 완료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples =
                            @ExampleObject(
                                    name = "프로필 사진 업로드 완료",
                                    value = """
                                            {
                                              "status": 200,
                                              "code": "SUCCESS_UPLOAD_PROFILE_IMAGE",
                                              "message": "성공적으로 프로필 사진을 추가했습니다.",
                                              "data": "프로필 사진 업로드 완료"
                                            }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "유저가 존재하지 않습니다.",
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
    @PatchMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ResponseDTO<String>> uploadProfileImage(
            @RequestPart("image") MultipartFile image) throws IOException;

    @Operation(summary = "멤버 요약 프로필 조회", description = "게시판에서 프로필 클릭 시 닉네임, 프로필 이미지, 게시글 수, 팔로워/팔로잉 수 반환")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "멤버 요약 프로필 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples =
                            @ExampleObject(
                                    name = "멤버 요약 프로필 조회",
                                    value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS_GET_PROFILE_SUMMARY",
                                      "message": "프로필 요약 조회를 성공했습니다.",
                                      "data": {
                                        "memberId": 156,
                                        "nickname": "길동hong",
                                        "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/member/01e2e094-3--.png",
                                        "role": "BUSINESS",
                                        "postCount": 512,
                                        "followerCount": 2,
                                        "followingCount": 1
                                      }
                                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "유저가 존재하지 않습니다.",
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
    ResponseEntity<ResponseDTO<MemberProfileSummaryDTO>> getProfileSummary();
}
