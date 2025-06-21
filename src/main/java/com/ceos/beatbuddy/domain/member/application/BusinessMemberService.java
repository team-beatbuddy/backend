package com.ceos.beatbuddy.domain.member.application;

import com.ceos.beatbuddy.domain.member.dto.*;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Random;

import static com.ceos.beatbuddy.domain.member.exception.MemberErrorCode.*;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BusinessMemberService {
    private final MemberService memberService;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, BusinessMemberDTO> businessMemberTempRedisTemplate;
    private final OnboardingService onboardingService;


    @Transactional
    public BusinessMemberResponseDTO businessMemberSignup(Long memberId, VerifyCodeDTO dto) {
        String key = "verification_code:" + memberId;
        String savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            throw new CustomException(VERIFICATION_CODE_EXPIRED);  // 새 예외코드 필요
        }

        if (!verifyCode(memberId, dto.getCode())) {
            throw new CustomException(INVALID_VERIFICATION_CODE);
        }

        // 임시저장한 인증번호 키
        String tempKey = "temp_business_member:" + memberId;
        BusinessMemberDTO tempBusinessMemberDTO = businessMemberTempRedisTemplate.opsForValue().get(tempKey);

        if (tempBusinessMemberDTO == null) {
            throw new CustomException(TEMPORARY_MEMBER_INFO_NOT_FOUND);
        }

        Member member = memberService.validateAndGetMember(memberId);

        member.getBusinessInfo().saveVerify();
        member.setRealName(tempBusinessMemberDTO.getRealName());
        member.setBusinessMember();
        member.getBusinessInfo().savePhoneNumber(tempBusinessMemberDTO.getPhoneNumber());
        member.setDateOfBirthAndGender(tempBusinessMemberDTO.getResidentRegistration());

        return BusinessMemberResponseDTO.toDTO(member);
    }

    public void saveTempMemberInfo(BusinessMemberDTO dto, Long memberId) {
        businessMemberTempRedisTemplate.opsForValue().set(
                "temp_business_member:" + memberId, dto, Duration.ofMinutes(5));
    }

    public VerificationCodeResponseDTO sendVerificationCode(BusinessMemberDTO dto, Long memberId) {
        String code = String.format("%06d", new Random().nextInt(999999));

        redisTemplate.opsForValue().set(
                "verification_code:" + memberId, code, Duration.ofMinutes(1));

        saveTempMemberInfo(dto, memberId);

        log.info("Redis: Saved verification code for memberId: {} with code: {}", memberId, code);
        return new VerificationCodeResponseDTO(code);
    }

    public boolean verifyCode(Long memberId, String inputCode) {
        String savedCode = redisTemplate.opsForValue().get("verification_code:" + memberId);
        log.info("Redis: Retrieved code for memberId: {}. Stored code: {}, Provided code: {}", memberId, savedCode, inputCode);
        return savedCode != null && savedCode.equals(inputCode);
    }

    @Transactional
    public BusinessMemberResponseDTO setNicknameAndBusinessName(Long memberId, NicknameAndBusinessNameDTO dto) {
        Member member = memberService.validateAndGetMember(memberId);

        // 닉네임 중복과 가능한 닉네임인지 확인
        if ((onboardingService.isDuplicate(memberId, NicknameDTO.builder().nickname(dto.getNickname()).build())) && (onboardingService.isValidate(memberId, NicknameDTO.builder().nickname(dto.getNickname()).build()))) {
            member.saveNickname(dto.getNickname());
            member.getBusinessInfo().saveBusinessName(dto.getBusinessName());
        }

        return BusinessMemberResponseDTO.toSetNicknameDTO(member);
    }
}
