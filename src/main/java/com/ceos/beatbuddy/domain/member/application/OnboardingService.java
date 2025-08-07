package com.ceos.beatbuddy.domain.member.application;

import com.ceos.beatbuddy.domain.member.constant.Region;
import com.ceos.beatbuddy.domain.member.dto.*;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.entity.PostProfileInfo;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberGenreRepository;
import com.ceos.beatbuddy.domain.member.repository.MemberMoodRepository;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.util.UploadUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final UploadUtil uploadUtil;

    @PersistenceContext
    private EntityManager entityManager;

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
        // 12글자 초과
        if (nickname.length() > 12) {
            throw new CustomException(MemberErrorCode.NICKNAME_OVER_LENGTH);
        }
        // 띄어쓰기 존재
        if (nickname.matches(".*\\s+.*")) {
            throw new CustomException(MemberErrorCode.NICKNAME_SPACE_EXIST);
        }
        // 중복 닉네임
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

        try {
            entityManager.flush(); // 실제 예외를 발생시켜 원인 확인
        } catch (Exception e) {
            e.printStackTrace(); // 콘솔에 root cause가 찍힘
            throw e;
        }

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


    // ============================ Member PostProfile ============================
    public void savePostProfile(Long memberId, PostProfileRequestDTO postProfileRequestDTO, MultipartFile postProfileImage) {
        Member member = memberService.validateAndGetMember(memberId);

        String postProfileNickname = postProfileRequestDTO.getPostProfileNickname();
        if (postProfileNickname == null || postProfileNickname.isEmpty()) {
            throw new CustomException("게시글 작성자의 닉네임은 필수입니다.");
        }

        // 프로필 사진 있으면 업로드
        if (postProfileImage != null && !postProfileImage.isEmpty()) {
            // s3 업로드
            String postProfileImageUrl = uploadUtil.upload(postProfileImage, UploadUtil.BucketType.MEDIA,"post-profile");
            member.setPostProfileInfo(
                    PostProfileInfo.from(postProfileNickname, postProfileImageUrl)
            );
        } else {
            // 프로필 사진이 없으면 기본값 설정
            member.setPostProfileInfo(
                    PostProfileInfo.from(postProfileNickname, "")
            );
        }
    }
}
