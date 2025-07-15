package com.ceos.beatbuddy.domain.member.controller;

import com.ceos.beatbuddy.domain.member.dto.MemberBlockRequestDTO;
import com.ceos.beatbuddy.domain.member.dto.MemberProfileSummaryDTO;
import com.ceos.beatbuddy.domain.member.dto.MemberResponseDTO;
import com.ceos.beatbuddy.domain.member.dto.NicknameDTO;
import com.ceos.beatbuddy.global.SwaggerExamples;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
                                            value = SwaggerExamples.MEMBER_NOT_EXIST)
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
                                            value = SwaggerExamples.IMAGE_UPLOAD_FAILED)
                            }
                    )
            )
    })
    @PatchMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ResponseDTO<String>> uploadProfileImage(
            @RequestPart("image") MultipartFile image) throws IOException;

    @Operation(summary = "멤버 요약 프로필 조회", description = "게시판에서 프로필 클릭 시 닉네임, 프로필 이미지, 게시글 수, 팔로워/팔로잉 수 반환, memberId를 넣지 않으면 본인 프로필 요약본이 조회됨.")
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
                    responseCode = "400",
                    description = "잘못된 요청입니다. 멤버 ID가 null인 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "잘못된 멤버 ID",
                                            value = """
                                                    
                                                    """)
                            }
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
                                            value = SwaggerExamples.MEMBER_NOT_EXIST)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<MemberProfileSummaryDTO>> getProfileSummary(
            @PathVariable Long memberId
    );

    @Operation(summary = "닉네임 변경, 14일 이내 조건이 포함된 변경 API", description = "닉네임을 변경합니다. 14일 내 최대 2회 변경 가능")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "닉네임 변경 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples =
                            @ExampleObject(
                                    name = "닉네임 변경 성공",
                                    value = """
                                            {
                                              "status": 200,
                                              "code": "SUCCESS_UPDATE_NICKNAME",
                                              "message": "닉네임을 성공적으로 변경했습니다.",
                                              "data": {
                                                "memberId": 156,
                                                "nickname": "새로운닉네임"
                                              }
                                            }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청입니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "동일한 닉네임으로는 변경이 불가능합니다.",
                                            value = SwaggerExamples.SAME_NICKNAME),
                                    @ExampleObject(
                                            name = "닉네임 변경은 14일 내에 2번까지만 가능합니다. 14일 뒤에 변경해주세요.",
                                            value = SwaggerExamples.NICKNAME_CHANGE_LIMITED)
                            }
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
                                            value = SwaggerExamples.MEMBER_NOT_EXIST)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "동일한 시점에 닉네임 변경 시도가 발생했습니다. 잠시 후 다시 시도해주세요.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "닉네임 변경 충돌",
                                            value = """
                                                    
                                            {
                                                "status": 409,
                                                "error": "CONFLICT",
                                                "code": "NICKNAME_CONFLICT",
                                                "message": "동일한 시점에 닉네임 변경 시도가 발생했습니다. 잠시 후 다시 시도해주세요."
                                            }
                                                    """)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<MemberResponseDTO>> updateNickname(@Valid @RequestBody NicknameDTO nicknameDTO);

    @Operation(summary = "멤버 차단", description = "특정 멤버를 차단합니다. 차단된 멤버는 게시글, 댓글 등에서 보이지 않습니다.")
    @ApiResponses(value = {
    @ApiResponse(
            responseCode = "200",
            description = "멤버 차단 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class),
                    examples =
                    @ExampleObject(
                            name = "멤버 차단 성공",
                            value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS_BLOCK_MEMBER",
                                      "message": "멤버를 성공적으로 차단했습니다.",
                                      "data": "성공적으로 차단했습니다."
                                    }
                    """
                    )
            )
    ),
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청입니다. 자신을 차단하려는 시도입니다.",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "자신을 차단하려는 시도",
                                    value = SwaggerExamples.SELF_BLOCKING_ATTEMPT)
                    }
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
                                    value = SwaggerExamples.MEMBER_NOT_EXIST)
                    }
            )
    )
    })
    ResponseEntity<ResponseDTO<String>> blockMember(@Valid @RequestBody MemberBlockRequestDTO request);
}
