package com.ceos.beatbuddy.domain.magazine.application;

import com.ceos.beatbuddy.domain.magazine.dto.MagazineDetailDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineHomeResponseDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazinePageResponseDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineRequestDTO;
import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.magazine.exception.MagazineErrorCode;
import com.ceos.beatbuddy.domain.magazine.repository.MagazineQueryRepository;
import com.ceos.beatbuddy.domain.magazine.repository.MagazineRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.constant.Role;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.MagazineInteractionId;
import com.ceos.beatbuddy.domain.scrapandlike.entity.MagazineLike;
import com.ceos.beatbuddy.domain.scrapandlike.repository.MagazineLikeRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.UploadUtil;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MagazineService {
    private final MagazineRepository magazineRepository;
    private final MemberService memberService;
    private final MagazineLikeRepository magazineLikeRepository;
    private final MagazineQueryRepository magazineQueryRepository;

    private final UploadUtil uploadUtil;
    /**
     * 매거진을 생성합니다. (관리자 또는 비즈니스 회원만 가능)
     *
     * @param memberId 매거진을 작성하는 회원의 ID
     * @param dto 매거진 생성 요청 DTO
     * @param images 첨부 이미지 리스트
     * @return 생성된 매거진의 상세 DTO
     * @throws CustomException 권한이 없는 회원이 요청한 경우
     */
    @Transactional
    public MagazineDetailDTO addMagazine(Long memberId, MagazineRequestDTO dto, List<MultipartFile> images) throws RuntimeException {
        Member member = memberService.validateAndGetMember(memberId);

        if (member.getRole() != Role.ADMIN && member.getRole() != Role.BUSINESS) {
            throw new CustomException(MagazineErrorCode.CANNOT_ADD_MAGAZINE_UNAUTHORIZED_MEMBER);
        }

        // 엔티티로 변경
        Magazine entity = MagazineRequestDTO.toEntity(dto, member);

        List<String> imageUrls = null;
        // 이미지 업로드 (이미지가 비어있는 경우는 제외)
        if (images != null && !images.isEmpty()) {
            imageUrls = uploadUtil.uploadImages(images, UploadUtil.BucketType.MEDIA, "magazine");
            // 썸네일 저장
            entity.setThumbImage(imageUrls.get(0));
            // 엔티티에 이미지 세팅
            entity.setImageUrls(imageUrls);
        }

        magazineRepository.save(entity);

        return MagazineDetailDTO.toDTO(entity, false);
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

        return magazines.stream().map((MagazineHomeResponseDTO::toDTO)).toList();
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

        Magazine magazine = validateAndGetMagazineVisibleTrue(magazineId);

        // 조회수 증가
        magazine.increaseView();

        // 매거진 좋아요 여부 확인
        boolean isLiked = magazineLikeRepository.existsById(
                MagazineInteractionId.builder().memberId(memberId).magazineId(magazineId).build());

        return MagazineDetailDTO.toDTO(magazine, isLiked);
    }

    // 전체 목록 조회, 좋아요 여부 체크
    public MagazinePageResponseDTO readAllMagazines(Long memberId, int page, int size) {
        memberService.validateAndGetMember(memberId);

        // 페이지 수는 0부터 시작
        Pageable pageable = PageRequest.of(page - 1, Math.min(size, 50));

        List<Magazine> magazines = magazineQueryRepository.findAllVisibleMagazines(pageable);
        List<Long> likedIds = magazineLikeRepository.findMagazineIdsByMemberId(memberId);
        Set<Long> likedIdSet = new HashSet<>(likedIds);

        List<MagazineDetailDTO> magazineDetailDTOS = magazines.stream()
                .map(magazine -> MagazineDetailDTO.toDTO(magazine, likedIdSet.contains(magazine.getId())))
                .toList();

        return MagazinePageResponseDTO.builder()
                .page(page)
                .size(size)
                .totalCount(magazineQueryRepository.countAllVisibleMagazines())
                .magazines(magazineDetailDTOS)
                .build();
    }


    /**
     * 지정된 회원이 해당 매거진에 좋아요를 등록합니다.
     *
     * @param magazineId 좋아요를 등록할 매거진의 ID
     * @param memberId 좋아요를 수행하는 회원의 ID
     * @return 좋아요 등록 후의 매거진 상세 DTO
     * @throws CustomException 매거진이 존재하지 않거나, 표시되지 않거나, 이미 좋아요를 등록한 경우
     */
    @Transactional
    public MagazineDetailDTO likeMagazine(Long magazineId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        // 엔티티 검색
        Magazine magazine = validateAndGetMagazineVisibleTrue(magazineId);

        // 좋아요 증가 (이미 좋아요가 있으면 예외처리
        boolean alreadyLiked = magazineLikeRepository.existsById(
                MagazineInteractionId.builder().memberId(memberId).magazineId(magazineId).build());

        if (alreadyLiked) {
            throw new CustomException(MagazineErrorCode.ALREADY_LIKE_MAGAZINE);
        }

        MagazineLike entity = MagazineLike.toEntity(member, magazine);
        magazineLikeRepository.save(entity);
        magazineRepository.increaseLike(magazineId);

        Magazine updatedEntity = this.validateAndGetMagazine(magazineId);

        return MagazineDetailDTO.toDTO(updatedEntity, true);
    }

    /**
     * 지정된 매거진에 대해 해당 회원의 좋아요를 제거합니다.
     *
     * @param magazineId 좋아요를 제거할 매거진의 ID
     * @param memberId 좋아요를 제거하는 회원의 ID
     * @return 좋아요 제거 후의 매거진 상세 DTO
     * @throws CustomException 좋아요가 존재하지 않거나, 매거진/회원이 존재하지 않는 경우
     */
    @Transactional
    public MagazineDetailDTO deleteLikeMagazine(Long magazineId, Long memberId) {
        memberService.validateAndGetMember(memberId);

        // 엔티티 검색
        validateAndGetMagazine(magazineId);

        // 좋아요 삭제
        MagazineLike magazineLike = magazineLikeRepository.findById(
                MagazineInteractionId.builder().memberId(memberId).magazineId(magazineId).build()).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_LIKE)
        );

        magazineLikeRepository.delete(magazineLike);
        magazineRepository.decreaseLike(magazineId);

        Magazine updatedEntity = this.validateAndGetMagazine(magazineId);

        return MagazineDetailDTO.toDTO(updatedEntity, false);
    }

    /**
     * ID를 기반으로 표시 가능한(visible) 매거진을 조회하며, 존재하지 않거나 표시 불가능한 경우 예외를 발생시킵니다.
     *
     * @param magazineId 조회할 매거진의 ID
     * @return 존재하고 표시 가능한 매거진 엔티티
     * @throws CustomException 매거진이 존재하지 않거나 표시 불가능한 경우
     */
    private Magazine validateAndGetMagazineVisibleTrue(Long magazineId) {
        return magazineRepository.findByIdAndIsVisibleTrue(magazineId).orElseThrow(() ->
                new CustomException(MagazineErrorCode.MAGAZINE_NOT_EXIST));
    }

    /**
     * 표시 여부와 관계없이 ID를 기반으로 매거진을 조회합니다.
     *
     * @param magazineId 조회할 매거진의 ID
     * @return 해당 ID를 가진 매거진 엔티티
     * @throws CustomException 매거진이 존재하지 않는 경우
     */
    private Magazine validateAndGetMagazine(Long magazineId) {
        return magazineRepository.findById(magazineId).orElseThrow(() ->
                new CustomException(MagazineErrorCode.MAGAZINE_NOT_EXIST));
    }
}
