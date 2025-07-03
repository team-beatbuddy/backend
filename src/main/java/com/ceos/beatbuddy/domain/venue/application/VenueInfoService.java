package com.ceos.beatbuddy.domain.venue.application;

import com.amazonaws.services.s3.AmazonS3;
import com.ceos.beatbuddy.domain.heartbeat.repository.HeartbeatRepository;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.domain.vector.entity.Vector;
import com.ceos.beatbuddy.domain.venue.dto.VenueInfoResponseDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueRequestDTO;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.domain.venue.entity.VenueGenre;
import com.ceos.beatbuddy.domain.venue.entity.VenueMood;
import com.ceos.beatbuddy.domain.venue.exception.VenueErrorCode;
import com.ceos.beatbuddy.domain.venue.exception.VenueGenreErrorCode;
import com.ceos.beatbuddy.domain.venue.exception.VenueMoodErrorCode;
import com.ceos.beatbuddy.domain.venue.repository.VenueGenreRepository;
import com.ceos.beatbuddy.domain.venue.repository.VenueMoodRepository;
import com.ceos.beatbuddy.domain.venue.repository.VenueRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.UploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueInfoService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final VenueRepository venueRepository;
    private final HeartbeatRepository heartbeatRepository;
    private final MemberRepository memberRepository;
    private final VenueGenreRepository venueGenreRepository;
    private final VenueMoodRepository venueMoodRepository;
    private final VenueSearchService venueSearchService;

    private final AmazonS3 amazonS3;

    private final UploadUtil uploadUtil;
    public List<Venue> getVenueInfoList() {
        return venueRepository.findAll();
    }

    public VenueInfoResponseDTO getVenueInfo(Long venueId, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException(VenueErrorCode.VENUE_NOT_EXIST));
        boolean isHeartbeat = heartbeatRepository.findByMemberVenue(member, venue).isPresent();

        VenueGenre venueGenre = venueGenreRepository.findByVenue(venue)
                .orElseThrow(() -> new CustomException(VenueGenreErrorCode.VENUE_GENRE_NOT_EXIST));
        List<String> trueGenreElements = Vector.getTrueGenreElements(venueGenre.getGenreVector());

        VenueMood venueMood = venueMoodRepository.findByVenue(venue)
                .orElseThrow(() -> new CustomException(VenueMoodErrorCode.VENUE_MOOD_NOT_EXIST));
        List<String> trueMoodElements = Vector.getTrueMoodElements(venueMood.getMoodVector());
        String region = venue.getRegion().getText();

        List<String> tagList = new ArrayList<>(trueGenreElements);
        tagList.addAll(trueMoodElements);
        tagList.add(region);

        return VenueInfoResponseDTO.builder()
                .venue(venue)
                .isHeartbeat(isHeartbeat)
                .tagList(tagList)
                .build();
    }

    @Transactional
    public Long deleteVenueInfo(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException(VenueErrorCode.VENUE_NOT_EXIST));
        uploadUtil.deleteImage(venue.getLogoUrl(), UploadUtil.BucketType.VENUE);
        uploadUtil.deleteImages(venue.getBackgroundUrl(), UploadUtil.BucketType.VENUE);
        venueSearchService.deleteVenueFromES(venueId); // 삭제 반영
        return venueRepository.deleteByVenueId(venueId);
    }

    @Transactional
    public Venue addVenueInfo(VenueRequestDTO request, MultipartFile logoImage, List<MultipartFile> backgroundImage)
            throws IOException {

        String logoImageUrl = null;
        List<String> backgroundImageUrls = new ArrayList<>();

        if (logoImage != null) {
            logoImageUrl = uploadUtil.upload(logoImage, UploadUtil.BucketType.VENUE, null);
        }

        if (!backgroundImage.isEmpty()) {
            uploadUtil.uploadImages(backgroundImage, UploadUtil.BucketType.VENUE, null);
        }

        Venue venue =venueRepository.save(Venue.of(request, logoImageUrl, backgroundImageUrls));

        venueSearchService.saveVenueToES(venue); // Venue 정보를 Elasticsearch에 저장

        return venue;
    }

    @Transactional
    public Venue updateVenueInfo(Long venueId, VenueRequestDTO venueRequestDTO, MultipartFile logoImage, List<MultipartFile> backgroundImages)
            throws IOException {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException(VenueErrorCode.VENUE_NOT_EXIST));

        String logoImageUrl = venue.getLogoUrl();
        List<String> backgroundImageUrls = venue.getBackgroundUrl();

        if (logoImage != null) {
            uploadUtil.deleteImage(logoImageUrl, UploadUtil.BucketType.VENUE);
            logoImageUrl = uploadUtil.upload(logoImage, UploadUtil.BucketType.VENUE, null);
        }

        if (!backgroundImages.isEmpty()) {
            uploadUtil.deleteImages(backgroundImageUrls, UploadUtil.BucketType.VENUE);
            backgroundImageUrls = uploadUtil.uploadImages(backgroundImages, UploadUtil.BucketType.VENUE, null);
        }

        venue.update(venueRequestDTO, logoImageUrl, backgroundImageUrls);

        venueSearchService.saveVenueToES(venue);
        return venueRepository.save(venue);
    }

    public Venue validateAndGetVenue(Long venueId) {
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomException(VenueErrorCode.VENUE_NOT_EXIST));
    }
}
