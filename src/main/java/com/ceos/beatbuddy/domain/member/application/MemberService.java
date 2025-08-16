package com.ceos.beatbuddy.domain.member.application;

import com.ceos.beatbuddy.domain.archive.repository.ArchiveRepository;
import com.ceos.beatbuddy.domain.follow.repository.FollowRepository;
import com.ceos.beatbuddy.domain.heartbeat.repository.HeartbeatRepository;
import com.ceos.beatbuddy.domain.member.constant.Region;
import com.ceos.beatbuddy.domain.member.constant.Role;
import com.ceos.beatbuddy.domain.member.dto.AdminMemberListDTO;
import com.ceos.beatbuddy.domain.member.dto.NicknameDTO;
import com.ceos.beatbuddy.domain.member.dto.response.MemberProfileSummaryDTO;
import com.ceos.beatbuddy.domain.member.dto.response.MemberResponseDTO;
import com.ceos.beatbuddy.domain.member.entity.*;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.exception.MemberGenreErrorCode;
import com.ceos.beatbuddy.domain.member.exception.MemberMoodErrorCode;
import com.ceos.beatbuddy.domain.member.repository.*;
import com.ceos.beatbuddy.domain.vector.entity.Vector;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.config.oauth.dto.Oauth2MemberDto;
import com.ceos.beatbuddy.global.util.UploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberBlockRepository memberBlockRepository;
    private final MemberMoodRepository memberMoodRepository;
    private final MemberGenreRepository memberGenreRepository;
    private final HeartbeatRepository heartbeatRepository;
    private final ArchiveRepository archiveRepository;
    private final UploadUtil uploadUtil;
    private final MemberQueryRepository memberQueryRepository;
    private final FollowRepository followRepository;
