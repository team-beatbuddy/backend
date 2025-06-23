package com.ceos.beatbuddy.domain.member.application;

import com.ceos.beatbuddy.domain.archive.repository.ArchiveRepository;
import com.ceos.beatbuddy.domain.heartbeat.repository.HeartbeatRepository;
import com.ceos.beatbuddy.domain.member.constant.Region;
import com.ceos.beatbuddy.domain.member.constant.Role;
import com.ceos.beatbuddy.domain.member.dto.MemberProfileSummaryDTO;
import com.ceos.beatbuddy.domain.member.dto.NicknameDTO;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.entity.MemberGenre;
import com.ceos.beatbuddy.domain.member.entity.MemberMood;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.exception.MemberGenreErrorCode;
import com.ceos.beatbuddy.domain.member.exception.MemberMoodErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberGenreRepository;
import com.ceos.beatbuddy.domain.member.repository.MemberMoodRepository;
import com.ceos.beatbuddy.domain.member.repository.MemberQueryRepository;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.domain.vector.entity.Vector;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.UploadUtil;
import com.ceos.beatbuddy.global.config.oauth.dto.Oauth2MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberMoodRepository memberMoodRepository;
    private final MemberGenreRepository memberGenreRepository;
    private final HeartbeatRepository heartbeatRepository;
    private final ArchiveRepository archiveRepository;
    private final UploadUtil uploadUtil;
    private final MemberQueryRepository memberQueryRepository;
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
    public void uploadProfileImage(Long memberId, MultipartFile image) throws IOException {
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

    public MemberProfileSummaryDTO getProfileSummary(Long memberId) {
        return memberQueryRepository.getMemberSummary(memberId);
    }

    public Member validateAndGetMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));
    }
}
