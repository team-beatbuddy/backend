package com.ceos.beatbuddy.domain.home.application;

import com.ceos.beatbuddy.domain.archive.application.ArchiveService;
import com.ceos.beatbuddy.domain.archive.entity.Archive;
import com.ceos.beatbuddy.domain.archive.exception.ArchiveErrorCode;
import com.ceos.beatbuddy.domain.archive.repository.ArchiveRepository;
import com.ceos.beatbuddy.domain.home.dto.KeywordResponseDTO;
import com.ceos.beatbuddy.domain.member.application.RecommendService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.entity.MemberGenre;
import com.ceos.beatbuddy.domain.member.entity.MemberMood;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.exception.MemberGenreErrorCode;
import com.ceos.beatbuddy.domain.member.exception.MemberMoodErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberGenreRepository;
import com.ceos.beatbuddy.domain.member.repository.MemberMoodRepository;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.domain.vector.util.VectorUtils;
import com.ceos.beatbuddy.domain.venue.dto.VenueResponseDTO;
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
    private final RecommendService recommendService;
    private final ArchiveService archiveService;
    private final ArchiveRepository archiveRepository;

    public List<VenueResponseDTO> saveArchiveAndRecommendVenues(Long memberId, long l) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        List<Archive> archive = archiveRepository.findByMember(member);

        if (archive.isEmpty()) {
            List<Long> memberGenreIds = memberGenreRepository.findIdsByMember(member);
            List<Long> memberMoodIds = memberMoodRepository.findIdsByMember(member);

            if (memberGenreIds.isEmpty()) {
                throw new CustomException(MemberGenreErrorCode.MEMBER_GENRE_NOT_EXIST);
            }
            if (memberMoodIds.isEmpty()) {
                throw new CustomException(MemberMoodErrorCode.MEMBER_MOOD_NOT_EXIST);
            }

            for (Long genreId : memberGenreIds) {
                for (Long moodId : memberMoodIds) {
                    try {
                        archiveService.addPreferenceInArchive(memberId, moodId, genreId);
                    } catch (CustomException e) {
                        // 이미 존재하는 조합이면 skip
                        throw new CustomException(ArchiveErrorCode.ARCHIVE_ALREADY_EXIST);
                    }
                }
            }
        }

        return recommendService.recommendVenues(memberId, 5L);
    }

//    public KeywordResponseDTO getKeyword(Long memberId) {
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new CustomException(MEMBER_NOT_EXIST));
//
//        List<String> filteredGenres = member.getMemberGenres().stream()
//                .map(MemberGenre::getGenreVectorString)
//                .map(VectorUtils::parseVector)
//                .flatMap(vector -> VectorUtils.getSelectedItems(vector, ALL_GENRES).stream())
//                .distinct()
//                .collect(Collectors.toList());
//
//        List<String> filteredMoods = member.getMemberMoods().stream()
//                .map(MemberMood::getMoodVectorString)
//                .map(VectorUtils::parseVector)
//                .flatMap(vector -> VectorUtils.getSelectedItems(vector, ALL_MOODS).stream())
//                .distinct()
//                .collect(Collectors.toList());
//
//        return KeywordResponseDTO.builder()
//                .genres(filteredGenres)
//                .moods(filteredMoods)
//                .regions(member.getRegions())
//                .build();
//    }
}
