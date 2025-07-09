package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.dto.EventCreateRequestDTO;
import com.ceos.beatbuddy.domain.event.dto.EventListResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventUpdateRequestDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.exception.EventErrorCode;
import com.ceos.beatbuddy.domain.event.repository.EventLikeRepository;
import com.ceos.beatbuddy.domain.event.repository.EventQueryRepository;
import com.ceos.beatbuddy.domain.event.repository.EventRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.EventInteractionId;
import com.ceos.beatbuddy.domain.scrapandlike.entity.EventLike;
import com.ceos.beatbuddy.domain.venue.application.VenueInfoService;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.UploadUtil;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EventService {
    private final UploadUtil uploadUtil;
    private final MemberService memberService;
    private final VenueInfoService venueInfoService;
    private final EventRepository eventRepository;
    private final EventQueryRepository eventQueryRepository;
    private final EventLikeRepository eventLikeRepository;
    private final EventValidator eventValidator;

    @Transactional
    public EventResponseDTO addEvent(Long memberId, EventCreateRequestDTO eventCreateRequestDTO, List<MultipartFile> images) {
        Member member = memberService.validateAndGetMember(memberId);

        if (!(Objects.equals(member.getRole().toString(), "ADMIN")) && !(Objects.equals(member.getRole().toString(), "BUSINESS"))) {
            throw new CustomException(EventErrorCode.CANNOT_ADD_EVENT_UNAUTHORIZED_MEMBER);
        }

        // 이미지 5장 이하인지 확인
        if (images != null && images.size() > 5) {
            throw new CustomException(ErrorCode.TOO_MANY_IMAGES_5);
        }

        // 에약금 정보 확인
        eventValidator.validateReceiveMoney(eventCreateRequestDTO.isReceiveMoney(), eventCreateRequestDTO.getDepositAccount(), eventCreateRequestDTO.getDepositAmount());

        // 엔티티 생성
        Event event = EventCreateRequestDTO.toEntity(eventCreateRequestDTO, member);

        // 베뉴가 등록되어있다면,
        if (eventCreateRequestDTO.getVenueId() != null) {
            Venue venue = venueInfoService.validateAndGetVenue(eventCreateRequestDTO.getVenueId());
            event.setVenue(venue);
        }

        // 이미지 setting
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = uploadUtil.uploadImages(images, UploadUtil.BucketType.MEDIA, "event");
            event.setThumbImage(imageUrls.get(0));
            event.setImageUrls(imageUrls);
        }

        eventRepository.save(event);

        return EventResponseDTO.toDTO(event, false, true); // 좋아요 여부는 false, 내가 작성자 여부는 true로 설정
    }

    @Transactional
    public EventResponseDTO updateEvent(Long eventId, EventUpdateRequestDTO dto, Long memberId, List<MultipartFile> imageFiles) {
        Event event = validateAndGet(eventId);

        eventValidator.checkAccessForEvent(eventId, memberId);

        int afterDeletionCount = event.getImageUrls().size();
        int addCount = imageFiles != null ? imageFiles.size() : 0;

        // 삭제된 이미지 개수 반영
        if (dto.getDeleteImageUrls() != null) {
            afterDeletionCount -= dto.getDeleteImageUrls().size();
        }

        int totalAfterUpdate = afterDeletionCount + addCount;

        if (totalAfterUpdate > 5) {
            throw new CustomException(ErrorCode.TOO_MANY_IMAGES_5);
        }

        // 1. 기본 정보 업데이트
        event.updateEventInfo(
                eventValidator.isNotBlank(dto.getTitle()) ? dto.getTitle() : null,
                eventValidator.isNotBlank(dto.getContent()) ? dto.getContent() : null,
                dto.getStartDate(),
                dto.getEndDate(),
                eventValidator.isNotBlank(dto.getLocation()) ? dto.getLocation() : null,
                dto.getIsVisible(),
                dto.getTicketCost(),
                dto.getNotice(),
                dto.getRegion()
        );

        // 2. 참석자 정보 설정
        eventValidator.validateReceiveInfoConfig(dto);
        event.updateReceiveSettings(
                dto.getReceiveInfo(),
                dto.getReceiveName(),
                dto.getReceiveGender(),
                dto.getReceivePhoneNumber(),
                dto.getReceiveTotalCount(),
                dto.getReceiveSNSId()
        );

        // 3. 예약금 설정
        boolean willUpdateDeposit = dto.getReceiveMoney() != null || dto.getDepositAccount() != null || dto.getDepositAmount() != null;
        if (willUpdateDeposit) {
            boolean updatedReceiveMoney = dto.getReceiveMoney() != null ? dto.getReceiveMoney() : event.isReceiveMoney();
            String updatedAccount = dto.getDepositAccount() != null ? dto.getDepositAccount() : event.getDepositAccount();
            Integer updatedAmount = dto.getDepositAmount() != null ? dto.getDepositAmount() : event.getDepositAmount();

            eventValidator.validateReceiveMoney(updatedReceiveMoney, updatedAccount, updatedAmount);
            event.updateDepositSettings(dto.getReceiveMoney(), dto.getDepositAccount(), dto.getDepositAmount());
        }

        // 4. 이미지 삭제/추가
        if (dto.getDeleteImageUrls() != null && !dto.getDeleteImageUrls().isEmpty()) {
            removeImages(event, dto.getDeleteImageUrls());
        }

        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<String> imageUrls = uploadUtil.uploadImages(imageFiles, UploadUtil.BucketType.MEDIA, "event");
            event.getImageUrls().addAll(imageUrls);
        }

        // 5. 응답 생성
        boolean liked = eventLikeRepository.existsById(new EventInteractionId(memberId, eventId));
        return EventResponseDTO.toDTO(event, liked, true); // 좋아요 여부는 조회 후 설정, 내가 작성자 여부는 true로 설정
    }

    @Transactional
    public void removeImages(Event event, List<String> deleteFileIds) {
        List<String> existing = event.getImageUrls();

        // 1. 삭제 대상 필터링
        List<String> matched = existing.stream()
                .filter(deleteFileIds::contains)
                .toList();

        // 2. 유효성 검증
        if (matched.size() != deleteFileIds.size()) {
            throw new CustomException(EventErrorCode.FILE_NOT_FOUND);
        }

        // 3. S3 삭제
        uploadUtil.deleteImages(deleteFileIds, UploadUtil.BucketType.MEDIA);

        // 4. 연관관계 해제
        existing.removeAll(matched);
    }


    public EventListResponseDTO getUpcomingEvents(String sort, Integer page, Integer size, Long memberId, String region) {
        Member member = memberService.validateAndGetMember(memberId);

        int offset = (page - 1) * size;

        List<Event> events = eventQueryRepository.findUpcomingEvents(sort, offset, size, region);

        Set<Long> likedEventIds = new HashSet<>(eventLikeRepository.findLikedEventIdsByMember(member));

        List<EventResponseDTO> dto = events.stream()
                .map(event -> EventResponseDTO.toUpcomingListDTO(event, member.getId().equals(event.getHost().getId()),
                        likedEventIds.contains(event.getId())))
                .toList();

        int totalSize = eventQueryRepository.countUpcomingEvents(); // 총 개수 (페이지네이션용)

        return EventListResponseDTO.builder()
                .sort(sort)
                .page(page)
                .size(size)
                .totalSize(totalSize)
                .eventResponseDTOS(dto)
                .build();
    }

    public EventListResponseDTO getNowEvents(Integer page, Integer size, Long memberId, String region) {
        Member member = memberService.validateAndGetMember(memberId);

        String sort = "latest"; // 기본적으로 최신순으로 설정
        int offset = (page - 1) * size;

        List<Event> events = eventQueryRepository.findNowEvents(sort, offset, size, region);

        Set<Long> likedEventIds = new HashSet<>(eventLikeRepository.findLikedEventIdsByMember(member));

        List<EventResponseDTO> dto = events.stream()
                .map(event -> EventResponseDTO.toNowListDTO(event, member.getId().equals(event.getHost().getId()),
                        likedEventIds.contains(event.getId())))
                .toList();

        int totalSize = eventQueryRepository.countNowEvents(); // 총 개수 (페이지네이션용)

        return EventListResponseDTO.builder()
                .sort(sort)
                .page(page)
                .size(size)
                .totalSize(totalSize)
                .eventResponseDTOS(dto)
                .build();
    }

    public EventListResponseDTO getPastEvents(int page, int limit, Long memberId, String region) {
        Member member = memberService.validateAndGetMember(memberId);

        int offset = (page - 1) * limit;

        String sort = "latest"; // 기본적으로 최신순으로 설정

        Set<Long> likedEventIds = new HashSet<>(eventLikeRepository.findLikedEventIdsByMember(member));

        // 최신순 (기존 방식)
        List<Event> events = eventQueryRepository.findPastEvents(sort, offset, limit, region);
        int total = eventQueryRepository.countPastEvents();

        List<EventResponseDTO> responseList = events.stream()
                .map(event -> EventResponseDTO.toPastListDTO(event, member.getId().equals(event.getHost().getId()),
                        likedEventIds.contains(event.getId())))
                .toList();

        return EventListResponseDTO.builder()
                .sort(sort)
                .totalSize(total)
                .page(page)
                .size(limit)
                .eventResponseDTOS(responseList)
                .build();
    }

    @Transactional
    public void likeEvent(Long eventId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        Event event = validateAndGet(eventId);

        EventInteractionId id = new EventInteractionId(memberId, eventId);

        if (eventLikeRepository.existsById(id)) {
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        }

        EventLike eventLike = EventLike.toEntity(member, event);
        eventLikeRepository.save(eventLike);
        eventRepository.increaseLike(eventId);
    }

    @Transactional
    public void deleteLikeEvent(Long eventId, Long memberId) {
        // 멤버 조회
        Member member = memberService.validateAndGetMember(memberId);

        // 이벤트 조회
        Event event = validateAndGet(eventId);


        // 좋아요 여부 확인
        EventInteractionId id = new EventInteractionId(memberId, eventId);
        EventLike eventLike = eventLikeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_LIKE));

        eventLikeRepository.delete(eventLike);
        eventRepository.decreaseLike(eventId);
    }

    @Transactional
    public EventResponseDTO getEventDetail(Long eventId, Long memberId) {
        // 멤버 조회
        Member member = memberService.validateAndGetMember(memberId);

        // 이벤트 조회
        Event event = validateAndGet(eventId);

        // 조회수 증가
        eventRepository.increaseViews(eventId);

        // 좋아요 여부 확인
        boolean liked = eventLikeRepository.existsById(new EventInteractionId(memberId, eventId));

        return EventResponseDTO.toDTO(event, liked, member.getId().equals(event.getHost().getId()));
    }

    public Event validateAndGet(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(EventErrorCode.NOT_FOUND_EVENT));
    }


//    boolean liked = eventLikeRepository.existsById(new EventInteractionId(memberId, event.getId()));
//    boolean scrapped = eventScrapRepository.existsById(new EventInteractionId(memberId, event.getId()));

}
