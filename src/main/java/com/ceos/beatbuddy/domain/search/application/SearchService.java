package com.ceos.beatbuddy.domain.search.application;


import com.ceos.beatbuddy.domain.heartbeat.repository.HeartbeatRepository;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.domain.recent_search.application.RecentSearchService;
import com.ceos.beatbuddy.domain.recent_search.entity.SearchTypeEnum;
import com.ceos.beatbuddy.domain.search.dto.SearchDropDownDTO;
import com.ceos.beatbuddy.domain.search.dto.SearchQueryResponseDTO;
import com.ceos.beatbuddy.domain.search.dto.SearchRankResponseDTO;
import com.ceos.beatbuddy.domain.search.exception.SearchErrorCode;
import com.ceos.beatbuddy.domain.vector.entity.Vector;
import com.ceos.beatbuddy.domain.venue.application.VenueInfoService;
import com.ceos.beatbuddy.domain.venue.application.VenueSearchService;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.domain.venue.entity.VenueDocument;
import com.ceos.beatbuddy.domain.venue.entity.VenueGenre;
import com.ceos.beatbuddy.domain.venue.entity.VenueMood;
import com.ceos.beatbuddy.domain.venue.exception.VenueErrorCode;
import com.ceos.beatbuddy.domain.venue.exception.VenueGenreErrorCode;
import com.ceos.beatbuddy.domain.venue.exception.VenueMoodErrorCode;
import com.ceos.beatbuddy.domain.venue.repository.VenueGenreRepository;
import com.ceos.beatbuddy.domain.venue.repository.VenueMoodRepository;
import com.ceos.beatbuddy.domain.venue.repository.VenueRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SearchService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;
    private final VenueRepository venueRepository;
    private final VenueGenreRepository venueGenreRepository;
    private final VenueMoodRepository venueMoodRepository;
    private final HeartbeatRepository heartbeatRepository;
    private final RecentSearchService recentSearchService;
    private final VenueInfoService venueInfoService;
    private final VenueSearchService venueSearchService;

    private void saveSearchKeywordsToRedis(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return;

        for (String keyword : keywords) {
            try {
                // 1점 증가
                redisTemplate.opsForZSet().incrementScore("ranking", keyword, 1.0);

                // 만료 시간 (24시간 후)
                long expireAt = Instant.now().getEpochSecond() + 86400;
                redisTemplate.opsForZSet().add("expire", keyword, (double) expireAt);
            } catch (Exception e) {
                System.err.println("❌ Redis 키워드 저장 실패: " + keyword + " / " + e.getMessage());
            }
        }
    }


    // 인기검색어 리스트 1위~10위까지
    public List<SearchRankResponseDTO> searchRankList() {
        String key = "ranking";
        ZSetOperations<String, String> ZSetOperations = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> typedTuples = ZSetOperations.reverseRangeWithScores(key, 0, 9);  //score순으로 10개 보여줌
        return typedTuples.stream().map(SearchRankResponseDTO::toSearchRankResponseDTO).collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    public void removeExpiredElements() {
        String rankingKey = "ranking";
        String expireKey = "expire";
        long currentTime = Instant.now().getEpochSecond();
        double currentTimeDouble = (double) currentTime;
        System.out.println("Remove expired elements.");
        Set<String> expiredWords = redisTemplate.opsForZSet().rangeByScore(expireKey, 0, currentTimeDouble);
        if (expiredWords != null) {
            for (String word : expiredWords) {
                redisTemplate.opsForZSet().remove(rankingKey, word);
                redisTemplate.opsForZSet().remove(expireKey, word);
            }
        }
    }

    public List<SearchQueryResponseDTO> searchDropDown(Long memberId, SearchDropDownDTO searchDropDownDTO, Double latitude, Double longitude, String searchType, int page, int size) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        if (page < 1) {
            throw new CustomException(ErrorCode.PAGE_OUT_OF_BOUNDS);
        }

        String keyword = searchDropDownDTO.getKeyword();

        // 최근 검색어로 추가
        recentSearchService.saveRecentSearch(String.valueOf(SearchTypeEnum.valueOf(searchType)), keyword, memberId);

        // Redis에 검색어 저장
        saveSearchKeywordsToRedis(Collections.singletonList(keyword));

        String regionKeyword = searchDropDownDTO.getRegionTag();
        String genreKeyword = searchDropDownDTO.getGenreTag();

        String criteria = searchDropDownDTO.getSortCriteria();

        List<VenueDocument> venueList = venueSearchService.searchMapDropDown(keyword, regionKeyword, genreKeyword);

        List<SearchQueryResponseDTO> searchQueryResponseDTOS = venueList.stream()
                .map(venueDocument -> {
                    Venue venue = venueRepository.findById(venueDocument.getId())
                            .orElseThrow(() -> new CustomException(VenueErrorCode.VENUE_NOT_EXIST));
                    VenueGenre venueGenre = venueGenreRepository.findByVenue(venue)
                            .orElseThrow(() -> new CustomException(VenueGenreErrorCode.VENUE_GENRE_NOT_EXIST));
                    VenueMood venueMood = venueMoodRepository.findByVenue(venue)
                            .orElseThrow(() -> new CustomException(VenueMoodErrorCode.VENUE_MOOD_NOT_EXIST));
                    boolean isHeartbeat = heartbeatRepository.findByMemberVenue(member, venue).isPresent();

                    List<String> trueGenreElements = Vector.getTrueGenreElements(venueGenre.getGenreVector());
                    List<String> trueMoodElements = Vector.getTrueMoodElements(venueMood.getMoodVector());

                    List<String> tagList = new ArrayList<>(trueGenreElements);
                    tagList.addAll(trueMoodElements);
                    tagList.add(venue.getRegion().getText());

                    return new SearchQueryResponseDTO(
                            LocalDateTime.now(),
                            venue.getId(),
                            venue.getEnglishName(),
                            venue.getKoreanName(),
                            tagList,
                            venue.getHeartbeatNum(),
                            isHeartbeat,
                            venue.getLogoUrl(),
                            venue.getBackgroundUrl(),
                            venue.getAddress()
                    );
                })
                .toList();

        // ✅ 정렬
        List<SearchQueryResponseDTO> sortedList = sortVenuesByCriteria(searchQueryResponseDTOS, criteria, latitude, longitude);

        // ✅ 페이지네이션 적용
        return applyPaginationIfNeeded(sortedList, criteria, page, size);
    }

    private List<SearchQueryResponseDTO> sortVenuesByCriteria(List<SearchQueryResponseDTO> list, String criteria, double lat, double lng) {
        if (criteria.equals("인기순")) {
            return list.stream()
                    .sorted(Comparator.comparingLong(SearchQueryResponseDTO::getHeartbeatNum).reversed())
                    .toList();
        } else if (criteria.equals("거리순")) {
            return list.stream()
                    .sorted(Comparator.comparingDouble(dto -> {
                        Venue v = venueInfoService.validateAndGetVenue(dto.getVenueId());
                        return haversine(lat, lng, v.getLatitude(), v.getLongitude());
                    }))
                    .toList();
        } else throw new CustomException(SearchErrorCode.UNAVAILABLE_SORT_CRITERIA);
    }

    private List<SearchQueryResponseDTO> applyPaginationIfNeeded(List<SearchQueryResponseDTO> list, String criteria, int page, int size) {
        if (criteria.equals("거리순")) {
            // 1-based를 0-based로 변환
            int zeroBasedPage = page - 1;
            return list.stream()
                    .skip((long) zeroBasedPage * size)
                    .limit(size)
                    .toList();
        }
        return list;
    }

    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // 지구 반지름 (단위: km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
