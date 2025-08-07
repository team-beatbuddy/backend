package com.ceos.beatbuddy.domain.search.application;


import com.ceos.beatbuddy.domain.heartbeat.entity.Heartbeat;
import com.ceos.beatbuddy.domain.heartbeat.repository.HeartbeatRepository;
import com.ceos.beatbuddy.domain.member.constant.Region;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.domain.recent_search.application.RecentSearchService;
import com.ceos.beatbuddy.domain.recent_search.entity.SearchTypeEnum;
import com.ceos.beatbuddy.domain.search.dto.SearchDropDownDTO;
import com.ceos.beatbuddy.domain.search.dto.SearchPageResponseDTO;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
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
                log.error("Redis 키워드 저장 실패: keyword={}, error={}", keyword, e.getMessage(), e);
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

    public SearchPageResponseDTO searchDropDown(Long memberId, SearchDropDownDTO searchDropDownDTO, Double latitude, Double longitude, String searchType, int page, int size) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        if (page < 1) {
            throw new CustomException(ErrorCode.PAGE_OUT_OF_BOUNDS);
        }

        String keyword = searchDropDownDTO.getKeyword();

        if (searchDropDownDTO.getKeyword() != null && !searchDropDownDTO.getKeyword().isBlank()) {
            // 최근 검색어로 추가
            recentSearchService.saveRecentSearch(String.valueOf(SearchTypeEnum.valueOf(searchType)), keyword, memberId);

            // Redis에 검색어 저장
            saveSearchKeywordsToRedis(Collections.singletonList(keyword));
        }

        String regionKeyword = searchDropDownDTO.getRegionTag();
        String genreKeyword = searchDropDownDTO.getGenreTag();

        String criteria = searchDropDownDTO.getSortCriteria();

        List<VenueDocument> venueList = venueSearchService.searchMapDropDown(keyword, regionKeyword, genreKeyword);

        List<Long> venueIds = venueList.stream()
                .map(VenueDocument::getId)
                .toList();

        // venueId → Venue 매핑
        List<Venue> venues = venueRepository.findByIdIn(venueIds);
        Map<Long, Venue> venueMap = venues.stream()
                .collect(Collectors.toMap(Venue::getId, Function.identity()));

        // 하트비트 정보를 한 번에 조회하여 Map으로 만들기
        List<Heartbeat> heartbeats = heartbeatRepository.findByMemberAndVenueIdIn(member, venueIds);
        Set<Long> heartbeatVenueIds = heartbeats.stream()
                .map(hb -> hb.getVenue().getId())
                .collect(Collectors.toSet());

        List<SearchQueryResponseDTO> searchQueryResponseDTOS = venueList.stream()
                .map(venueDocument -> {
                    Venue venue = venueMap.get(venueDocument.getId());
                    if (venue == null) return null; // 예외 처리 필요 시 추가

                    boolean isHeartbeat = heartbeatVenueIds.contains(venueDocument.getId());

                    List<String> tagList = new ArrayList<>(venueDocument.getGenre());
                    tagList.addAll(venueDocument.getMood());
                    tagList.add(venueDocument.getRegion());

                    return new SearchQueryResponseDTO(
                            LocalDateTime.now(),
                            venueDocument.getId(),
                            venueDocument.getEnglishName(),
                            venueDocument.getKoreanName(),
                            tagList,
                            venue.getHeartbeatNum(),
                            isHeartbeat,
                            venue.getLogoUrl(),
                            venue.getBackgroundUrl(),
                            venueDocument.getAddress(),
                            venue.getLatitude(),
                            venue.getLongitude()
                    );
                })
                .toList();

        // ✅ 정렬
        List<SearchQueryResponseDTO> sortedList = sortVenuesByCriteria(searchQueryResponseDTOS, criteria, latitude, longitude);

        // ✅ 페이지네이션 적용
        List<SearchQueryResponseDTO> paginatedList = applyPaginationIfNeeded(sortedList, criteria, page, size);

        // ✅ 페이지 정보와 함께 응답 생성
        int totalElements = sortedList.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return SearchPageResponseDTO.builder()
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .content(paginatedList)
                .build();
    }

    private List<SearchQueryResponseDTO> sortVenuesByCriteria(List<SearchQueryResponseDTO> list, String criteria, Double lat, Double lng) {
        if (criteria.equals("인기순")) {
            return list.stream()
                    .sorted(Comparator.comparingLong(SearchQueryResponseDTO::getHeartbeatNum).reversed())
                    .toList();
        } else if (criteria.equals("가까운 순")) {
            return list.stream()
                    .sorted(Comparator.comparingDouble(dto -> 
                        haversine(lat, lng, dto.getLatitude(), dto.getLongitude())
                    ))
                    .toList();
        } else throw new CustomException(SearchErrorCode.UNAVAILABLE_SORT_CRITERIA);
    }

    private List<SearchQueryResponseDTO> applyPaginationIfNeeded(List<SearchQueryResponseDTO> list, String criteria, int page, int size) {
        // 모든 정렬 기준에 페이지네이션 적용
        // 1-based를 0-based로 변환
        int zeroBasedPage = page - 1;
        return list.stream()
                .skip((long) zeroBasedPage * size)
                .limit(size)
                .toList();
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
