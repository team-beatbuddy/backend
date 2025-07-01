package com.ceos.beatbuddy.domain.venue.application;

import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.repository.VenueReviewLikeRepository;
import com.ceos.beatbuddy.domain.venue.dto.VenueReviewRequestDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueReviewResponseDTO;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.domain.venue.entity.VenueReview;
import com.ceos.beatbuddy.domain.venue.repository.VenueReviewQueryRepository;
import com.ceos.beatbuddy.domain.venue.repository.VenueReviewRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.UploadUtil;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueReviewService {
    private final VenueReviewRepository venueReviewRepository;
    private final VenueInfoService venueInfoService;
    private final MemberService memberService;
    private final UploadUtil uploadUtil;
    private final VenueReviewQueryRepository venueReviewQueryRepository;
    private final VenueReviewLikeRepository venueReviewLikeRepository;


    private static final String REVIEW_FOLDER = "review";

    public VenueReviewResponseDTO createVenueReview(Long venueId, Long memberId, VenueReviewRequestDTO dto, List<MultipartFile> images) {
        // 이미지 개수 검사
        if (images != null && images.stream().filter(file -> file != null && !file.isEmpty()).count() > 5) {
            throw new CustomException(ErrorCode.TOO_MANY_IMAGES_5);
        }

        // Venue ID와 Member ID 유효성 검사
        Member member = memberService.validateAndGetMember(memberId);
        Venue venue = venueInfoService.validateAndGetVenue(venueId);

        // VenueReview 엔티티 생성
        VenueReview venueReview = VenueReviewRequestDTO.toEntity(dto);

        // Venue와 Member 설정
        venueReview.setVenue(venue);
        venueReview.setMember(member);

        // 이미지 업로드
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = uploadUtil.uploadImages(images, UploadUtil.BucketType.VENUE,REVIEW_FOLDER);
            venueReview.setImageUrls(imageUrls);
        }

        // 리뷰 저장
        venueReview = venueReviewRepository.save(venueReview);
        return VenueReviewResponseDTO.toDTO(venueReview, false); // false는 해당 댓글에 대한 좋아요 여부를 나타냄, 새로 생성된 리뷰의 초기 좋아요 상태 (false)
    }

    public List<VenueReviewResponseDTO> getVenueReview(Long venueId, Long memberId, boolean hasImage) {
        // Venue ID 유효성 검사
        Venue venue = venueInfoService.validateAndGetVenue(venueId);
        memberService.validateAndGetMember(memberId);

        List<VenueReview> reviews = null;
        if (hasImage) {
            // 이미지가 있는 리뷰만 조회
            reviews = venueReviewQueryRepository.findReviewsWithImages(venueId);
        }
        else {
            // 모든 리뷰 조회
            reviews = venueReviewRepository.findByVenueId(venue.getId());
        }

        // 리뷰에 대한 좋아요 여부 설정
        return reviews.stream()
                .map(review -> {
                    boolean isLiked = venueReviewLikeRepository.existsByVenueReview_IdAndMember_Id(review.getId(), memberId);
                    return VenueReviewResponseDTO.toDTO(review, isLiked);
                })
                .toList();
    }
}
