package com.ceos.beatbuddy.domain.magazine.application;

import com.ceos.beatbuddy.domain.magazine.dto.MagazineRequestDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineResponseDTO;
import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.magazine.exception.MagazineErrorCode;
import com.ceos.beatbuddy.domain.magazine.repository.MagazineRepository;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.UploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    @Autowired
    private UploadUtil uploadUtil;

    public MagazineResponseDTO addMagazine(Long memberId, MagazineRequestDTO dto, List<MultipartFile> images) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        if (!(Objects.equals(member.getRole(), "ADMIN")) && !(Objects.equals(member.getRole(), "BUSINESS"))) {
            throw new CustomException(MagazineErrorCode.CANNOT_ADD_MAGAZINE_UNAUTHORIZED_MEMBER);
        }

        // 이미지 업로드
        List<String> imageUrls = uploadImages(images);

        // 엔티티로 변경
        Magazine entity = MagazineRequestDTO.toEntity(dto, member, imageUrls);

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
}
