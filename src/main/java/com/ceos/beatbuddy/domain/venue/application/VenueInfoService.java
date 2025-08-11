package com.ceos.beatbuddy.domain.venue.application;

import com.ceos.beatbuddy.domain.admin.application.AdminService;
import com.ceos.beatbuddy.domain.coupon.repository.CouponRepository;
import com.ceos.beatbuddy.domain.event.dto.EventListResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.domain.event.repository.EventAttendanceRepository;
import com.ceos.beatbuddy.domain.event.repository.EventLikeRepository;
import com.ceos.beatbuddy.domain.event.repository.EventQueryRepository;
import com.ceos.beatbuddy.domain.event.repository.EventRepository;
import com.ceos.beatbuddy.domain.heartbeat.repository.HeartbeatRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.vector.entity.Vector;
import com.ceos.beatbuddy.domain.venue.dto.VenueInfoResponseDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueRequestDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueUpdateDTO;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.domain.venue.entity.VenueGenre;
import com.ceos.beatbuddy.domain.venue.entity.VenueMood;
import com.ceos.beatbuddy.domain.venue.exception.VenueErrorCode;
import com.ceos.beatbuddy.domain.venue.exception.VenueGenreErrorCode;
import com.ceos.beatbuddy.domain.venue.exception.VenueMoodErrorCode;
import com.ceos.beatbuddy.domain.venue.kakaoMap.KakaoLocalClient;
import com.ceos.beatbuddy.domain.venue.repository.VenueGenreRepository;
import com.ceos.beatbuddy.domain.venue.repository.VenueMoodRepository;
import com.ceos.beatbuddy.domain.venue.repository.VenueRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import com.ceos.beatbuddy.global.util.UploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueInfoService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final VenueRepository venueRepository;
    private final HeartbeatRepository heartbeatRepository;
    private final VenueGenreRepository venueGenreRepository;
    private final VenueMoodRepository venueMoodRepository;
    private final VenueSearchService venueSearchService;
    private final AdminService adminService;
    private final CouponRepository couponRepository;
    private final MemberService memberService;
    private final EventQueryRepository eventQueryRepository;
    private final EventLikeRepository eventLikeRepository;
    private final EventAttendanceRepository eventAttendanceRepository;
    private final EventRepository eventRepository;
    private final KakaoLocalClient kakaoLocalClient;

    private final UploadUtil uploadUtil;
    public List<Venue> getVenueInfoList() {
        return venueRepository.findAll();
    }

    public VenueInfoResponseDTO getVenueInfo(Long venueId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException(VenueErrorCode.VENUE_NOT_EXIST));
        boolean isHeartbeat = heartbeatRepository.findByMemberVenue(member, venue).isPresent();

        VenueGenre venueGenre = venueGenreRepository.findByVenue(venue)
                .orElseThrow(() -> new CustomException(VenueGenreErrorCode.VENUE_GENRE_NOT_EXIST));
        List<String> trueGenreElements = Vector.getTrueGenreElements(venueGenre.getGenreVector());

        VenueMood venueMood = venueMoodRepository.findByVenue(venue)
                .orElseThrow(() -> new CustomException(VenueMoodErrorCode.VENUE_MOOD_NOT_EXIST));
        List<String> trueMoodElements = Vector.getTrueMoodElements(venueMood.getMoodVector());
        String region = venue.getRegion().getText();

        List<String> tagList = new ArrayList<>(trueGenreElements);
        tagList.addAll(trueMoodElements);
        tagList.add(region);

        // 쿠폰 사용 가능한 여부
        boolean hasCoupon = couponRepository.existsByVenues_IdAndExpireDateIsAfter(venue.getId(), LocalDate.now());

        return VenueInfoResponseDTO.builder()
                .venue(venue)
                .isHeartbeat(isHeartbeat)
                .isCoupon(hasCoupon)
                .tagList(tagList)
                .build();
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
        venueSearchService.save(venue, null, null); // Venue 정보를 Elasticsearch에 저장
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

        return venueIds.stream()
                .map(this::validateAndGetVenue)
                .collect(Collectors.toList());
    }
}