//
//    @Value("${iamport.api.key}")
//    private String imp_key;
//
//    @Value("${iamport.api.secret}")
//    private String imp_secret;

    /**
     * loginId로 유저 식별자 조회 유저가 존재하면 식별자 반환 유저가 존재하지 않으면 회원가입 처리 후 식별자 반환
     *
     * @param loginId
     * @param name
     * @return UserId
     */
    @Transactional
    public Oauth2MemberDto findOrCreateUser(String loginId, String name) throws CustomException {
        Member member = memberRepository.findByLoginId(loginId)
                .orElse(null);
        if (member == null) {
            return Oauth2MemberDto.of(this.join(loginId, name));
        } else {
            return Oauth2MemberDto.of(member);
        }
    }

    private Member join(String loginId, String name) {
        return memberRepository.save(
                Member.builder()
                        .loginId(loginId)
                        .realName(name)
                        .role(Role.USER)
                        .nickname(name)
                        .build());
    }

    public Boolean getNicknameSet(Long memberId) {
        Member member = this.validateAndGetMember(memberId);

        return member.getSetNewNickname();
    }

    public NicknameDTO getNickname(Long memberId) {
        Member member = this.validateAndGetMember(memberId);
        return NicknameDTO.builder()
                .nickname(member.getNickname()).build();
    }

    public List<String> getPreferences(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        MemberGenre memberGenre = memberGenreRepository.findLatestGenreByMember(member)
                .orElseThrow(() -> new CustomException(MemberGenreErrorCode.MEMBER_GENRE_NOT_EXIST));
        MemberMood memberMood = memberMoodRepository.findLatestMoodByMember(member)
                .orElseThrow(() -> new CustomException(MemberMoodErrorCode.MEMBER_MOOD_NOT_EXIST));
        List<String> trueGenreElements = Vector.getTrueGenreElements(memberGenre.getGenreVector());
        List<String> trueMoodElements = Vector.getTrueMoodElements(memberMood.getMoodVector());

        List<String> memberRegion =  member.getRegions().stream()
                .map(Region::getText)
                .toList();

        if(memberRegion.isEmpty()){
            throw new CustomException(MemberErrorCode.REGION_FIELD_EMPTY);
        }
        List<String> preferenceList = new ArrayList<>(trueGenreElements);
        preferenceList.addAll(trueMoodElements);
        preferenceList.addAll(memberRegion);

        return preferenceList;
    }

    public String deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));
        memberMoodRepository.deleteByMember(member);
        memberGenreRepository.deleteByMember(member);
        heartbeatRepository.deleteByMember(member);
        archiveRepository.deleteByMember(member);
        memberRepository.delete(member);
        return member.getLoginId();
    }

    @Transactional
    public void uploadProfileImage(Long memberId, MultipartFile image) {
        Member member = this.validateAndGetMember(memberId);

        //기존 이미지 삭제
        if (member.getProfileImage() != null && !member.getProfileImage().isBlank()) {
            uploadUtil.deleteImage(member.getProfileImage(), UploadUtil.BucketType.MEDIA);
        }

        // 새 이미지 업로드
        String imageUrl = uploadUtil.upload(image, UploadUtil.BucketType.MEDIA, "member");

        // 멤버 정보 업데이트
        member.setProfileImage(imageUrl);
    }

    public MemberProfileSummaryDTO getProfileSummary(Long targetMemberId, Long currentMemberId) {
        boolean isOwnProfile = targetMemberId.equals(currentMemberId);
        return memberQueryRepository.getMemberSummary(targetMemberId, isOwnProfile);
    }


    // 14일 내 최대 2회 변경 가능
    // 3번째 변경 시도 시: “마지막 변경 기준 14일 이후에 변경 가능” 안내
    @Transactional
    public MemberResponseDTO updateNickname(Long memberId, NicknameDTO nicknameDTO) {
        Member member = validateAndGetMember(memberId);
        String newNickname = nicknameDTO.getNickname();

        // 닉네임 동일하면 예외
        if (member.getNickname().equals(newNickname)) {
            throw new CustomException(MemberErrorCode.SAME_NICKNAME);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastChangedAt = member.getNicknameChangedAt();

        // 변경 이력이 없는 경우
        if (lastChangedAt == null) {
            member.setNicknameChangedAt(now);
            member.setNicknameChangeCount(1);
        } else {
            if (member.getNicknameChangeCount() >= 2) {
                // 마지막 변경일 기준 14일 이내면 차단
                if (lastChangedAt.plusDays(14).isAfter(now)) {
                    throw new CustomException(MemberErrorCode.NICKNAME_CHANGE_LIMITED); // "14일 내 최대 2회까지 변경 가능합니다."
                } else {
                    // 14일 지났으므로 초기화 후 1회 카운트
                    member.setNicknameChangedAt(now);
                    member.setNicknameChangeCount(1);
                }
            } else {
                // 아직 2회 미만이므로 변경 허용
                member.setNicknameChangedAt(now);
                member.setNicknameChangeCount(member.getNicknameChangeCount() + 1);
            }
        }

        member.setNickname(newNickname);
        memberRepository.save(member);
        return MemberResponseDTO.toSetNicknameDTO(member);
    }

    public Member validateAndGetMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));
    }

    // ============= Member Blocking Functionality =============
    
    /**
     * 특정 사용자가 차단한 멤버들의 ID 목록을 조회
     * @param blockerId 차단하는 사용자의 ID
     * @return 차단된 멤버들의 ID 목록
     */
    public Set<Long> getBlockedMemberIds(Long blockerId) {
        return memberBlockRepository.findBlockedMemberIdsByBlockerId(blockerId);
    }
    
    /**
     * 멤버 차단
     * @param blockerId 차단하는 사용자의 ID
     * @param blockedId 차단당하는 사용자의 ID
     */
    @Transactional
    public void blockMember(Long blockerId, Long blockedId) {
        // 자기 자신을 차단할 수 없음
        if (blockerId.equals(blockedId)) {
            throw new CustomException(MemberErrorCode.CANNOT_BLOCK_SELF);
        }
        
        Member blocker = validateAndGetMember(blockerId);
        Member blocked = validateAndGetMember(blockedId);
        
        // 이미 차단되어 있는지 확인
        if (memberBlockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId).isPresent()) {
            throw new CustomException(MemberErrorCode.ALREADY_BLOCKED);
        }
        
        MemberBlock memberBlock = MemberBlock.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();

        // 그 사람을 팔로잉 하고 있었다면 삭제
        if (followRepository.existsByFollower_IdAndFollowing_Id(blockerId, blockedId)) {
            followRepository.deleteByFollower_IdAndFollowing_Id(blockerId, blockedId);
        }

        memberBlockRepository.save(memberBlock);
    }
    // v2 개발 기능
    /**
     * 멤버 차단 해제
     * @param blockerId 차단하는 사용자의 ID
     * @param blockedId 차단당하는 사용자의 ID
     */
    @Transactional
    public void unblockMember(Long blockerId, Long blockedId) {
        MemberBlock memberBlock = memberBlockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.BLOCK_NOT_FOUND));
        
        memberBlockRepository.delete(memberBlock);
    }
    
    /**
     * 두 사용자 간 차단 상태 확인
     * @param blockerId 차단하는 사용자의 ID
     * @param blockedId 차단당하는 사용자의 ID
     * @return 차단 상태 여부
     */
    public boolean isBlocked(Long blockerId, Long blockedId) {
        return memberBlockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId).isPresent();
    }

    @Transactional
    public void updateFcmToken(Long memberId, String token) {
        Member member = validateAndGetMember(memberId);

        if (token != null && !token.trim().isEmpty()) {
            if (!Objects.equals(member.getFcmToken(), token)) {
                member.setFcmToken(token);
            }
        } else {
            member.setFcmToken(null);
        }
    }

    @Transactional(readOnly = true)
    public List<AdminMemberListDTO> getAllMembers(Long memberId, String role) {
        Member member = validateAndGetMember(memberId);
        // 어드민인지 확인
        if (!member.isAdmin()) {
            throw new CustomException(MemberErrorCode.NOT_ADMIN);
        }

        List<Member> members = memberRepository.findAllByRole(Role.valueOf(role.toUpperCase()));
        return members.stream()
                .map(AdminMemberListDTO::fromMember)
                .toList();
    }

    @Transactional(readOnly = true)
    public PostProfileInfo getPostProfile(Long memberId) {
        Member member = validateAndGetMember(memberId);

        if (member.getPostProfileInfo() == null) {
            // 게시판용 프로필이 없는 경우 기본값 반환
            return PostProfileInfo.from(null, null);
        }
        return PostProfileInfo.from(member.getPostProfileInfo().getPostProfileNickname(), member.getPostProfileInfo().getPostProfileImageUrl());
    }
}
