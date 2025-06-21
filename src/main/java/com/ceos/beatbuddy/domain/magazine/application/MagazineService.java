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
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
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

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        List<String> imageUrls = uploadUtil.uploadImages(images, "magazine");

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

    public MagazineDetailDTO readDetailMagazine(Long memberId, Long magazineId) {
        Member member = memberService.validateAndGetMember(memberId);

        Magazine magazine = magazineRepository.findByIdAndIsVisibleTrue(magazineId).orElseThrow(() ->
                new CustomException(MagazineErrorCode.MAGAZINE_NOT_EXIST)
        );

        magazine.increaseView();

        return MagazineDetailDTO.toDTO(magazine);
    }

    @Transactional
    public MagazineDetailDTO scrapMagazine(Long memberId, Long magazineId) {
        Member member = memberService.validateAndGetMember(memberId);

        Magazine magazine = magazineRepository.findById(magazineId).orElseThrow(() ->
                new CustomException(MagazineErrorCode.MAGAZINE_NOT_EXIST)
        );

        boolean alreadyScrapped = magazineScrapRepository.existsById(MagazineInteractionId.builder().magazineId(magazineId).memberId(memberId).build());

        if (alreadyScrapped) {
            throw new CustomException(MagazineErrorCode.ALREADY_SCRAP_MAGAZINE);
        }

        MagazineScrap magazineScrap = MagazineScrap.toEntity(member, magazine);
        magazine.getScraps().add(magazineScrap);

        return MagazineDetailDTO.toDTO(magazine);
    }

    public List<MagazineHomeResponseDTO> getScrapMagazines(Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        List<MagazineScrap> magazineScraps = magazineScrapRepository.findAllByMember(member);
        List<Magazine> magazines = magazineScraps.stream().map((magazineScrap -> {
            return magazineRepository.findById(magazineScrap.getMagazine().getId()).orElseThrow(() -> new CustomException(MagazineErrorCode.MAGAZINE_NOT_EXIST));
        })).toList();

        return magazines.stream().map((MagazineHomeResponseDTO::toScrapDTO)).toList();
    }

    @Transactional
    public MagazineDetailDTO likeMagazine(Long magazineId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        // 엔티티 검색
        Magazine magazine = magazineRepository.findByIdAndIsVisibleTrue(magazineId).orElseThrow(() ->
                new CustomException(MagazineErrorCode.MAGAZINE_NOT_EXIST)
        );

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

    @Transactional
    public MagazineDetailDTO deleteLikeMagazine(Long magazineId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        // 엔티티 검색
        Magazine magazine = magazineRepository.findByIdAndIsVisibleTrue(magazineId).orElseThrow(() ->
                new CustomException(MagazineErrorCode.MAGAZINE_NOT_EXIST)
        );

        // 좋아요 삭제
        MagazineLike magazineLike = magazineLikeRepository.findById(
                MagazineInteractionId.builder().memberId(memberId).magazineId(magazineId).build()).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_LIKE)
        );

        magazineLikeRepository.delete(magazineLike);
        magazine.decreaseLike();

        return MagazineDetailDTO.toDTO(magazine);
    }

    @Transactional
    public MagazineDetailDTO deleteScrapMagazine(Long magazineId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        // 엔티티 검색
        Magazine magazine = magazineRepository.findByIdAndIsVisibleTrue(magazineId).orElseThrow(() ->
                new CustomException(MagazineErrorCode.MAGAZINE_NOT_EXIST)
        );

        // 스크랩 삭제
        MagazineScrap magazineScrap = magazineScrapRepository.findById(
                MagazineInteractionId.builder().memberId(memberId).magazineId(magazineId).build()).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_SCRAP)
        );

        magazineScrapRepository.delete(magazineScrap);

        return MagazineDetailDTO.toDTO(magazine);

    }
}
