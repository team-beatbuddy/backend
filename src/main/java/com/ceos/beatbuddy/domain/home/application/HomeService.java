package com.ceos.beatbuddy.domain.home.application;

import com.ceos.beatbuddy.domain.home.dto.KeywordResponseDTO;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.entity.MemberGenre;
import com.ceos.beatbuddy.domain.member.entity.MemberMood;
import com.ceos.beatbuddy.domain.member.repository.MemberGenreRepository;
import com.ceos.beatbuddy.domain.member.repository.MemberMoodRepository;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.domain.vector.util.VectorUtils;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ceos.beatbuddy.domain.member.exception.MemberErrorCode.MEMBER_NOT_EXIST;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomeService {
    private final MemberRepository memberRepository;
    private final MemberGenreRepository memberGenreRepository;
    private final MemberMoodRepository memberMoodRepository;

    private static final List<String> ALL_GENRES = Arrays.asList(
            "HIPHOP", "R&B", "EDM", "HOUSE", "TECHNO", "SOUL&FUNK", "ROCK", "LATIN", "K-POP", "POP"
    );
    private static final List<String> ALL_MOODS = Arrays.asList(
            "CLUB", "PUB", "ROOFTOP", "DEEP", "COMMERCIAL", "CHILL", "EXOTIC", "HUNTING"
    );

    public KeywordResponseDTO getKeyword(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_EXIST));

        List<String> filteredGenres = member.getMemberGenres().stream()
                .map(MemberGenre::getGenreVectorString)
                .map(VectorUtils::parseVector)
                .flatMap(vector -> VectorUtils.getSelectedItems(vector, ALL_GENRES).stream())
                .distinct()
                .collect(Collectors.toList());

        List<String> filteredMoods = member.getMemberMoods().stream()
                .map(MemberMood::getMoodVectorString)
                .map(VectorUtils::parseVector)
                .flatMap(vector -> VectorUtils.getSelectedItems(vector, ALL_MOODS).stream())
                .distinct()
                .collect(Collectors.toList());

        return KeywordResponseDTO.builder()
                .genres(filteredGenres)
                .moods(filteredMoods)
                .regions(member.getRegions())
                .build();
    }
}
