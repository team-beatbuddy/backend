package com.ceos.beatbuddy.domain.member.controller;

import com.amazonaws.Response;
import com.ceos.beatbuddy.domain.member.application.BusinessMemberService;
import com.ceos.beatbuddy.domain.member.dto.*;
import com.ceos.beatbuddy.global.ResponseTemplate;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class BusinessMemberController implements BusinessMemberApiDocs{
    private final BusinessMemberService businessMemberService;

    @Override
    @PostMapping("/verify-code")
    public ResponseEntity<ResponseDTO<VerificationCodeResponseDTO>> verifyCodeForBusiness(@Valid @RequestBody BusinessMemberDTO dto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        VerificationCodeResponseDTO result = businessMemberService.sendVerificationCode(dto, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_BUSINESS_VERIFY_CODE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_BUSINESS_VERIFY_CODE, result));
    }

    @Override
    @PostMapping("/verify")
    public ResponseEntity<ResponseDTO<BusinessMemberResponseDTO>> verifyBusinessMember (@Valid @RequestBody VerifyCodeDTO dto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        BusinessMemberResponseDTO result = businessMemberService.businessMemberSignup(memberId, dto);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_BUSINESS_VERIFY.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_BUSINESS_VERIFY, result));
    }

    @Override
    @PostMapping("/settings")
    public ResponseEntity<ResponseDTO<BusinessMemberResponseDTO>> setNicknameAndBusinessName(@Valid @RequestBody NicknameAndBusinessNameDTO dto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        BusinessMemberResponseDTO result = businessMemberService.setNicknameAndBusinessName(memberId, dto);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_BUSINESS_SETTINGS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_BUSINESS_SETTINGS, result));
    }

}
