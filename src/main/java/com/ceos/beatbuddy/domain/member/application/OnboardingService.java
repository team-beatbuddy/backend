package com.ceos.beatbuddy.domain.member.application;

import com.ceos.beatbuddy.domain.member.constant.Region;
import com.ceos.beatbuddy.domain.member.dto.*;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberGenreRepository;
import com.ceos.beatbuddy.domain.member.repository.MemberMoodRepository;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnboardingService {
    private final MemberService memberService;
    private final MemberGenreRepository memberGenreRepository;
    private final MemberMoodRepository memberMoodRepository;
    private final MemberRepository memberRepository;

    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9가-힣._]*$");

    public OnboardingResponseDto isOnboarding(Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        return getOnboardingMap(member);
    }

    @Transactional
    public MemberResponseDTO saveMemberConsent(Long memberId, MemberConsentRequestDTO memberConsentRequestDTO) {
        Member member = memberService.validateAndGetMember(memberId);

        member.saveConsents(memberConsentRequestDTO.getIsLocationConsent(),
                memberConsentRequestDTO.getIsMarketingConsent());

        memberRepository.save(member);
        return MemberResponseDTO.builder()
                .memberId(member.getId())
                .loginId(member.getLoginId())
                .nickname(member.getNickname())
                .isLocationConsent(member.getIsLocationConsent())
                .isMarketingConsent(member.getIsMarketingConsent())
                .build();
    }

    public Boolean isTermConsent(Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        if (member.getIsLocationConsent() && member.getIsMarketingConsent()) {
            return true;
        }
        return false;
    }

    private OnboardingResponseDto getOnboardingMap(Member member) {
        OnboardingResponseDto responseDto = new OnboardingResponseDto();

        if (memberGenreRepository.existsByMember(member)) {
            responseDto.setGenre();
        }
        if (memberMoodRepository.existsByMember(member)) {
            responseDto.setMood();
        }

        if (memberRepository.existsRegionsById(member.getId())) {
            responseDto.setRegion();
        }
        return responseDto;
    }


    public Boolean isDuplicate(Long memberId, NicknameDTO nicknameDTO) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));
        String nickname = nicknameDTO.getNickname();

        if (memberRepository.existsDistinctByNickname(nickname)) {
            throw new CustomException(MemberErrorCode.NICKNAME_ALREADY_EXIST);
        }

        return true;
    }

    public Boolean isValidate(Long memberId, NicknameDTO nicknameDTO) {
        Member member = memberService.validateAndGetMember(memberId);

        String nickname = nicknameDTO.getNickname();
        if (nickname.length() > 12) {
            throw new CustomException(MemberErrorCode.NICKNAME_OVER_LENGTH);
        }
        if (nickname.matches(".*\\s+.*")) {
            throw new CustomException(MemberErrorCode.NICKNAME_SPACE_EXIST);
        }
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new CustomException(MemberErrorCode.NICKNAME_SYMBOL_EXIST);
        }
        return true;
    }



    @Transactional
    public MemberResponseDTO saveNickname(Long memberId, NicknameDTO nicknameDTO) {
        Member member = memberService.validateAndGetMember(memberId);

        String nickname = nicknameDTO.getNickname();
        member.saveNickname(nickname);

        return MemberResponseDTO.builder()
                .memberId(member.getId())
                .loginId(member.getLoginId())
                .nickname(member.getNickname())
                .isLocationConsent(member.getIsLocationConsent())
                .isMarketingConsent(member.getIsMarketingConsent())
                .build();
    }


    @Transactional
    public MemberResponseDTO saveRegions(Long memberId, RegionRequestDTO regionRequestDTO) {
        Member member = memberService.validateAndGetMember(memberId);

        List<Region> regions = Arrays.stream(regionRequestDTO.getRegions().split(","))
                .map(Region::fromText)
                .collect(Collectors.toList());
        member.saveRegions(regions);

        return MemberResponseDTO.builder()
                .memberId(member.getId())
                .loginId(member.getLoginId())
                .nickname(member.getNickname())
                .isLocationConsent(member.getIsLocationConsent())
                .isMarketingConsent(member.getIsMarketingConsent())
                .build();
    }

}
