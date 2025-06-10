package com.ceos.beatbuddy.domain.member.controller;

import com.ceos.beatbuddy.domain.member.dto.BusinessMemberDTO;
import com.ceos.beatbuddy.domain.member.dto.BusinessMemberResponseDTO;
import com.ceos.beatbuddy.domain.member.dto.VerificationCodeResponseDTO;
import com.ceos.beatbuddy.domain.member.dto.VerifyCodeDTO;
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
                    description = "인증번호 반환",
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
            summary = "비즈니스 사용자 본인 확인",
            description = """
                verify-code 에서 받은 응답코드를 넣으면 인증됩니다.
        """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "본인 인증 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
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
                    responseCode = "404",
                    description = "요청한 유저가 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)
                    )
            )
    })
    ResponseEntity<ResponseDTO<BusinessMemberResponseDTO>> verifyBusinessMember (@Valid @RequestBody VerifyCodeDTO dto);
}
