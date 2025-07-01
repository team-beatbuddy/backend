package com.ceos.beatbuddy.domain.venue.application;

import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.VenueReviewLike;
import com.ceos.beatbuddy.domain.scrapandlike.repository.VenueReviewLikeRepository;
import com.ceos.beatbuddy.domain.venue.dto.VenueReviewRequestDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueReviewResponseDTO;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.domain.venue.entity.VenueReview;
import com.ceos.beatbuddy.domain.venue.exception.VenueReviewErrorCode;
import com.ceos.beatbuddy.domain.venue.repository.VenueReviewQueryRepository;
import com.ceos.beatbuddy.domain.venue.repository.VenueReviewRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.UploadUtil;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
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

    @Transactional(readOnly = true)
    public List<VenueReviewResponseDTO> getVenueReview(Long venueId, Long memberId, boolean hasImage, String sort) {
        // Venue ID 유효성 검사
        Venue venue = venueInfoService.validateAndGetVenue(venueId);
        memberService.validateAndGetMember(memberId);

        // 정렬 기준
        String sortBy = (sort != null && sort.equals("popular")) ? "popular" : "latest";

        // 리뷰 조회 - 이미지 유무 + 정렬 기준 포함
        List<VenueReview> reviews;
        if (hasImage) {
            reviews = venueReviewQueryRepository.findReviewsWithImagesSorted(venueId, sortBy);
        } else {
            reviews = venueReviewQueryRepository.findAllReviewsSorted(venueId, sortBy);
        }

        // 리뷰에 대한 좋아요 여부 설정
        return reviews.stream()
                .map(review -> {
                    boolean isLiked = venueReviewLikeRepository.existsByVenueReview_IdAndMember_Id(review.getId(), memberId);
                    return VenueReviewResponseDTO.toDTO(review, isLiked);
                })
                .toList();
    }

    @Transactional
    public void likeVenueReview(Long venueReviewId, Long memberId) {
        // VenueReview ID 유효성 검사
        VenueReview venueReview = validateAndGetVenueReview(venueReviewId);
        // Member ID 유효성 검사
        Member member = memberService.validateAndGetMember(memberId);

        // 이미 좋아요가 있는지 확인
        if (venueReviewLikeRepository.existsByVenueReview_IdAndMember_Id(venueReviewId, memberId)) {
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        }

        // 리뷰 좋아요 생성
        VenueReviewLike venueReviewLike = VenueReviewLike.toEntity(member, venueReview);

        // 좋아요 수 증가
        venueReviewLikeRepository.save(venueReviewLike);
        venueReviewRepository.increaseLikeCount(venueReview.getId());
    }

    protected VenueReview validateAndGetVenueReview(Long venueReviewId) {
        return venueReviewRepository.findById(venueReviewId)
                .orElseThrow(() -> new CustomException(VenueReviewErrorCode.VENUE_REVIEW_NOT_FOUND));
    }
}
