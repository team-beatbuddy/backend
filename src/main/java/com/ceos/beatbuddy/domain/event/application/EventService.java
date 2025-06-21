package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.dto.EventCreateRequestDTO;
import com.ceos.beatbuddy.domain.event.dto.EventListResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventAttendance;
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
import java.time.LocalDate;
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
    private final EventAttendanceRepository eventAttendanceRepository;

    @Transactional
    public EventResponseDTO addEvent(Long memberId, EventCreateRequestDTO eventCreateRequestDTO, MultipartFile image) throws IOException {
        Member member = memberService.validateAndGetMember(memberId);

        if (!(Objects.equals(member.getRole(), "ADMIN")) && !(Objects.equals(member.getRole(), "BUSINESS"))) {
            throw new CustomException(EventErrorCode.CANNOT_ADD_EVENT_UNAUTHORIZED_MEMBER);
        }

        // 엔티티 생성
        Event event = EventCreateRequestDTO.toEntity(eventCreateRequestDTO, member);

        // 베뉴가 등록되어있다면,
        if (eventCreateRequestDTO.getVenueId() != null) {
            Venue venue = venueInfoService.validateAndGetVenue(eventCreateRequestDTO.getVenueId());
            event.setVenue(venue);
        }

        // 이미지 setting
        String imageUrl = uploadUtil.upload(image, UploadUtil.BucketType.MEDIA, "event");
        event.setThumbImage(imageUrl);

        eventRepository.save(event);

        return EventResponseDTO.toDTO(event, null);
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
                .map(EventResponseDTO::toPastAndNowListDTO)
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

    public EventListResponseDTO getPastEvents(String sort, Integer page, Integer size, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        int offset = (page - 1) * size;
        // 지난 이벤트 조회
        List<Event> events = eventQueryRepository.findPastEvents(sort, offset, size);

        List<EventResponseDTO> dto = events.stream()
                .map(EventResponseDTO::toPastAndNowListDTO)
                .toList();

        int totalSize = eventQueryRepository.countPastEvents();

        return EventListResponseDTO.builder()
                .sort(sort)
                .page(page)
                .size(size)
                .totalSize(totalSize)
                .eventResponseDTOS(dto)
                .build();
    }

    @Transactional
    public EventResponseDTO likeEvent(Long eventId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        Event event = validateAndGet(eventId);

        EventInteractionId id = new EventInteractionId(memberId, eventId);

        if (eventLikeRepository.existsById(id)) {
            throw new CustomException(EventErrorCode.ALREADY_LIKE_EVENT);
        }

        EventLike eventLike = EventLike.toEntity(member, event);
        eventLikeRepository.save(eventLike);
        eventRepository.increaseLike(eventId);

        boolean liked = eventLikeRepository.existsById(new EventInteractionId(memberId, event.getId()));

        return EventResponseDTO.toDTO(event, liked);
    }

    @Transactional
    public EventResponseDTO deleteLikeEvent(Long eventId, Long memberId) {
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

        boolean liked = eventLikeRepository.existsById(new EventInteractionId(memberId, event.getId()));

        return EventResponseDTO.toDTO(event, liked);
    }

    @Transactional(readOnly = true)
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


    public Map<String, List<EventResponseDTO>> getMyPageEvents(Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        LocalDate today = LocalDate.now();
        Set<Event> myEvents = new HashSet<>();

        if (member.getRole().equals("BUSINESS")) {
            // 비즈니스 회원: 내가 생성한 이벤트
            myEvents.addAll(eventRepository.findAllByHost(member));
        } else {
            // 일반 회원: 좋아요 + 참석한 이벤트
            List<Event> likedEvents = eventLikeRepository.findByMember(member).stream()
                    .map(EventLike::getEvent)
                    .toList();
            List<Event> attendedEvents = eventAttendanceRepository.findByMember(member).stream()
                    .map(EventAttendance::getEvent)
                    .toList();

            myEvents.addAll(likedEvents);
            myEvents.addAll(attendedEvents);
        }

        // 상태별 분리 및 정렬
        List<EventResponseDTO> upcoming = myEvents.stream()
                .filter(e -> !e.getStartDate().isBefore(today))
                .sorted(Comparator.comparing(Event::getStartDate).reversed())
                .map(EventResponseDTO::toUpcomingListDTO)
                .toList();

        List<EventResponseDTO> past = myEvents.stream()
                .filter(e -> e.getStartDate().isBefore(today))
                .sorted(Comparator.comparing(Event::getStartDate).reversed())
                .map(EventResponseDTO::toPastAndNowListDTO)
                .toList();

        Map<String, List<EventResponseDTO>> result = new HashMap<>();
        result.put("upcoming", upcoming);
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
