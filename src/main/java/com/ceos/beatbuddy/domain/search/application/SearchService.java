package com.ceos.beatbuddy.domain.search.application;


import com.ceos.beatbuddy.domain.heartbeat.entity.Heartbeat;
import com.ceos.beatbuddy.domain.heartbeat.repository.HeartbeatRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.recent_search.application.RecentSearchService;
import com.ceos.beatbuddy.domain.recent_search.entity.SearchTypeEnum;
import com.ceos.beatbuddy.domain.search.dto.SearchDropDownDTO;
import com.ceos.beatbuddy.domain.search.dto.SearchPageResponseDTO;
import com.ceos.beatbuddy.domain.search.dto.SearchQueryResponseDTO;
import com.ceos.beatbuddy.domain.search.dto.SearchRankResponseDTO;
import com.ceos.beatbuddy.domain.search.exception.SearchErrorCode;
import com.ceos.beatbuddy.domain.venue.application.VenueSearchService;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.domain.venue.entity.VenueDocument;
import com.ceos.beatbuddy.domain.venue.repository.VenueRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
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
    private final MemberService memberService;
    private final VenueRepository venueRepository;
    private final HeartbeatRepository heartbeatRepository;
    private final RecentSearchService recentSearchService;
    private final VenueSearchService venueSearchService;

    private void saveSearchKeywordsToRedis(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return;

        try {
            // Pipeline을 사용하여 배치 처리로 성능 최적화
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                long expireAt = Instant.now().getEpochSecond() + 86400;
                
                for (String keyword : keywords) {
                    try {
                        // 1점 증가
                        redisTemplate.opsForZSet().incrementScore("ranking", keyword, 1.0);
                        // 만료 시간 설정
                        redisTemplate.opsForZSet().add("expire", keyword, (double) expireAt);
                    } catch (Exception e) {
                        log.error("Redis 키워드 저장 실패: keyword={}, error={}", keyword, e.getMessage(), e);
                    }
                }
                return null;
            });
        } catch (Exception e) {
            log.error("Redis Pipeline 실행 실패: keywords={}, error={}", keywords, e.getMessage(), e);
        }
    }


    // 인기검색어 리스트 1위~10위까지
    public List<SearchRankResponseDTO> searchRankList() {
        String key = "ranking";
        ZSetOperations<String, String> ZSetOperations = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> typedTuples = ZSetOperations.reverseRangeWithScores(key, 0, 9);  //score순으로 10개 보여줌
        return Objects.requireNonNull(typedTuples).stream().map(SearchRankResponseDTO::toSearchRankResponseDTO).collect(Collectors.toList());
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
        Member member = memberService.validateAndGetMember(memberId);

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
        
        // 정렬 기준에 따른 위도/경도 유효성 검증
        validateCoordinatesForSortCriteria(criteria, latitude, longitude);

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

        List<SearchQueryResponseDTO> searchQueryResponseDTOS = convertToSearchQueryResponseDTOs(
                venueList, venueMap, heartbeatVenueIds);

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

    private List<SearchQueryResponseDTO> convertToSearchQueryResponseDTOs(
            List<VenueDocument> venueList, 
            Map<Long, Venue> venueMap, 
            Set<Long> heartbeatVenueIds) {
        
        return venueList.stream()
                .map(venueDocument -> {
                    Venue venue = venueMap.get(venueDocument.getId());
                    if (venue == null) {
                        log.error("Venue not found for document ID: {} - data inconsistency detected", venueDocument.getId());
                        return null; // 전체 검색 실패를 방지하기 위해 null 반환 후 필터링
                    }

                    boolean isHeartbeat = heartbeatVenueIds.contains(venueDocument.getId());
                    List<String> tagList = createTagList(venueDocument);

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
                .filter(Objects::nonNull)
                .toList();
    }

    private List<String> createTagList(VenueDocument venueDocument) {
        List<String> tagList = new ArrayList<>(venueDocument.getGenre());
        tagList.addAll(venueDocument.getMood());
        tagList.add(venueDocument.getRegion());
        return tagList;
    }

    private void validateCoordinatesForSortCriteria(String criteria, Double latitude, Double longitude) {
        if ("가까운 순".equals(criteria)) {
            if (latitude == null || longitude == null) {
                throw new CustomException(SearchErrorCode.COORDINATES_REQUIRED_FOR_DISTANCE_SORT);
            }
            
            // 위도/경도 범위 검증 (대한민국 기준)
            if (latitude < 33.0 || latitude > 43.0) {
                throw new CustomException(SearchErrorCode.INVALID_LATITUDE_RANGE);
            }
            if (longitude < 124.0 || longitude > 132.0) {
                throw new CustomException(SearchErrorCode.INVALID_LONGITUDE_RANGE);
            }
        }
        // "인기순"의 경우 위도/경도가 null이어도 괜찮음
    }
}
