package com.ceos.beatbuddy.domain.member.application;

import com.ceos.beatbuddy.domain.member.constant.Role;
import com.ceos.beatbuddy.domain.member.dto.BusinessMemberDTO;
import com.ceos.beatbuddy.domain.member.dto.NicknameAndBusinessNameDTO;
import com.ceos.beatbuddy.domain.member.dto.NicknameDTO;
import com.ceos.beatbuddy.domain.member.dto.VerifyCodeDTO;
import com.ceos.beatbuddy.domain.member.dto.response.BusinessMemberResponseDTO;
import com.ceos.beatbuddy.domain.member.entity.BusinessInfo;
import com.ceos.beatbuddy.domain.member.entity.Carrier;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.config.DanalConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

import static com.ceos.beatbuddy.domain.member.exception.MemberErrorCode.VERIFICATION_CODE_EXPIRED;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BusinessMemberService {
    private final MemberService memberService;
    private final RedisTemplate<String, String> redisTemplate;
    private final DanalAuthService danalAuthService;
    private final OnboardingService onboardingService;
    private final MemberRepository memberRepository;


    /**
     * 임시 비즈니스 멤버 인증 Danal에 요청을 보내고 TID를 Redis에 저장
     * @param dto 비즈니스 멤버 DTO
     * @param memberId 회원 ID
     */
    public void verifyWithDanal(BusinessMemberDTO dto, Long memberId) {
        // 1분 내 중복 요청 체크
        String rateLimitKey = "danal:rate_limit:" + memberId;
        String lastRequestTimeStr = redisTemplate.opsForValue().get(rateLimitKey);
        
        if (lastRequestTimeStr != null) {
            try {
                long lastRequestTime = Long.parseLong(lastRequestTimeStr);
                long currentTime = System.currentTimeMillis();
                long elapsedSeconds = (currentTime - lastRequestTime) / 1000;
                long remainingSeconds = 60 - elapsedSeconds;
                
                if (remainingSeconds > 0) {
                    String message = String.format("인증 요청은 1분에 한 번만 가능합니다. %d초 후에 다시 시도해주세요.", remainingSeconds);
                    throw new CustomException(message);
                }
            } catch (NumberFormatException e) {
                // 잘못된 형태의 데이터가 저장된 경우, 해당 키를 삭제하고 진행
                redisTemplate.delete(rateLimitKey);
            }
        }
        
        Member member = memberService.validateAndGetMember(memberId);

        Carrier.Result carrier = Carrier.parse(dto.getTelCarrier());

        // Danal 인증 요청
        String response = danalAuthService.requestPhoneAuth(
                carrier.danalCode(),
                carrier.mvno(),
                BusinessMemberDTO.digitsOnly(dto.getPhoneNumber()),
                dto.getResidentRegistration(),
                dto.getRealName(),
                String.valueOf(member.getId())); // userId로 로그인 ID 사용

        // 응답 파싱
        var responseMap = DanalConfig.parseResponse(response);
        String returnCode = responseMap.get("RETURNCODE");
        String returnMsg = responseMap.get("RETURNMSG");
        String tid = responseMap.get("TID");

        // RETURNCODE 체크
        if (!"0000".equals(returnCode)) {
            throw new CustomException(returnMsg != null ? returnMsg : "다날 본인인증에 실패했습니다.");
        }

        // memberId를 키로 tid를 Redis에 저장 (5분 TTL)
        redisTemplate.opsForValue().set("danal:auth:" + memberId, tid, Duration.ofMinutes(5));
        
        // rate limit 키 저장 (1분 TTL)
        redisTemplate.opsForValue().set(rateLimitKey, String.valueOf(System.currentTimeMillis()), Duration.ofMinutes(1));
        
        log.info("Danal auth request successful - memberId: {}, tid: {}", memberId, tid);
    }

    /**
     * Danal OTP 인증 확인 및 비즈니스 멤버 가입 처리
     * @param memberId 회원 ID
     * @param dto 임시 저장할 비즈니스 멤버 정보
     * @return BusinessMemberResponseDTO
     */
    @Transactional
    public BusinessMemberResponseDTO confirmDanalAuth(Long memberId, VerifyCodeDTO dto) {
        // Redis에서 tid 조회
        String tid = redisTemplate.opsForValue().get("danal:auth:" + memberId);
        if (tid == null) {
            log.warn("Danal auth failed - No TID found in Redis for memberId: {}", memberId);
            throw new CustomException(VERIFICATION_CODE_EXPIRED);
        }
        
        log.info("Danal auth check - memberId: {}, Redis stored TID: {}", memberId, tid);

        // Danal OTP 확인 요청
        var authResultMap = danalAuthService.requestPhoneAuthResult(tid, dto.getCode());
        String returnCode = authResultMap.get("RETURNCODE");
        String returnMsg = authResultMap.get("RETURNMSG");

        if (!"0000".equals(returnCode)) {
            throw new CustomException(returnMsg != null ? returnMsg : "다날 본인인증 확인에 실패했습니다.");
        }

        String realName = authResultMap.get("NAME");
        String phoneNumber = authResultMap.get("PHONE");
        String residentRegistration = authResultMap.get("IDEN");
        String carrier = authResultMap.get("CARRIER");
        String responseTid = authResultMap.get("TID");
        
        // TID 일치 여부 확인 로그
        if (responseTid != null && responseTid.equals(tid)) {
            log.info("TID verification SUCCESS - memberId: {}, stored TID: {}, response TID: {}", 
                    memberId, tid, responseTid);
        } else {
            log.warn("TID verification MISMATCH - memberId: {}, stored TID: {}, response TID: {}", 
                    memberId, tid, responseTid);
        }

        // Redis 키 삭제 (1회성 사용)
        redisTemplate.delete("danal:auth:" + memberId);

        Member member = memberService.validateAndGetMember(memberId);

        // 주민등록번호 앞 7자리 저장
        member.setDateOfBirthAndGender(residentRegistration);

        // 비즈니스 멤버 정보 저장
        member.setRealName(realName);
        member.setRole(Role.BUSINESS_NOT); // 임시 비즈니스 멤버로 설정
        member.getBusinessInfo().savePhoneNumber(phoneNumber);
        member.getBusinessInfo().updateVerify(); // 인증 완료
        // 통신사 정보를 "SKT_알뜰폰" 형태로 저장
        String telCarrierDisplay = Carrier.convertDanalCarrierToDisplay(carrier);
        member.getBusinessInfo().saveTelCarrier(telCarrierDisplay);

        log.info("Danal auth confirmed successful - memberId: {}, tid: {}", memberId, tid);
        return BusinessMemberResponseDTO.toDTO(member);
    }


    @Transactional
    public BusinessMemberResponseDTO updateBusinessInfo(Long memberId, NicknameAndBusinessNameDTO dto) {
        Member member = memberService.validateAndGetMember(memberId);

        NicknameDTO nicknameDTO = NicknameDTO.builder()
                .nickname(dto.getNickname())
                .build();

        // 중복 검증 (중복이면 예외 발생)
        onboardingService.isDuplicate(memberId, nicknameDTO);

        // 유효성 검증 (불가능한 닉네임이면 예외 발생)
        onboardingService.isValidate(memberId, nicknameDTO);

        member.saveNickname(dto.getNickname()); // 저장
        member.getBusinessInfo().saveBusinessName(dto.getBusinessName());

        return BusinessMemberResponseDTO.toSetNicknameDTO(member);
    }


    // 비즈니스 멤버 승인 처리
    @Transactional
    public void approveBusinessMember(Long memberId, Long adminId) {
        Member admin = memberService.validateAndGetMember(adminId);
        if (!admin.isAdmin()) {
            throw new CustomException(MemberErrorCode.NOT_ADMIN);
        }

        Member businessMember = memberService.validateAndGetMember(memberId);

        if (businessMember.getRole() != Role.BUSINESS_NOT) {
            throw new CustomException(MemberErrorCode.NOT_BUSINESS_MEMBER);
        }

        businessMember.getBusinessInfo().setApproved(true);
        businessMember.setRole(Role.BUSINESS);
        memberRepository.save(businessMember);
    }

}
