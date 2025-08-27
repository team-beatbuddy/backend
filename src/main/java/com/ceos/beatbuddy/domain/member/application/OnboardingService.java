package com.ceos.beatbuddy.domain.member.application;

import com.ceos.beatbuddy.domain.member.constant.Region;
import com.ceos.beatbuddy.domain.member.dto.MemberConsentRequestDTO;
import com.ceos.beatbuddy.domain.member.dto.NicknameDTO;
import com.ceos.beatbuddy.domain.member.dto.PostProfileRequestDTO;
import com.ceos.beatbuddy.domain.member.dto.RegionRequestDTO;
import com.ceos.beatbuddy.domain.member.dto.response.MemberResponseDTO;
import com.ceos.beatbuddy.domain.member.dto.response.OnboardingResponseDto;
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

import java.time.LocalDateTime;
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

    /**
     * 게시판 프로필 닉네임 중복 체크
     */
    public Boolean isPostProfileNicknameDuplicate(Long memberId, String postProfileNickname) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        if (memberRepository.existsByPostProfileInfo_PostProfileNickname(postProfileNickname)) {
            throw new CustomException(MemberErrorCode.NICKNAME_ALREADY_EXIST);
        }

        return true;
    }

    /**
     * 게시판 프로필 닉네임 유효성 검증
     */
    public Boolean isPostProfileNicknameValid(Long memberId, String postProfileNickname) {
        Member member = memberService.validateAndGetMember(memberId);

        // 12글자 초과
        if (postProfileNickname.length() > 12) {
            throw new CustomException(MemberErrorCode.NICKNAME_OVER_LENGTH);
        }
        // 띄어쓰기 존재
        if (postProfileNickname.matches(".*\\s+.*")) {
            throw new CustomException(MemberErrorCode.NICKNAME_SPACE_EXIST);
        }
        // 특수문자 체크
        if (!NICKNAME_PATTERN.matcher(postProfileNickname).matches()) {
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
    @Transactional
    public void savePostProfile(Long memberId, PostProfileRequestDTO postProfileRequestDTO, MultipartFile postProfileImage) {
        Member member = memberService.validateAndGetMember(memberId);

        String postProfileNickname = postProfileRequestDTO.getPostProfileNickname();
        if (postProfileNickname == null || postProfileNickname.isEmpty()) {
            throw new CustomException(MemberErrorCode.POST_PROFILE_NICKNAME_REQUIRED);
        }
        
        // 닉네임 유효성 검증
        isPostProfileNicknameValid(memberId, postProfileNickname);
        // 닉네임 중복 체크
        isPostProfileNicknameDuplicate(memberId, postProfileNickname);

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

    @Transactional
    public void updatePostProfile(Long memberId, PostProfileRequestDTO postProfileRequestDTO, MultipartFile postProfileImage) {
        Member member = memberService.validateAndGetMember(memberId);
        PostProfileInfo currentPostProfileInfo = getCurrentPostProfileOrCreate(member);
        
        String newNickname = processNicknameUpdate(memberId, postProfileRequestDTO, currentPostProfileInfo);
        String newImageUrl = processImageUpdate(postProfileImage, currentPostProfileInfo);
        
        PostProfileInfo updatedProfileInfo = buildUpdatedPostProfileInfo(
                currentPostProfileInfo, newNickname, newImageUrl);
        
        member.setPostProfileInfo(updatedProfileInfo);
    }
    
    /**
     * 기존 PostProfile 정보 가져오기 (없으면 기본값으로 생성)
     */
    private PostProfileInfo getCurrentPostProfileOrCreate(Member member) {
        PostProfileInfo currentPostProfileInfo = member.getPostProfileInfo();
        return currentPostProfileInfo != null ? currentPostProfileInfo : PostProfileInfo.from("", "");
    }
    
    /**
     * 닉네임 업데이트 처리
     */
    private String processNicknameUpdate(Long memberId, PostProfileRequestDTO postProfileRequestDTO, 
                                       PostProfileInfo currentPostProfileInfo) {
        if (postProfileRequestDTO == null || 
            postProfileRequestDTO.getPostProfileNickname() == null || 
            postProfileRequestDTO.getPostProfileNickname().trim().isEmpty()) {
            return currentPostProfileInfo.getPostProfileNickname();
        }
        
        String requestedNickname = postProfileRequestDTO.getPostProfileNickname().trim();
        
        // 닉네임이 기존과 동일하면 그대로 반환
        if (requestedNickname.equals(currentPostProfileInfo.getPostProfileNickname())) {
            return currentPostProfileInfo.getPostProfileNickname();
        }
        
        // 닉네임 변경 시 검증
        isPostProfileNicknameValid(memberId, requestedNickname);
        isPostProfileNicknameDuplicate(memberId, requestedNickname);
        validatePostProfileNicknameChangeLimit(currentPostProfileInfo);
        
        return requestedNickname;
    }
    
    /**
     * 이미지 업데이트 처리
     */
    private String processImageUpdate(MultipartFile postProfileImage, PostProfileInfo currentPostProfileInfo) {
        if (postProfileImage == null || postProfileImage.isEmpty()) {
            return currentPostProfileInfo.getPostProfileImageUrl();
        }
        
        // 기존 이미지 삭제
        deleteExistingImageIfExists(currentPostProfileInfo.getPostProfileImageUrl());
        
        // 새 이미지 업로드
        return uploadUtil.upload(postProfileImage, UploadUtil.BucketType.MEDIA, "post-profile");
    }
    
    /**
     * 기존 이미지 삭제 (존재하는 경우)
     */
    private void deleteExistingImageIfExists(String existingImageUrl) {
        if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
            uploadUtil.deleteImage(existingImageUrl, UploadUtil.BucketType.MEDIA);
        }
    }
    
    /**
     * 업데이트된 PostProfileInfo 빌드
     */
    private PostProfileInfo buildUpdatedPostProfileInfo(PostProfileInfo currentPostProfileInfo, 
                                                      String newNickname, String newImageUrl) {
        boolean nicknameChanged = !newNickname.equals(currentPostProfileInfo.getPostProfileNickname());
        
        if (nicknameChanged) {
            return buildPostProfileInfoWithNicknameChange(currentPostProfileInfo, newNickname, newImageUrl);
        } else {
            return buildPostProfileInfoWithoutNicknameChange(currentPostProfileInfo, newImageUrl);
        }
    }
    
    /**
     * 닉네임 변경된 경우의 PostProfileInfo 빌드
     */
    private PostProfileInfo buildPostProfileInfoWithNicknameChange(PostProfileInfo currentPostProfileInfo, 
                                                                 String newNickname, String newImageUrl) {
        LocalDateTime now = LocalDateTime.now();
        int newChangeCount = calculateNewChangeCount(currentPostProfileInfo, now);
        
        return PostProfileInfo.builder()
                .postProfileNickname(newNickname)
                .postProfileImageUrl(newImageUrl)
                .postProfileNicknameChangedAt(now)
                .postProfileNicknameChangeCount(newChangeCount)
                .setNewPostProfileNickname(true)
                .build();
    }
    
    /**
     * 닉네임 변경되지 않은 경우의 PostProfileInfo 빌드
     */
    private PostProfileInfo buildPostProfileInfoWithoutNicknameChange(PostProfileInfo currentPostProfileInfo, 
                                                                    String newImageUrl) {
        return PostProfileInfo.builder()
                .postProfileNickname(currentPostProfileInfo.getPostProfileNickname())
                .postProfileImageUrl(newImageUrl)
                .postProfileNicknameChangedAt(currentPostProfileInfo.getPostProfileNicknameChangedAt())
                .postProfileNicknameChangeCount(currentPostProfileInfo.getPostProfileNicknameChangeCount())
                .setNewPostProfileNickname(currentPostProfileInfo.getSetNewPostProfileNickname())
                .build();
    }
    
    /**
     * 새로운 변경 횟수 계산
     */
    private int calculateNewChangeCount(PostProfileInfo currentPostProfileInfo, LocalDateTime now) {
        LocalDateTime lastChangedAt = currentPostProfileInfo.getPostProfileNicknameChangedAt();
        
        if (lastChangedAt == null) {
            return 1;
        }
        
        int currentCount = currentPostProfileInfo.getPostProfileNicknameChangeCount();
        if (currentCount >= 2 && lastChangedAt.plusDays(14).isAfter(now)) {
            throw new CustomException(MemberErrorCode.NICKNAME_CHANGE_LIMITED);
        }
        
        // 14일이 지났거나 아직 2회 미만인 경우
        return currentCount >= 2 ? 1 : currentCount + 1;
    }

    /**
     * PostProfile 닉네임 변경 제한 검증
     */
    private void validatePostProfileNicknameChangeLimit(PostProfileInfo currentPostProfileInfo) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastChangedAt = currentPostProfileInfo.getPostProfileNicknameChangedAt();
        
        // 변경 이력이 없는 경우는 허용
        if (lastChangedAt == null) {
            return;
        }
        
        int changeCount = currentPostProfileInfo.getPostProfileNicknameChangeCount();
        if (changeCount >= 2) {
            // 마지막 변경일 기준 14일 이내면 차단
            if (lastChangedAt.plusDays(14).isAfter(now)) {
                throw new CustomException(MemberErrorCode.NICKNAME_CHANGE_LIMITED);
            }
        }
    }
}
