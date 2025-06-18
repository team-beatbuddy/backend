package com.ceos.beatbuddy.domain.event.application;

import com.ceos.beatbuddy.domain.event.dto.EventCreateRequestDTO;
import com.ceos.beatbuddy.domain.event.dto.EventListResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.exception.EventErrorCode;
import com.ceos.beatbuddy.domain.event.repository.EventLikeRepository;
import com.ceos.beatbuddy.domain.event.repository.EventQueryRepository;
import com.ceos.beatbuddy.domain.event.repository.EventRepository;
import com.ceos.beatbuddy.domain.magazine.exception.MagazineErrorCode;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.domain.scrapandlike.entity.EventInteractionId;
import com.ceos.beatbuddy.domain.scrapandlike.entity.EventLike;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.domain.venue.exception.VenueErrorCode;
import com.ceos.beatbuddy.domain.venue.repository.VenueRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.UploadUtil;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final UploadUtil uploadUtil;
    private final MemberRepository memberRepository;
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final EventQueryRepository eventQueryRepository;
    private final EventLikeRepository eventLikeRepository;

    @Transactional
    public EventResponseDTO addEvent(Long memberId, EventCreateRequestDTO eventCreateRequestDTO, MultipartFile image) throws IOException {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        if (!(Objects.equals(member.getRole(), "ADMIN")) && !(Objects.equals(member.getRole(), "BUSINESS"))) {
            throw new CustomException(MagazineErrorCode.CANNOT_ADD_MAGAZINE_UNAUTHORIZED_MEMBER);
        }

        // 엔티티 생성
        Event event = EventCreateRequestDTO.toEntity(eventCreateRequestDTO, member);

        // 베뉴가 등록되어있다면,
        if (eventCreateRequestDTO.getVenueId() != null) {
            Venue venue = venueRepository.findById(eventCreateRequestDTO.getVenueId()).orElseThrow(
                    () -> new CustomException(VenueErrorCode.VENUE_NOT_EXIST)
            );

            event.setVenue(venue);
        }

        // 이미지 setting
        String imageUrl = uploadUtil.upload(image, UploadUtil.BucketType.MEDIA, "event");
        event.setThumbImage(imageUrl);

        eventRepository.save(event);

        return EventResponseDTO.toDTO(event, null);
    }


    public EventListResponseDTO getUpcomingEvents(String sort, Integer page, Integer size, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

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

    public EventListResponseDTO getPastEvents(String sort, Integer page, Integer size, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        int offset = (page - 1) * size;
        // 지난 이벤트 조회
        List<Event> events = eventQueryRepository.findPastEvents(sort, offset, size);

        List<EventResponseDTO> dto = events.stream()
                .map(EventResponseDTO::toPastListDTO)
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
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(EventErrorCode.NOT_FOUND_EVENT));

        EventInteractionId id = new EventInteractionId(memberId, eventId);

        if (eventLikeRepository.existsById(id)) {
            throw new CustomException(EventErrorCode.ALREADY_LIKE_EVENT);
        }

        EventLike eventLike = EventLike.toEntity(member, event);
        eventLikeRepository.save(eventLike);
        event.increaseLike();

        boolean liked = eventLikeRepository.existsById(new EventInteractionId(memberId, event.getId()));

        return EventResponseDTO.toDTO(event, liked);
    }

    @Transactional
    public EventResponseDTO deleteLikeEvent(Long eventId, Long memberId) {
        // 멤버 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        // 이벤트 조회
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(EventErrorCode.NOT_FOUND_EVENT));

        // 좋아요 여부 확인
        EventInteractionId id = new EventInteractionId(memberId, eventId);
        EventLike eventLike = eventLikeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_LIKE));

        eventLikeRepository.delete(eventLike);
        event.decreaseLike();

        boolean liked = eventLikeRepository.existsById(new EventInteractionId(memberId, event.getId()));

        return EventResponseDTO.toDTO(event, liked);
    }

    @Transactional(readOnly = true)
    public EventResponseDTO getEventDetail(Long eventId, Long memberId) {
        // 멤버 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        // 이벤트 조회
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(EventErrorCode.NOT_FOUND_EVENT));

        // 조회수 증가
        event.increaseView();

        // 좋아요 여부 확인
        boolean liked = eventLikeRepository.existsById(new EventInteractionId(memberId, eventId));

        return EventResponseDTO.toDTO(event, liked);
    }


//    boolean liked = eventLikeRepository.existsById(new EventInteractionId(memberId, event.getId()));
//    boolean scrapped = eventScrapRepository.existsById(new EventInteractionId(memberId, event.getId()));

}
