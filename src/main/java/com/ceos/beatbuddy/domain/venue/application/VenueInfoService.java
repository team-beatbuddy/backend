package com.ceos.beatbuddy.domain.venue.application;

import com.ceos.beatbuddy.domain.admin.application.AdminService;
import com.ceos.beatbuddy.domain.event.dto.EventListResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.domain.event.repository.EventAttendanceRepository;
import com.ceos.beatbuddy.domain.event.repository.EventLikeRepository;
import com.ceos.beatbuddy.domain.event.repository.EventQueryRepository;
import com.ceos.beatbuddy.domain.event.repository.EventRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.venue.dto.VenueInfoResponseDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueRequestDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueUpdateDTO;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.domain.venue.exception.VenueErrorCode;
import com.ceos.beatbuddy.domain.venue.kakaoMap.KakaoLocalClient;
import com.ceos.beatbuddy.domain.venue.repository.VenueInfoQueryRepository;
import com.ceos.beatbuddy.domain.venue.repository.VenueRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import com.ceos.beatbuddy.global.translation.TranslationService;
import com.ceos.beatbuddy.global.util.UploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class VenueInfoService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final VenueRepository venueRepository;
    private final VenueSearchService venueSearchService;
    private final AdminService adminService;
    private final MemberService memberService;
    private final EventQueryRepository eventQueryRepository;
    private final EventLikeRepository eventLikeRepository;
    private final EventAttendanceRepository eventAttendanceRepository;
    private final EventRepository eventRepository;
    private final KakaoLocalClient kakaoLocalClient;
    private final VenueInfoQueryRepository venueInfoQueryRepository;
    private final TranslationService translationService;

    private final UploadUtil uploadUtil;
    public List<Venue> getVenueInfoList() {
        return venueRepository.findAll();
    }

    public VenueInfoResponseDTO getVenueInfo(Long venueId, Long memberId) {
        return getVenueInfo(venueId, memberId, "ko");
    }
    
    public VenueInfoResponseDTO getVenueInfo(Long venueId, Long memberId, String locale) {
        memberService.validateAndGetMember(memberId);
        
        var optimizedData = venueInfoQueryRepository.findVenueInfoOptimized(venueId, memberId);
        if (optimizedData == null) {
            throw new CustomException(VenueErrorCode.VENUE_NOT_EXIST);
        }

        return VenueInfoResponseDTO.forLocale(
                optimizedData.getVenue(),
                optimizedData.isHeartbeat(),
                optimizedData.isHasCoupon(),
                optimizedData.getTagList(),
                locale
        );
    }

    @Transactional
    public Long deleteVenueInfo(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException(VenueErrorCode.VENUE_NOT_EXIST));
        uploadUtil.deleteImage(venue.getLogoUrl(), UploadUtil.BucketType.VENUE);
        uploadUtil.deleteImages(venue.getBackgroundUrl(), UploadUtil.BucketType.VENUE);

        Long deletedCount = venueRepository.deleteByVenueId(venueId);
        venueSearchService.delete(venueId); // DB 삭제 후 ES 삭제
        return deletedCount;
    }

    @Transactional
    public Long addVenueInfo(VenueRequestDTO request, MultipartFile logoImage, List<MultipartFile> backgroundImage, Long memberId) {
        // 베뉴 등록 시, 멤버가 ADMIN인지 확인
        adminService.validateAdmin(memberId);

        // backgroundImage가 5개 초과면 예외
        if (backgroundImage.size() > 5) {
            throw new CustomException(ErrorCode.TOO_MANY_IMAGES_5);
        }

        String logoImageUrl = null;
        List<String> backgroundImageUrls = new ArrayList<>();

        if (logoImage != null && !logoImage.isEmpty()) {
            logoImageUrl = uploadUtil.upload(logoImage, UploadUtil.BucketType.VENUE, null);
        }

        if (backgroundImage != null && !backgroundImage.isEmpty()) {
            backgroundImageUrls = uploadUtil.uploadImages(backgroundImage, UploadUtil.BucketType.VENUE, null);
        }

        Venue venue = venueRepository.save(Venue.of(request, logoImageUrl, backgroundImageUrls));

        // 비동기로 번역 처리
        translateVenueFields(venue);

        kakaoLocalClient.getCoordinateFromAddress(request.getAddress())
                .subscribe(coord -> {
                    venueRepository.updateLatLng(venue.getId(), coord.getY(), coord.getX());
                });

        venueSearchService.save(venue, null, null); // Venue 정보를 Elasticsearch에 저장

        return venue.getId();
    }

    @Transactional
    public void updateVenueInfo(Long venueId, VenueUpdateDTO dto, MultipartFile logoImage, List<MultipartFile> backgroundImages, Long memberId)  {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException(VenueErrorCode.VENUE_NOT_EXIST));

        // 베뉴 수정 시, 멤버가 ADMIN인지 확인
        adminService.validateAdmin(memberId);

        String logoImageUrl = venue.getLogoUrl();
        List<String> currentImageUrls = venue.getBackgroundUrl();
        List<String> deleteImageUrls = dto.getDeleteImageUrls();
        List<String> existingImages = new ArrayList<>(currentImageUrls);

        // 총 이미지 수 유효성 검사 (기존 + 업로드 예정 <= 5)
        int newImageCount = (backgroundImages != null) ? backgroundImages.size() : 0;
        int finalImageCount = existingImages.size() + newImageCount;
        if (finalImageCount > 5) {
            throw new CustomException(ErrorCode.TOO_MANY_IMAGES_5);
        }

        // 유효성 검사 - 삭제 대상이 실제 존재하는 이미지인지
        if (deleteImageUrls != null && !deleteImageUrls.isEmpty()) {
            if (!new HashSet<>(existingImages).containsAll(deleteImageUrls)) {
                throw new CustomException(ErrorCode.NOT_FOUND_IMAGE);
            }

            // S3에서 삭제
            uploadUtil.deleteImages(deleteImageUrls, UploadUtil.BucketType.VENUE);

            // 기존 이미지 리스트에서 삭제
            existingImages.removeAll(deleteImageUrls);
        }

        // 주소가 변경되었다면, 위도 경도 업데이트
        if (!venue.getAddress().equals(dto.getVenueRequestDTO().getAddress())) {
            kakaoLocalClient.getCoordinateFromAddress(dto.getVenueRequestDTO().getAddress())
                    .flatMap(coord -> {
                        venueRepository.updateLatLng(venueId, coord.getY(), coord.getX());
                        return Mono.empty();
                    })
                    .block(Duration.ofSeconds(10)); // 10초 타임아웃 설정
        }

        // 로고 이미지 변경
        if (logoImage != null) {
            uploadUtil.deleteImage(logoImageUrl, UploadUtil.BucketType.VENUE);
            logoImageUrl = uploadUtil.upload(logoImage, UploadUtil.BucketType.VENUE, null);
            venue.updateLogoUrl(logoImageUrl);
        }

        // 새로운 이미지 업로드
        if (backgroundImages!= null && !backgroundImages.isEmpty()) {
            List<String> newImageUrls = uploadUtil.uploadImages(backgroundImages, UploadUtil.BucketType.VENUE, null);
            existingImages.addAll(newImageUrls);
        }

        // venue에 최종 이미지 리스트 반영
        venue.updateBackgroundUrl(existingImages);

        venue.update(dto);
        
        // 업데이트된 내용 번역
        translateVenueFields(venue);
        
        venueSearchService.save(venue, null, null); // Venue 정보를 Elasticsearch에 저장
    }

    @Transactional
    public void translateVenueFields(Venue venue) {
        try {
            Map<String, String> fieldsToTranslate = new HashMap<>();
            
            if (venue.getDescription() != null && !venue.getDescription().trim().isEmpty()) {
                fieldsToTranslate.put("description", venue.getDescription());
            }
            if (venue.getAddress() != null && !venue.getAddress().trim().isEmpty()) {
                fieldsToTranslate.put("address", venue.getAddress());
            }
            if (venue.getEntranceNotice() != null && !venue.getEntranceNotice().trim().isEmpty()) {
                fieldsToTranslate.put("entranceNotice", venue.getEntranceNotice());
            }
            if (venue.getNotice() != null && !venue.getNotice().trim().isEmpty()) {
                fieldsToTranslate.put("notice", venue.getNotice());
            }
            
            if (!fieldsToTranslate.isEmpty()) {
                Map<String, String> translations = translationService.translateBatch(fieldsToTranslate);
                
                venue.updateTranslations(
                    translations.get("description"),
                    translations.get("address"),
                    translations.get("entranceNotice"),
                    translations.get("notice")
                );
            }
        } catch (Exception e) {
            log.error("번역 중 오류 발생: venueId={}, error={}", venue.getId(), e.getMessage(), e);
        }
    }

    public EventListResponseDTO getVenueEventsLatest(Long venueId, Long memberId, int page, int size, boolean isPast) {
        // 멤버 유효성 검사
        Member member = memberService.validateAndGetMember(memberId);

        // Venue 유효성 검사
        validateAndGetVenue(venueId);

        // 페이지 유효성 검사
        if (page < 1) {
            throw new CustomException(ErrorCode.PAGE_OUT_OF_BOUNDS);
        }

        Set<Long> likedEventIds = eventLikeRepository.findLikedEventIdsByMember(member);
        Set<Long> attendingEventIds = eventAttendanceRepository.findByMember(member).stream()
                .map(att -> att.getEvent().getId())
                .collect(Collectors.toSet());

        Pageable pageable = PageRequest.of(page - 1, size);

        var pagedEvents = isPast
                ? eventQueryRepository.findVenuePastEvents(venueId, pageable)
                : eventQueryRepository.findVenueOngoingOrUpcomingEvents(venueId, pageable);

        List<EventResponseDTO> events = pagedEvents.stream()
                .map(event -> EventResponseDTO.toListDTO(
                        event,
                        event.getHost().getId().equals(memberId),
                        likedEventIds.contains(event.getId()),
                        attendingEventIds.contains(event.getId())
                ))
                .collect(Collectors.toList());

        int totalSize = isPast
                ? eventQueryRepository.countVenuePastEvents(venueId)
                : eventQueryRepository.countVenueOngoingOrUpcomingEvents(venueId);

        return EventListResponseDTO.builder()
                .page(page)
                .size(size)
                .totalSize(totalSize)
                .sort("latest")
                .eventResponseDTOS(events)
                .build();

    }

    public EventListResponseDTO getVenueEventsByPopularity(Long venueId, Long memberId, int page, int size) {
        // 멤버 유효성 검사
        Member member = memberService.validateAndGetMember(memberId);

        // Venue 유효성 검사
        validateAndGetVenue(venueId);

        // 페이지 유효성 검사
        if (page < 1) {
            throw new CustomException(ErrorCode.PAGE_OUT_OF_BOUNDS);
        }

        Set<Long> likedEventIds = eventLikeRepository.findLikedEventIdsByMember(member);
        Set<Long> attendingEventIds = eventAttendanceRepository.findByMember(member).stream()
                .map(att -> att.getEvent().getId())
                .collect(Collectors.toSet());

        List<EventResponseDTO> events = eventQueryRepository.findEventsByVenueOrderByPopularity(venueId, PageRequest.of(page-1, size))
                .stream()
                .map(event -> EventResponseDTO.toListDTO(event, event.getHost().getId().equals(memberId),
                        likedEventIds.contains(event.getId()),
                        attendingEventIds.contains(event.getId())))
                .collect(Collectors.toList());

        return EventListResponseDTO.builder()
                .page(page)
                .size(size)
                .totalSize(eventRepository.countAllByVenue_Id(venueId))
                .sort("popular")
                .eventResponseDTOS(events)
                .build();
    }

    public Venue validateAndGetVenue(Long venueId) {
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException(VenueErrorCode.VENUE_NOT_EXIST));
    }

    public List<Venue> validateAndGetVenues(List<Long> venueIds) {
        if (venueIds == null || venueIds.isEmpty()) {
            throw new CustomException(VenueErrorCode.VENUE_NOT_EXIST);
        }

        List<Venue> venues = venueRepository.findByIdIn(venueIds);
        
        if (venues.size() != venueIds.size()) {
            throw new CustomException(VenueErrorCode.VENUE_NOT_EXIST);
        }

        return venues;
    }

    /**
     * 번역되지 않은 기존 venue들에 대해 번역 동기화 수행
     */
    @Transactional
    public void syncVenueTranslations() {
        log.info("Venue 번역 동기화 작업을 시작합니다.");
        
        List<Venue> venuesNeedingTranslation = findVenuesNeedingTranslation();
        log.info("번역이 필요한 Venue 수: {}", venuesNeedingTranslation.size());
        
        if (venuesNeedingTranslation.isEmpty()) {
            log.info("번역이 필요한 Venue가 없습니다.");
            return;
        }
        
        int batchSize = 10; // 한 번에 처리할 venue 개수
        int totalBatches = (int) Math.ceil((double) venuesNeedingTranslation.size() / batchSize);
        
        for (int i = 0; i < totalBatches; i++) {
            int start = i * batchSize;
            int end = Math.min(start + batchSize, venuesNeedingTranslation.size());
            List<Venue> batch = venuesNeedingTranslation.subList(start, end);
            
            log.info("배치 {}/{} 처리 중... ({}-{})", i + 1, totalBatches, start + 1, end);
            
            processBatch(batch);
            
            // API 요청 제한을 위해 잠시 대기
            try {
                Thread.sleep(1000); // 1초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("번역 동기화 중 인터럽트 발생", e);
                break;
            }
        }
        
        log.info("Venue 번역 동기화 작업이 완료되었습니다.");
    }
    
    /**
     * 번역이 필요한 venue들을 찾아서 반환
     */
    private List<Venue> findVenuesNeedingTranslation() {
        return venueRepository.findAll().stream()
                .filter(this::needsTranslation)
                .collect(Collectors.toList());
    }
    
    /**
     * venue가 번역이 필요한지 확인
     */
    private boolean needsTranslation(Venue venue) {
        // description이 있는데 영어 번역이 없는 경우
        if (hasContent(venue.getDescription()) && !hasContent(venue.getDescriptionEng())) {
            return true;
        }
        
        // address가 있는데 영어 번역이 없는 경우
        if (hasContent(venue.getAddress()) && !hasContent(venue.getAddressEng())) {
            return true;
        }
        
        // entranceNotice가 있는데 영어 번역이 없는 경우
        if (hasContent(venue.getEntranceNotice()) && !hasContent(venue.getEntranceNoticeEng())) {
            return true;
        }
        
        // notice가 있는데 영어 번역이 없는 경우
        if (hasContent(venue.getNotice()) && !hasContent(venue.getNoticeEng())) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 문자열이 유효한 내용을 가지고 있는지 확인
     */
    private boolean hasContent(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * venue 배치를 처리하여 번역 수행
     */
    private void processBatch(List<Venue> venues) {
        for (Venue venue : venues) {
            try {
                translateVenueFields(venue);
                log.debug("Venue ID {} 번역 완료", venue.getId());
            } catch (Exception e) {
                log.error("Venue ID {} 번역 중 오류 발생: {}", venue.getId(), e.getMessage(), e);
            }
        }
    }
    
    /**
     * 특정 venue ID에 대해서만 번역 동기화 수행
     */
    @Transactional
    public void syncVenueTranslation(Long venueId) {
        log.info("Venue ID {} 번역 동기화 시작", venueId);
        
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException(VenueErrorCode.VENUE_NOT_EXIST));
        
        if (!needsTranslation(venue)) {
            log.info("Venue ID {} 는 번역이 필요하지 않습니다.", venueId);
            return;
        }
        
        translateVenueFields(venue);
        log.info("Venue ID {} 번역 동기화 완료", venueId);
    }
    
    /**
     * 번역 동기화 상태를 확인
     */
    public Map<String, Object> getTranslationSyncStatus() {
        List<Venue> allVenues = venueRepository.findAll();
        List<Venue> venuesNeedingTranslation = allVenues.stream()
                .filter(this::needsTranslation)
                .collect(Collectors.toList());
        
        Map<String, Object> status = new HashMap<>();
        status.put("totalVenues", allVenues.size());
        status.put("venuesNeedingTranslation", venuesNeedingTranslation.size());
        status.put("translatedVenues", allVenues.size() - venuesNeedingTranslation.size());
        status.put("translationProgress", allVenues.isEmpty() ? 100.0 : 
            ((double)(allVenues.size() - venuesNeedingTranslation.size()) / allVenues.size()) * 100.0);
        
        // 번역이 필요한 venue ID 목록도 포함
        status.put("venueIdsNeedingTranslation", 
            venuesNeedingTranslation.stream().map(Venue::getId).collect(Collectors.toList()));
        
        return status;
    }
}
