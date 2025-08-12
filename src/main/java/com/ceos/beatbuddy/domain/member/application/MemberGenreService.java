package com.ceos.beatbuddy.domain.member.application;

import com.ceos.beatbuddy.domain.member.dto.response.MemberVectorResponseDTO;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.entity.MemberGenre;
import com.ceos.beatbuddy.domain.member.exception.MemberGenreErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberGenreRepository;
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
public class MemberGenreService {
    private final MemberService memberService;
    private final MemberGenreRepository memberGenreRepository;

    @Transactional
    public MemberVectorResponseDTO addGenreVector(Long memberId, Map<String, Double> genres) {
        Member member = memberService.validateAndGetMember(memberId);

        Vector preferenceVector = Vector.fromGenres(genres);

        MemberGenre memberGenre = MemberGenre.builder()
                .member(member).genreVectorString(preferenceVector.toString())
                .build();

        memberGenreRepository.save(memberGenre);
        return MemberVectorResponseDTO.builder()
                .vectorString(memberGenre.getGenreVectorString())
                .memberId(member.getId())
                .vectorId(memberGenre.getId())
                .loginId(member.getLoginId())
                .nickname(member.getNickname())
                .realName(member.getRealName())
                .build();
    }


    @Transactional
    public MemberVectorResponseDTO deleteGenreVector(Long memberId, Long memberGenreId) {
        Member member = memberService.validateAndGetMember(memberId);
        MemberGenre memberGenre = memberGenreRepository.findById(memberGenreId).orElseThrow(()->new CustomException((MemberGenreErrorCode.MEMBER_GENRE_NOT_EXIST)));
        List<MemberGenre> memberGenres = memberGenreRepository.findAllByMember(member);

        if (memberGenres.size() <= 1) {
            throw new CustomException(MemberGenreErrorCode.MEMBER_GENRE_ONLY_ONE);
        }

        memberGenreRepository.delete(memberGenre);

        return MemberVectorResponseDTO.builder()
                .vectorString(memberGenre.getGenreVectorString())
                .memberId(member.getId())
                .vectorId(memberGenre.getId())
                .loginId(member.getLoginId())
                .nickname(member.getNickname())
                .realName(member.getRealName())
                .build();
    }

    public List<MemberVectorResponseDTO> getAllGenreVector(Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);
        List<MemberGenre> memberGenres = memberGenreRepository.findAllByMember(member);
        return memberGenres.stream()
                .map(memberGenre -> MemberVectorResponseDTO.builder()
                        .memberId(member.getId())
                        .vectorId(memberGenre.getId())
                        .loginId(member.getLoginId())
                        .nickname(member.getNickname())
                        .realName(member.getRealName())
                        .vectorString(memberGenre.getGenreVectorString())
                        .build())
                .collect(Collectors.toList());
    }

    public MemberVectorResponseDTO getLatestGenreVector(Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);
        MemberGenre memberGenre = memberGenreRepository.findLatestGenreByMember(member).orElseThrow(()-> new CustomException((MemberGenreErrorCode.MEMBER_GENRE_NOT_EXIST)));
        return MemberVectorResponseDTO.builder()
                .vectorString(memberGenre.getGenreVectorString())
                .memberId(member.getId())
                .vectorId(memberGenre.getId())
                .loginId(member.getLoginId())
                .nickname(member.getNickname())
                .realName(member.getRealName())
                .build();
    }

}
