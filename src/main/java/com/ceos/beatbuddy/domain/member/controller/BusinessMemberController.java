package com.ceos.beatbuddy.domain.member.controller;

import com.ceos.beatbuddy.domain.member.application.BusinessMemberService;
import com.ceos.beatbuddy.domain.member.dto.BusinessMemberDTO;
import com.ceos.beatbuddy.domain.member.dto.NicknameAndBusinessNameDTO;
import com.ceos.beatbuddy.domain.member.dto.VerifyCodeDTO;
import com.ceos.beatbuddy.domain.member.dto.response.BusinessMemberResponseDTO;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business")
@RequiredArgsConstructor
@Tag(name = "Business Controller", description = "비즈니스 사용자 컨트롤러\n")
//        + "현재는 회원가입 관련 로직만 작성되어 있습니다\n"
//        + "추후 사용자 상세 정보, 아카이브를 조회하는 기능이 추가될 수 있습니다")
public class BusinessMemberController {
    private final BusinessMemberService businessMemberService;

    @PostMapping("/verify-code")
    @Operation(summary = "비즈니스 사용자 인증 코드 요청")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 코드 요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "요청한 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "요청은 1분에 한 번만 가능합니다"),

    })
    public ResponseEntity<ResponseDTO<Void>> verifyCodeForBusiness(@Valid @RequestBody BusinessMemberDTO dto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        businessMemberService.verifyWithDanal(dto, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_BUSINESS_VERIFY_CODE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_BUSINESS_VERIFY_CODE, null));
    }

    @PostMapping("/verify")
    @Operation(summary = "비즈니스 사용자 인증 코드 확인")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 코드 확인 성공",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BusinessMemberResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "요청한 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "417", description = "인증 코드가 만료되었습니다"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ResponseDTO<BusinessMemberResponseDTO>> verifyBusinessMember (@Valid @RequestBody VerifyCodeDTO dto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        BusinessMemberResponseDTO result = businessMemberService.confirmDanalAuth(memberId, dto);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_BUSINESS_VERIFY.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_BUSINESS_VERIFY, result));
    }

    @PostMapping("/settings")
    @Operation(summary = "비즈니스 사용자 닉네임 및 사업자명 설정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "닉네임 및 사업자명 설정 성공",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BusinessMemberResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "요청한 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ResponseDTO<BusinessMemberResponseDTO>> setNicknameAndBusinessName(@Valid @RequestBody NicknameAndBusinessNameDTO dto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        BusinessMemberResponseDTO result = businessMemberService.updateBusinessInfo(memberId, dto);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_BUSINESS_SETTINGS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_BUSINESS_SETTINGS, result));
    }

}
