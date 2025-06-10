package com.ceos.beatbuddy.domain.member.controller;

import com.ceos.beatbuddy.domain.member.dto.*;
import com.ceos.beatbuddy.global.ResponseTemplate;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface BusinessMemberApiDocs {
    @Operation(
            summary = "비즈니스 사용자 전화번호, 주민등록번호 앞 7자리, 실명을 입력하여 본인 인증을 진행합니다.  \n" +
                    "인증번호 요청",
            description = "테스트 과정입니다. 인증번호를 응답합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "인증번호 반환, 인증번호 유효기간은 3분입니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                        {
                          "status": 200,
                          "code": "SUCCESS_BUSINESS_VERIFY_CODE",
                          "message": "인증번호가 성공적으로 발급되었습니다.",
                          "data": {
                            "code": "123456"
                          }
                        }
                        """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 유효하지 않음 (필수값 누락 등)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "status": 400,
                              "error": "BAD_REQUEST",
                              "code": "BAD_REQUEST",
                              "message": "잘못된 요청입니다.",
                              "errors": {
                                "realName": "실명은 필수입니다.",
                                "phoneNumber": "전화번호는 필수입니다.",
                                "telCarrier": "통신사는 필수입니다.",
                                "residentRegistration": "주민번호 앞 7자리는 필수입니다."
                              }
                            }
                        """)
                    )
            )
    })
    ResponseEntity<ResponseDTO<VerificationCodeResponseDTO>> verifyCodeForBusiness(@Valid @RequestBody BusinessMemberDTO dto);
    @Operation(
            summary = "인증번호의 일치 여부를 확인합니다. 현재는 응답으로만 전해집니다. 문자 X",
            description = "응답 받은 인증번호를 입력해주세요."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "본인 인증 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value= """
                                    {
                                        "status": 200,
                                        "code": "SUCCESS_BUSINESS_VERIFY",
                                        "message": "성공적으로 인증되었습니다",
                                        "data": {
                                            "realName": "이규민",
                                            "phoneNumber": "010-6875-5844",
                                            "role": "BUSINESS"
                                        }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 유효하지 않음 (필수값 누락 등)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                        {
                          "status": 400,
                          "error": "BAD_REQUEST",
                          "code": "BAD_REQUEST",
                          "message": "잘못된 요청입니다.",
                          "errors": {
                            "code": "인증번호는 필수입니다."
                          }
                        }
                        """)
                    )
            ),

            @ApiResponse(
            responseCode = "400",
            description = "잘못된 인증번호 입력 시",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                                "status": 400,
                                "error": "BAD_REQUEST",
                                "code": "INVALID_VERIFICATION_CODE",
                                "message": "인증번호가 올바르지 않습니다."
                            }
                        """)
                    )
            ),

            @ApiResponse(
                    responseCode = "417",
                    description = "인증번호가 만료되었을 때. 인증번호 유효기간은 3분입니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                                "status": 417,
                                "error": "EXPECTATION_FAILED",
                                "code": "VERIFICATION_CODE_EXPIRED",
                                "message": "인증번호가 만료되었습니다. 다시 생성해주세요."
                            }
                        """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "요청한 유저가 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)
                    )
            )
    })
    ResponseEntity<ResponseDTO<BusinessMemberResponseDTO>> verifyBusinessMember (@Valid @RequestBody VerifyCodeDTO dto);
    @Operation(
            summary = "닉네임과 비즈니스명을 세팅합니다.",
            description = "닉네임은 중복되지 않게, 설정을 맞춰서 입력되어야 합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "본인 인증 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value= """
                            {
                                "status": 200,
                                "code": "SUCCESS_BUSINESS_SETTINGS",
                                "message": "성공적으로 프로필 세팅을 완료했습니다.",
                                "data": {
                                    "realName": "이규민",
                                    "phoneNumber": "010-6875-5844",
                                    "role": "BUSINESS",
                                    "nickname": "호호롱"
                                }
                            }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 유효하지 않음 (필수값 누락 등)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                                "status": 400,
                                "error": "BAD_REQUEST",
                                "code": "BAD_REQUEST_VALIDATION",
                                "message": "요청 값이 유효하지 않습니다.",
                                "errors": {
                                    "businessName": "비즈니스명은 필수입니다.",
                                    "nickname": "닉네임은 필수입니다."
                                }
                            }
                        """)
                    )
            ),

            @ApiResponse(
                    responseCode = "409",
                    description = "중복된 닉네임인 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                                "status": 409,
                                "error": "CONFLICT",
                                "code": "NICKNAME_ALREADY_EXIST",
                                "message": "이미 존재하는 닉네임입니다."
                            }
                        """)
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "닉네임에 공백이 존재함",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                                "status": 404,
                                "error": "NOT_FOUND",
                                "code": "NICKNAME_SPACE_EXIST",
                                "message": "닉네임에 공백이 있습니다"
                            }
                        """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "12자 초과 닉네임",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                                "status": 404,
                                "error": "NOT_FOUND",
                                "code": "NICKNAME_OVER_LENGTH",
                                "message": "닉네임이 12자 초과입니다"
                            }
                        """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "요청한 유저가 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)
                    )
            )
    })
    ResponseEntity<ResponseDTO<BusinessMemberResponseDTO>> setNicknameAndBusinessName(@Valid @RequestBody NicknameAndBusinessNameDTO dto);
}
