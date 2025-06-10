package com.ceos.beatbuddy.domain.member.application;

import com.ceos.beatbuddy.domain.member.dto.*;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
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
    private final MemberRepository memberRepository;
    private final RedisTemplate<Long, String> redisTemplate;
    private final RedisTemplate<Long, BusinessMemberDTO> businessMemberTempRedisTemplate;

    @Transactional
    public BusinessMemberResponseDTO businessMemberSignup(Long memberId, VerifyCodeDTO  dto) {
        //인증번호 확인
        boolean verified = verifyCode(memberId, dto.getCode());

        if (!verified) {
            throw new CustomException(INVALID_VERIFICATION_CODE);
        }

        BusinessMemberDTO tempBusinessMemberDTO = businessMemberTempRedisTemplate.opsForValue().get(memberId);

        if (tempBusinessMemberDTO == null) {
            throw new CustomException(TEMPORARY_MEMBER_INFO_NOT_FOUND);
        }

        //회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_EXIST));

        //회원 정보 업데이트
        member.saveVerify();
        member.setRealName(tempBusinessMemberDTO.getRealName());
        member.setBusinessMember();
        member.setPhoneNumber(tempBusinessMemberDTO.getPhoneNumber());
        member.setDateOfBirthAndGender(tempBusinessMemberDTO.getResidentRegistration());

        return BusinessMemberResponseDTO.toDTO(member);
    }

    // 임시로 비즈니스 회원 정보 저장
    public void saveTempMemberInfo(BusinessMemberDTO dto, Long memberId) {
        // redis 에 임시저장
        businessMemberTempRedisTemplate.opsForValue().set(memberId, dto, Duration.ofMinutes(5));
    }

    // 인증번호 전송
    public VerificationCodeResponseDTO sendVerificationCode(BusinessMemberDTO dto, Long memberId) {
        String code = String.format("%06d", new Random().nextInt(999999));
        redisTemplate.opsForValue().set(memberId, code, Duration.ofMinutes(5));
        log.info("전송된 인증 코드: " + code); // 테스트용 출력

        // 임시 정보 저장
        saveTempMemberInfo(dto, memberId);

        return new VerificationCodeResponseDTO(code);
    }

    // 인증번호 검증
    public boolean verifyCode(Long memberId, String inputCode) {
        String savedCode = redisTemplate.opsForValue().get(memberId);
        return savedCode != null && savedCode.equals(inputCode);
    }
}
