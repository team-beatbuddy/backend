package com.ceos.beatbuddy.domain.magazine.application;

import com.ceos.beatbuddy.domain.magazine.dto.MagazineDetailDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineHomeResponseDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineRequestDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineResponseDTO;
import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.magazine.exception.MagazineErrorCode;
import com.ceos.beatbuddy.domain.magazine.repository.MagazineRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.MagazineInteractionId;
import com.ceos.beatbuddy.domain.scrapandlike.entity.MagazineLike;
import com.ceos.beatbuddy.domain.scrapandlike.entity.MagazineScrap;
import com.ceos.beatbuddy.domain.scrapandlike.repository.MagazineLikeRepository;
import com.ceos.beatbuddy.domain.scrapandlike.repository.MagazineScrapRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.UploadUtil;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MagazineService {
    private final MagazineRepository magazineRepository;
    private final MemberService memberService;
    private final MagazineScrapRepository magazineScrapRepository;
    private final MagazineLikeRepository magazineLikeRepository;

    private final UploadUtil uploadUtil;
    @Transactional
    public MagazineResponseDTO addMagazine(Long memberId, MagazineRequestDTO dto, List<MultipartFile> images) throws RuntimeException {
        Member member = memberService.validateAndGetMember(memberId);

        if (!(Objects.equals(member.getRole(), "ADMIN")) && !(Objects.equals(member.getRole(), "BUSINESS"))) {
            throw new CustomException(MagazineErrorCode.CANNOT_ADD_MAGAZINE_UNAUTHORIZED_MEMBER);
        }

        // 이미지 업로드
        List<String> imageUrls = uploadUtil.uploadImages(images, UploadUtil.BucketType.MEDIA, "magazine");

        // 엔티티로 변경
        Magazine entity = MagazineRequestDTO.toEntity(dto, member, imageUrls);
        // 썸네일 이미지 세팅
        entity.setThumbImage(imageUrls.get(0));

        magazineRepository.save(entity);

        return MagazineResponseDTO.toDTO(entity);
    }

    public List<MagazineHomeResponseDTO> readHomeMagazines(Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        List<Magazine> magazines = magazineRepository.findMagazinesByIsVisibleTrue();

        return magazines.stream().map((MagazineHomeResponseDTO::toDTO)).toList();
    }

    /**
     * Retrieves detailed information for a visible magazine and increments its view count.
     *
     * @param memberId the ID of the member requesting the magazine details
     * @param magazineId the ID of the magazine to retrieve
     * @return a detailed DTO representing the magazine
     * @throws CustomException if the member or magazine does not exist, or if the magazine is not visible
     */
    public MagazineDetailDTO readDetailMagazine(Long memberId, Long magazineId) {
        Member member = memberService.validateAndGetMember(memberId);

        Magazine magazine = validateAndGetMagazineVisibleTrue(magazineId);

        magazine.increaseView();

        return MagazineDetailDTO.toDTO(magazine);
    }

    /**
     * Adds a scrap (bookmark) for the specified magazine by the given member.
     *
     * @param memberId the ID of the member performing the scrap action
     * @param magazineId the ID of the magazine to be scrapped
     * @return a detailed DTO of the magazine after the scrap action
     * @throws CustomException if the magazine does not exist, is not visible, or has already been scrapped by the member
     */
    @Transactional
    public MagazineDetailDTO scrapMagazine(Long memberId, Long magazineId) {
        Member member = memberService.validateAndGetMember(memberId);

        Magazine magazine = validateAndGetMagazineVisibleTrue(magazineId);

        boolean alreadyScrapped = magazineScrapRepository.existsById(MagazineInteractionId.builder().magazineId(magazineId).memberId(memberId).build());

        if (alreadyScrapped) {
            throw new CustomException(MagazineErrorCode.ALREADY_SCRAP_MAGAZINE);
        }

        MagazineScrap magazineScrap = MagazineScrap.toEntity(member, magazine);
        magazine.getScraps().add(magazineScrap);

        return MagazineDetailDTO.toDTO(magazine);
    }

    /**
     * Retrieves a list of magazines scrapped by the specified member.
     *
     * @param memberId the ID of the member whose scrapped magazines are to be retrieved
     * @return a list of DTOs representing the scrapped magazines
     */
    public List<MagazineHomeResponseDTO> getScrapMagazines(Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        List<MagazineScrap> magazineScraps = magazineScrapRepository.findAllByMember(member);
        List<Magazine> magazines = magazineScraps.stream().map((magazineScrap ->
                validateAndGetMagazine(magazineScrap.getId().getMagazineId()))).toList();

        return magazines.stream().map((MagazineHomeResponseDTO::toScrapDTO)).toList();
    }

    /**
     * Registers a like from the specified member on the given magazine.
     *
     * @param magazineId the ID of the magazine to like
     * @param memberId the ID of the member performing the like
     * @return a detailed DTO of the magazine after the like is registered
     * @throws CustomException if the magazine does not exist, is not visible, or the member has already liked the magazine
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
        magazine.increaseLike();

        return MagazineDetailDTO.toDTO(magazine);
    }

    /**
     * Removes a like from the specified magazine by the given member.
     *
     * @param magazineId the ID of the magazine from which the like will be removed
     * @param memberId the ID of the member removing the like
     * @return a detailed DTO of the magazine after the like has been removed
     * @throws CustomException if the like does not exist or the magazine/member is not found
     */
    @Transactional
    public MagazineDetailDTO deleteLikeMagazine(Long magazineId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        // 엔티티 검색
        Magazine magazine = validateAndGetMagazine(magazineId);

        // 좋아요 삭제
        MagazineLike magazineLike = magazineLikeRepository.findById(
                MagazineInteractionId.builder().memberId(memberId).magazineId(magazineId).build()).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_LIKE)
        );

        magazineLikeRepository.delete(magazineLike);
        magazine.decreaseLike();

        return MagazineDetailDTO.toDTO(magazine);
    }

    /**
     * Removes a scrap (bookmark) of the specified magazine for the given member.
     *
     * @param magazineId the ID of the magazine to unscrap
     * @param memberId the ID of the member performing the unscrap action
     * @return a detailed DTO of the magazine after the scrap is removed
     * @throws CustomException if the member or magazine does not exist, or if the scrap is not found
     */
    @Transactional
    public MagazineDetailDTO deleteScrapMagazine(Long magazineId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        // 엔티티 검색
        Magazine magazine = validateAndGetMagazine(magazineId);

        // 스크랩 삭제
        MagazineScrap magazineScrap = magazineScrapRepository.findById(
                MagazineInteractionId.builder().memberId(memberId).magazineId(magazineId).build()).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_SCRAP)
        );

        magazineScrapRepository.delete(magazineScrap);

        return MagazineDetailDTO.toDTO(magazine);

    }

    /**
     * Retrieves a visible magazine by its ID or throws an exception if not found.
     *
     * @param magazineId the ID of the magazine to retrieve
     * @return the magazine entity if it exists and is marked as visible
     * @throws CustomException if the magazine does not exist or is not visible
     */
    private Magazine validateAndGetMagazineVisibleTrue(Long magazineId) {
        return magazineRepository.findByIdAndIsVisibleTrue(magazineId).orElseThrow(() ->
                new CustomException(MagazineErrorCode.MAGAZINE_NOT_EXIST));
    }

    /**
     * Retrieves a magazine by its ID, regardless of visibility.
     *
     * @param magazineId the ID of the magazine to retrieve
     * @return the magazine entity with the specified ID
     * @throws CustomException if the magazine does not exist
     */
    private Magazine validateAndGetMagazine(Long magazineId) {
        return magazineRepository.findById(magazineId).orElseThrow(() ->
                new CustomException(MagazineErrorCode.MAGAZINE_NOT_EXIST));
    }
}
