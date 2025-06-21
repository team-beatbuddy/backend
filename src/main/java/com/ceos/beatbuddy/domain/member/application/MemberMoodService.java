package com.ceos.beatbuddy.domain.member.application;

import com.ceos.beatbuddy.domain.member.dto.MemberVectorResponseDTO;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.entity.MemberMood;
import com.ceos.beatbuddy.domain.member.exception.MemberGenreErrorCode;
import com.ceos.beatbuddy.domain.member.exception.MemberMoodErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberMoodRepository;
import com.ceos.beatbuddy.domain.vector.entity.Vector;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberMoodService {
    private final MemberService memberService;
    private final MemberMoodRepository memberMoodRepository;

    @Transactional
    public MemberVectorResponseDTO addMoodVector(Long memberId, Map<String, Double> moods) {
        Member member = memberService.validateAndGetMember(memberId);

        Vector preferenceVector = Vector.fromMoods(moods);

        MemberMood memberMood = MemberMood.builder()
                .member(member).moodVectorString(preferenceVector.toString())
                .build();

        memberMoodRepository.save(memberMood);
        return MemberVectorResponseDTO.builder()
                .vectorString(memberMood.getMoodVectorString())
                .memberId(member.getId())
                .vectorId(memberMood.getId())
                .loginId(member.getLoginId())
                .nickname(member.getNickname())
                .realName(member.getRealName())
                .build();
    }

    @Transactional
    public MemberVectorResponseDTO deleteMoodVector(Long memberId, Long memberMoodId) {
        Member member = memberService.validateAndGetMember(memberId);
        MemberMood memberMood = memberMoodRepository.findById(memberMoodId).orElseThrow(()->new CustomException((MemberMoodErrorCode.MEMBER_MOOD_NOT_EXIST)));
        List<MemberMood> memberMoods = memberMoodRepository.findAllByMember(member);

        if (memberMoods.size() <= 1) {
            throw new CustomException(MemberMoodErrorCode.MEMBER_MOOD_ONLY_ONE);
        }

        memberMoodRepository.delete(memberMood);

        return MemberVectorResponseDTO.builder()
                .vectorString(memberMood.getMoodVectorString())
                .memberId(member.getId())
                .vectorId(memberMood.getId())
                .loginId(member.getLoginId())
                .nickname(member.getNickname())
                .realName(member.getRealName())
                .build();
    }

    public List<MemberVectorResponseDTO> getAllMoodVector(Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);
        List<MemberMood> memberMoods = memberMoodRepository.findAllByMember(member);
        return memberMoods.stream()
                .map(memberGenre -> MemberVectorResponseDTO.builder()
                        .memberId(member.getId())
                        .vectorId(memberGenre.getId())
                        .loginId(member.getLoginId())
                        .nickname(member.getNickname())
                        .realName(member.getRealName())
                        .vectorString(memberGenre.getMoodVectorString())
                        .build())
                .collect(Collectors.toList());
    }

    public MemberVectorResponseDTO getLatestMoodVector(Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);
        MemberMood memberMood = memberMoodRepository.findLatestMoodByMember(member).orElseThrow(()-> new CustomException(MemberMoodErrorCode.MEMBER_MOOD_NOT_EXIST));
        return MemberVectorResponseDTO.builder()
                .vectorString(memberMood.getMoodVectorString())
                .memberId(member.getId())
                .vectorId(memberMood.getId())
                .loginId(member.getLoginId())
                .nickname(member.getNickname())
                .realName(member.getRealName())
                .build();
    }


}
