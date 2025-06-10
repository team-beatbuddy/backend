package com.ceos.beatbuddy.domain.member.application;

import com.ceos.beatbuddy.domain.member.dto.*;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Random;

import static com.ceos.beatbuddy.domain.member.exception.MemberErrorCode.INVALID_VERIFICATION_CODE;
import static com.ceos.beatbuddy.domain.member.exception.MemberErrorCode.MEMBER_NOT_EXIST;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BusinessMemberService {
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public BusinessMemberResponseDTO businessMemberSignup(Long memberId, BusinessMemberDTO dto) {
        //인증번호 확인
        boolean verified = verifyCode(dto.getPhoneNumber(), dto.getVerificationCode());
        if (!verified) {
            throw new CustomException(INVALID_VERIFICATION_CODE);
        }

        //회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_EXIST));

        //회원 정보 업데이트
        member.saveVerify();
        member.setRealName(dto.getRealName());
        member.setBusinessMember();
        member.setPhoneNumber(dto.getPhoneNumber());
        member.setDateOfBirthAndGender(dto.getResidentRegistration());

        return BusinessMemberResponseDTO.toDTO(member);
    }

    // 인증번호 전송
    public VerificationCodeResponseDTO sendVerificationCode(String phoneNumber) {
        String code = String.format("%06d", new Random().nextInt(999999));
        redisTemplate.opsForValue().set("VERIF:" + phoneNumber, code, Duration.ofMinutes(5));
        log.info("전송된 인증 코드: " + code); // 테스트용 출력
        return new VerificationCodeResponseDTO(code);
    }

    // 인증번호 검증
    public boolean verifyCode(String phoneNumber, String inputCode) {
        String key = "VERIF:" + phoneNumber;
        String savedCode = redisTemplate.opsForValue().get(key);
        return savedCode != null && savedCode.equals(inputCode);
    }
}
