package com.ceos.beatbuddy.domain.magazine.application;

import com.ceos.beatbuddy.domain.event.application.EventService;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.magazine.dto.*;
import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.magazine.exception.MagazineErrorCode;
import com.ceos.beatbuddy.domain.magazine.repository.MagazineQueryRepository;
import com.ceos.beatbuddy.domain.magazine.repository.MagazineRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.constant.Role;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.MagazineInteractionId;
import com.ceos.beatbuddy.domain.scrapandlike.repository.MagazineLikeRepository;
import com.ceos.beatbuddy.domain.venue.application.VenueInfoService;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.UploadUtil;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MagazineService {
    private final MagazineRepository magazineRepository;
    private final MemberService memberService;
    private final MagazineLikeRepository magazineLikeRepository;
    private final MagazineQueryRepository magazineQueryRepository;
    private final EventService eventService;
    private final VenueInfoService venueInfoService;
    private final MagazineValidator magazineValidator;

    private final UploadUtil uploadUtil;
    /**
     * 매거진을 생성합니다. (관리자 또는 비즈니스 회원만 가능)
     *
     * @param memberId       매거진을 작성하는 회원의 ID
     * @param dto            매거진 생성 요청 DTO
     * @param images         첨부 이미지 리스트
     * @param thumbnailImage 썸네일 이미지
     * @return 생성된 매거진의 상세 DTO
     * @throws CustomException 권한이 없는 회원이 요청한 경우
     */
    @Transactional
    public MagazineDetailDTO addMagazine(Long memberId, MagazineRequestDTO dto, List<MultipartFile> images, MultipartFile thumbnailImage) throws RuntimeException {
        Member member = memberService.validateAndGetMember(memberId);
        // 고정된 매거진이라면 숫자가 있어야 함. 유효성 검사 (또한, 따로 isPinned 된 매거진 중 같은 숫자일 수 없음)
        validatePinnedMagazine(dto.isPinned(), dto.getOrderInHome());

        // 이미지 20장 넘지 않도록 체크
        if (images != null && images.size() > 20) {
            throw new CustomException(ErrorCode.TOO_MANY_IMAGES_20);
        }

        if (member.getRole() != Role.ADMIN && member.getRole() != Role.BUSINESS) {
            throw new CustomException(MagazineErrorCode.CANNOT_ADD_MAGAZINE_UNAUTHORIZED_MEMBER);
        }

        // 엔티티로 변경
        Magazine entity = MagazineRequestDTO.toEntity(dto, member);

        // 이벤트 유효성 검사
        if (dto.getEventId() != null) {
            Event event = eventService.validateAndGet(dto.getEventId());
            entity.setEvent(event);
        }

        // 관련 베뉴 존재 시, 유효성 검사 및 추가
        if (dto.getVenueIds() != null && !dto.getVenueIds().isEmpty()) {
            List<Venue> venues = dto.getVenueIds().stream().map(venueInfoService::validateAndGetVenue).collect(Collectors.toList());
            entity.setVenues(venues);
        }

        // 썸네일 업로드
        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            String thumbImageUrl = uploadUtil.upload(thumbnailImage, UploadUtil.BucketType.MEDIA, "magazine");
            entity.setThumbImage(thumbImageUrl);
        }

        // 이미지 업로드 (이미지가 비어있는 경우는 제외)
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = uploadUtil.uploadImages(images, UploadUtil.BucketType.MEDIA, "magazine");
            // 엔티티에 이미지 세팅
            entity.setImageUrls(imageUrls);
        }

        magazineRepository.save(entity);

        return MagazineDetailDTO.toDTO(entity, false, true); // 작성자는 항상 좋아요가 false로 설정됨
    }
    /**
     * 홈 화면에 노출할 매거진 목록을 조회합니다. (표시 가능한 매거진만 반환) 5개 반환
     *
     * @param memberId 매거진 목록을 요청하는 회원의 ID
     * @return 매거진 홈 카드 정보를 담은 DTO 리스트
     * @throws CustomException 회원이 존재하지 않을 경우
     */
    public List<MagazineHomeResponseDTO> readHomeMagazines(Long memberId) {
        memberService.validateAndGetMember(memberId);

        List<Magazine> magazines = magazineQueryRepository.findPinnedMagazines();

        // 좋아요 여부 확인
        Set<Long> likedMagazineIds = magazineLikeRepository
                .findAllByMember_IdAndMagazine_IdIn(
                        memberId,
                        magazines.stream().map(Magazine::getId).toList()
                )
                .stream()
                .map(like -> like.getMagazine().getId())
                .collect(Collectors.toSet());

        // 매거진 + 좋아요 여부 + 본인 글 여부 DTO 매핑
        return magazines.stream()
                .map(magazine -> MagazineHomeResponseDTO.toDTO(
                        magazine,
                        likedMagazineIds.contains(magazine.getId()),
                        magazine.getMember().getId().equals(memberId) // 본인 글 여부
                ))
                .toList();
    }


    /**
     * 표시 가능한(visible) 매거진의 상세 정보를 조회하고, 조회수를 증가시킵니다.
     *
     * @param memberId 매거진 상세 정보를 요청하는 회원의 ID
     * @param magazineId 조회할 매거진의 ID
     * @return 매거진을 나타내는 상세 DTO
     * @throws CustomException 회원 또는 매거진이 존재하지 않거나, 매거진이 표시 불가능한 경우
     */
    public MagazineDetailDTO readDetailMagazine(Long memberId, Long magazineId) {
        memberService.validateAndGetMember(memberId);

        Magazine magazine = magazineValidator.validateAndGetMagazineVisibleTrue(magazineId);

        // 조회수 증가
        magazine.increaseView();

        // 매거진 좋아요 여부 확인
        boolean isLiked = magazineLikeRepository.existsById(
                MagazineInteractionId.builder().memberId(memberId).magazineId(magazineId).build());

        return MagazineDetailDTO.toDTO(magazine, isLiked, magazine.getMember().getId().equals(memberId));
    }

    // 전체 목록 조회, 좋아요 여부 체크
    public MagazinePageResponseDTO readAllMagazines(Long memberId, int page, int size) {
        memberService.validateAndGetMember(memberId);

        // 클라이언트는 1부터 시작하는 페이지 번호를 전달하며, 0-based 인덱스로 변환
        if (page < 1) {
            throw new CustomException(ErrorCode.PAGE_OUT_OF_BOUNDS);
        }

        Pageable pageable = PageRequest.of(page - 1, Math.min(size, 50));

        List<Magazine> magazines = magazineQueryRepository.findAllVisibleMagazines(pageable);
        List<Long> likedIds = magazineLikeRepository.findMagazineIdsByMemberId(memberId);
        Set<Long> likedIdSet = new HashSet<>(likedIds);

        List<MagazineDetailDTO> magazineDetailDTOS = magazines.stream()
                .map(magazine -> MagazineDetailDTO.toDTO(magazine, likedIdSet.contains(magazine.getId()),
                        magazine.getMember().getId().equals(memberId)))
                .toList();

        return MagazinePageResponseDTO.builder()
                .page(page)
                .size(size)
                .totalCount(magazineQueryRepository.countAllVisibleMagazines())
                .magazines(magazineDetailDTOS)
                .build();
    }

    @Transactional
    public MagazineDetailDTO updateMagazine(Long memberId, Long magazineId, MagazineUpdateRequestDTO dto, List<MultipartFile> images, MultipartFile thumbnailImage) {
        Member member = memberService.validateAndGetMember(memberId);
        Magazine magazine = magazineValidator.validateAndGetMagazine(magazineId);

        // 작성자가 아니거나 관리자 권한이 없는 경우
        if (!magazine.getMember().getId().equals(memberId) && member.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        Boolean newPinned = dto.getPinned();
        Integer newOrder = dto.getOrderInHome();
        boolean currentPinned = magazine.isPinned();

        if (newPinned == null) {
            // pinned 필드는 수정하지 않음
            if (currentPinned && newOrder != null) {
                // 기존에 pinned 상태에서 순서만 바꾸려는 경우
                validatePinnedMagazine(true, newOrder);
            }
        } else if (newPinned) {
            // pinned=true로 변경 → 순서 필수
            if (newOrder == null) {
                throw new CustomException(MagazineErrorCode.INVALID_ORDER_IN_HOME); // 순서 누락
            }
            validatePinnedMagazine(true, newOrder);
        } else {
            // pinned=false로 변경 → 순서 강제 0 처리 (검증 불필요)
            magazine.setOrderInHome(0);
        }

        // 수정된 필드 업데이트
        updateCommonFields(magazine, dto);

        // 이벤트 유효성 검사 및 설정
        if (dto.getEventId() != null) {
            Event event = eventService.validateAndGet(dto.getEventId());
            magazine.setEvent(event);
        }

        // 장소 유효성 검사 및 설정
        if (dto.getVenueIds() != null && !dto.getVenueIds().isEmpty()) {
            List<Venue> venues = dto.getVenueIds().stream()
                    .map(venueInfoService::validateAndGetVenue)
                    .collect(Collectors.toList());
            magazine.setVenues(venues);
        }

        // 썸네일 이미지가 비어있지 않을 때만 업로드 진행
        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            log.info("New thumbnail image detected, proceeding with update.");

            // 기존 썸네일 있으면 삭제
            if (magazine.getThumbImage() != null) {
                uploadUtil.deleteImage(magazine.getThumbImage(), UploadUtil.BucketType.MEDIA);
                log.info("Existing thumbnail image deleted.");
            }

            // 새 이미지 업로드
            String thumbImageUrl = uploadUtil.upload(thumbnailImage, UploadUtil.BucketType.MEDIA, "magazine");
            log.info("New thumbnail image uploaded: {}", thumbImageUrl);
            magazine.setThumbImage(thumbImageUrl);
            log.info("Thumbnail image updated successfully.");
        }

        // 기존에 있던 이미지(지우려는 이미지 제외) 와, 추가로 넣는 이미지의 개수 검사
        int existingCount = magazine.getImageUrls().size();
        int deleteCount = dto.getDeleteImageUrls() != null ? dto.getDeleteImageUrls().size() : 0;
        int newUploadCount = images != null ? (int) images.stream().filter(file -> file != null && !file.isEmpty()).count() : 0;

        if (existingCount - deleteCount + newUploadCount > 20) {
            throw new CustomException(ErrorCode.TOO_MANY_IMAGES_20);
        }

        // 이미지 삭제
        if (dto.getDeleteImageUrls() != null && !dto.getDeleteImageUrls().isEmpty()) {
            uploadUtil.deleteImages(dto.getDeleteImageUrls(), UploadUtil.BucketType.MEDIA);
            magazine.getImageUrls().removeAll(dto.getDeleteImageUrls());
        }


        // 이미지 업로드
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = uploadUtil.uploadImages(images, UploadUtil.BucketType.MEDIA, "magazine");
            magazine.getImageUrls().addAll(imageUrls);
        }

        return MagazineDetailDTO.toDTO(magazine, false, true); // 작성자는 항상 좋아요가 false로 설정됨
    }




    // 홈에 고정되는 매거진의 유효성 검사
    private void validatePinnedMagazine(boolean isPinned, Integer orderInHome) {
        if (isPinned) {
            // 1. null 또는 1~5 범위 밖이면 에러
            if (orderInHome == null || orderInHome < 1 || orderInHome > 5) {
                throw new CustomException(MagazineErrorCode.INVALID_ORDER_IN_HOME);
            }

            // 2. 동일한 orderInHome이 이미 존재하면 에러
            boolean exists = magazineRepository.existsByIsPinnedTrueAndOrderInHome(orderInHome);
            if (exists) {
                throw new CustomException(MagazineErrorCode.DUPLICATE_ORDER_IN_HOME);
            }
        }
    }

    private void updateCommonFields(Magazine magazine, MagazineUpdateRequestDTO dto) {
        if (dto.getTitle() != null) magazine.setTitle(dto.getTitle());
        if (dto.getContent() != null) magazine.setContent(dto.getContent());
        if (dto.getVisible() != null) magazine.setVisible(dto.getVisible());
        if (dto.getPinned() != null) magazine.setPinned(dto.getPinned());
        if (dto.getSponsored() != null) magazine.setSponsored(dto.getSponsored());
        if (dto.getPicked() != null) magazine.setPicked(dto.getPicked());

        // orderInHome은 null이 아닌 경우에만 수정
        if (dto.getOrderInHome() != null) {
            magazine.setOrderInHome(dto.getOrderInHome());
        }

        // pinned가 false로 명시된 경우 → 순서를 무조건 0으로
        if (Boolean.FALSE.equals(dto.getPinned())) {
            magazine.setOrderInHome(0);
        }
    }
}
