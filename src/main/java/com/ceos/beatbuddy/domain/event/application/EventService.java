package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.dto.EventCreateRequestDTO;
import com.ceos.beatbuddy.domain.event.dto.EventListResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventUpdateRequestDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.exception.EventErrorCode;
import com.ceos.beatbuddy.domain.event.repository.EventAttendanceRepository;
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

import java.io.IOException;
import java.util.*;

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
    public EventResponseDTO addEvent(Long memberId, EventCreateRequestDTO eventCreateRequestDTO, List<MultipartFile> images) throws IOException {
        Member member = memberService.validateAndGetMember(memberId);

        if (!(Objects.equals(member.getRole().toString(), "ADMIN")) && !(Objects.equals(member.getRole().toString(), "BUSINESS"))) {
            throw new CustomException(EventErrorCode.CANNOT_ADD_EVENT_UNAUTHORIZED_MEMBER);
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

        return EventResponseDTO.toDTO(event, false);
    }

    @Transactional
    public EventResponseDTO updateEvent(Long eventId, EventUpdateRequestDTO dto, Long memberId, List<MultipartFile> imageFiles) {
        Event event = validateAndGet(eventId);

        eventValidator.checkAccessForEvent(eventId, memberId);

        if (eventValidator.isNotBlank(dto.getTitle())) event.setTitle(dto.getTitle());
        if (eventValidator.isNotBlank(dto.getContent())) event.setContent(dto.getContent());
        if (dto.getStartDate() != null) event.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) event.setEndDate(dto.getEndDate());
        if (eventValidator.isNotBlank(dto.getLocation())) event.setLocation(dto.getLocation());

        if (dto.getIsVisible() != null) event.setVisible(dto.getIsVisible());

        // userInfo 가 false 면 나머지도 false 여야 함.
        eventValidator.validateReceiveInfoConfig(dto);
//      1. receiveInfo 세팅
        if (dto.getReceiveInfo() != null) {
            event.setReceiveInfo(dto.getReceiveInfo());

            // 만약 false로 바뀌면, 하위 항목 모두 false로 초기화
            if (!dto.getReceiveInfo()) {
                event.setReceiveName(false);
                event.setReceiveGender(false);
                event.setReceivePhoneNumber(false);
                event.setReceiveTotalCount(false);
                event.setReceiveSNSId(false);
            }
        }

        // 2. receiveInfo가 true일 경우, 하위 항목을 선택적으로 적용
        if (Boolean.TRUE.equals(event.isReceiveInfo())) {
            if (dto.getReceiveName() != null) event.setReceiveName(dto.getReceiveName());
            if (dto.getReceiveGender() != null) event.setReceiveGender(dto.getReceiveGender());
            if (dto.getReceivePhoneNumber() != null) event.setReceivePhoneNumber(dto.getReceivePhoneNumber());
            if (dto.getReceiveTotalCount() != null) event.setReceiveTotalCount(dto.getReceiveTotalCount());
            if (dto.getReceiveSNSId() != null) event.setReceiveSNSId(dto.getReceiveSNSId());
        }

        // 기존에 money를 안 받겠다고 했으면, account랑 amount 가 없을 테니까, true 로 바꿨을 때라면 account. amount가 다 있어야겠지
        // 기존에 money를 받았는데, 안 받겠다고 했으면, account랑 amount를 지우면 됨.
        // 1. receiveMoney 상태 변경 반영
        if (dto.getReceiveMoney() != null) {
            event.setReceiveMoney(dto.getReceiveMoney());
        }

        // 2. 반영 후의 상태 기반 값 결정
        boolean updatedReceiveMoney = dto.getReceiveMoney() != null ? dto.getReceiveMoney() : event.isReceiveMoney();
        String updatedAccount = dto.getDepositAccount() != null ? dto.getDepositAccount() : event.getDepositAccount();
        Integer updatedAmount = dto.getDepositAmount() != null ? dto.getDepositAmount() : event.getDepositAmount();

        // 3. 상태에 따라 처리
        if (dto.getReceiveMoney() != null || dto.getDepositAccount() != null || dto.getDepositAmount() != null) {
            if (updatedReceiveMoney) {
                // true일 때는 검증 및 설정
                eventValidator.validateReceiveMoney(true, updatedAccount, updatedAmount);

                if (dto.getDepositAccount() != null) {
                    event.setDepositAccount(dto.getDepositAccount());
                }
                if (dto.getDepositAmount() != null) {
                    event.setDepositAmount(dto.getDepositAmount());
                }
            } else {
                // false일 경우에는 해당 필드 초기화
                event.setDepositAccount(null);
                event.setDepositAmount(null);
            }
        }

        List<String> deleteFiles = dto.getDeleteImageUrls();

        if (deleteFiles != null && !deleteFiles.isEmpty()) {
            removeImages(event, deleteFiles);
        }

        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<String> imageUrls = uploadUtil.uploadImages(imageFiles, UploadUtil.BucketType.MEDIA, "event");
            event.getImageUrls().addAll(imageUrls);
        }

        boolean liked = eventLikeRepository.existsById(new EventInteractionId(memberId, eventId));

        // 6. 응답 생성
        return EventResponseDTO.toDTO(event, liked);
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


    public EventListResponseDTO getUpcomingEvents(String sort, Integer page, Integer size, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        int offset = (page - 1) * size;

        List<Event> events = eventQueryRepository.findUpcomingEvents(sort, offset, size);

        List<EventResponseDTO> dto = events.stream()
                .map(EventResponseDTO::toUpcomingListDTO)
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

    public EventListResponseDTO getNowEvents(String sort, Integer page, Integer size, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        int offset = (page - 1) * size;

        List<Event> events = eventQueryRepository.findNowEvents(sort, offset, size);

        List<EventResponseDTO> dto = events.stream()
                .map(EventResponseDTO::toNowListDTO)
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

    public EventListResponseDTO getPastEvents(String sort, int page, int limit, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        int offset = (page - 1) * limit;

        if ("popular".equals(sort)) {
            // 월별 인기순으로 전체 데이터 가져와서 그룹핑

            Map<String, List<Event>> groupedEvents = eventQueryRepository.findPastEventsGroupedByMonth();

            List<EventResponseDTO> groupedResponse = groupedEvents.entrySet().stream()
                    .sorted(Map.Entry.<String, List<Event>>comparingByKey().reversed()) // 최신 월이 먼저 오도록
                    .map(entry -> EventResponseDTO.groupByMonth(entry.getKey(), entry.getValue()))
                    .toList();

            return EventListResponseDTO.builder()
                    .sort(sort)
                    .eventResponseDTOS(groupedResponse)
                    .totalSize(groupedResponse.size()) // 페이지 처리 안 함
                    .build();
        }

        // 최신순 (기존 방식)
        List<Event> events = eventQueryRepository.findPastEvents(sort, offset, limit);
        int total = eventQueryRepository.countPastEvents();

        List<EventResponseDTO> responseList = events.stream()
                .map(EventResponseDTO::toPastListDTO)
                .toList();

        return EventListResponseDTO.builder()
                .sort(sort)
                .totalSize(total)
                .page(offset)
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
        event.increaseView();

        // 좋아요 여부 확인
        boolean liked = eventLikeRepository.existsById(new EventInteractionId(memberId, eventId));

        return EventResponseDTO.toDTO(event, liked);
    }

    // 내가 작성한 이벤트
    public Map<String, List<EventResponseDTO>> getMyEvents(Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        List<EventResponseDTO> upcoming = eventQueryRepository.findMyUpcomingEvents(member)
                .stream()
                .map(EventResponseDTO::toUpcomingListDTO)
                .toList();

        List<EventResponseDTO> now = eventQueryRepository.findMyNowEvents(member)
                .stream()
                .map(EventResponseDTO::toNowListDTO)
                .toList();

        List<EventResponseDTO> past = eventQueryRepository.findMyPastEvents(member)
                .stream()
                .map(EventResponseDTO::toPastListDTO)
                .toList();

        Map<String, List<EventResponseDTO>> result = new HashMap<>();
        result.put("upcoming", upcoming);
        result.put("now", now);
        result.put("past", past);

        return result;
    }

    public Event validateAndGet(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(EventErrorCode.NOT_FOUND_EVENT));
    }


//    boolean liked = eventLikeRepository.existsById(new EventInteractionId(memberId, event.getId()));
//    boolean scrapped = eventScrapRepository.existsById(new EventInteractionId(memberId, event.getId()));

}
