package com.ceos.beatbuddy.domain.magazine.application;

import com.ceos.beatbuddy.domain.magazine.dto.MagazineDetailDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineHomeResponseDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineRequestDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineResponseDTO;
import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.magazine.exception.MagazineErrorCode;
import com.ceos.beatbuddy.domain.magazine.repository.MagazineRepository;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.domain.scrap.entity.MagazineScrap;
import com.ceos.beatbuddy.domain.scrap.repository.MagazineScrapRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.UploadUtil;
import com.ceos.beatbuddy.global.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final MemberRepository memberRepository;
    private final MagazineScrapRepository magazineScrapRepository;

    private final UploadUtil uploadUtil;

    public MagazineResponseDTO addMagazine(Long memberId, MagazineRequestDTO dto, List<MultipartFile> images) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        if (!(Objects.equals(member.getRole(), "ADMIN")) && !(Objects.equals(member.getRole(), "BUSINESS"))) {
            throw new CustomException(MagazineErrorCode.CANNOT_ADD_MAGAZINE_UNAUTHORIZED_MEMBER);
        }

        // 이미지 업로드
        List<String> imageUrls = uploadImages(images);

        // 엔티티로 변경
        Magazine entity = MagazineRequestDTO.toEntity(dto, member, imageUrls);
        // 썸네일 이미지 세팅
        entity.setThumbImage(imageUrls.get(0));

        magazineRepository.save(entity);

        return MagazineResponseDTO.toDTO(entity);
    }

    private List<String> uploadImages(List<MultipartFile> images) {
        return images.stream()
                .map(image -> {
                    try {
                        return uploadUtil.upload(image, UploadUtil.BucketType.MEDIA);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    public List<MagazineHomeResponseDTO> readHomeMagazines(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        List<Magazine> magazines = magazineRepository.findMagazinesByIsVisibleTrue();

        return magazines.stream().map((MagazineHomeResponseDTO::toDTO)).toList();
    }

    public MagazineDetailDTO readDetailMagazine(Long memberId, Long magazineId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        Magazine magazine = magazineRepository.findByIdAndIsVisibleTrue(magazineId).orElseThrow(() ->
                new CustomException(MagazineErrorCode.MAGAZINE_NOT_EXIST)
        );

        magazine.increaseView();

        return MagazineDetailDTO.toDTO(magazine);
    }

    @Transactional
    public MagazineDetailDTO scrapMagazine(Long memberId, Long magazineId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        Magazine magazine = magazineRepository.findByIdAndIsVisibleTrue(magazineId).orElseThrow(() ->
                new CustomException(MagazineErrorCode.MAGAZINE_NOT_EXIST)
        );

        MagazineScrap magazineScrap = MagazineScrap.toEntity(member, magazine);
        magazine.getScraps().add(magazineScrap);

        return MagazineDetailDTO.toDTO(magazine);
    }

    public List<MagazineHomeResponseDTO> getScrapMagazines(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        List<MagazineScrap> magazineScraps = magazineScrapRepository.findAllByMember(member);
        List<Magazine> magazines = magazineScraps.stream().map((magazineScrap -> {
            return magazineRepository.findById(magazineScrap.getMagazine().getId()).orElseThrow(() -> new CustomException(MagazineErrorCode.MAGAZINE_NOT_EXIST));
        })).toList();

        return magazines.stream().map((MagazineHomeResponseDTO::toScrapDTO)).toList();
    }

    @Transactional
    public MagazineDetailDTO likeMagazine(Long magazineId, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));
        
        // 엔티티 검색
        Magazine magazine = magazineRepository.findByIdAndIsVisibleTrue(magazineId).orElseThrow(() ->
                new CustomException(MagazineErrorCode.MAGAZINE_NOT_EXIST)
        );

        // 좋아요 증가
        magazine.increaseLike();

        return MagazineDetailDTO.toDTO(magazine);
    }
}
