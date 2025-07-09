package com.ceos.beatbuddy.domain.admin.application;

import com.ceos.beatbuddy.domain.admin.dto.ReportSummaryDTO;
import com.ceos.beatbuddy.domain.comment.entity.Comment;
import com.ceos.beatbuddy.domain.comment.exception.CommentErrorCode;
import com.ceos.beatbuddy.domain.comment.repository.CommentRepository;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventComment;
import com.ceos.beatbuddy.domain.event.exception.EventErrorCode;
import com.ceos.beatbuddy.domain.event.repository.EventCommentRepository;
import com.ceos.beatbuddy.domain.event.repository.EventRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.constant.Role;
import com.ceos.beatbuddy.domain.member.dto.AdminResponseDto;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.PiecePost;
import com.ceos.beatbuddy.domain.post.exception.PostErrorCode;
import com.ceos.beatbuddy.domain.post.repository.FreePostRepository;
import com.ceos.beatbuddy.domain.post.repository.PiecePostRepository;
import com.ceos.beatbuddy.domain.report.entity.Report;
import com.ceos.beatbuddy.domain.report.exception.ReportErrorCode;
import com.ceos.beatbuddy.domain.report.repository.ReportQueryRepository;
import com.ceos.beatbuddy.domain.report.repository.ReportRepository;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.domain.venue.entity.VenueReview;
import com.ceos.beatbuddy.domain.venue.exception.VenueErrorCode;
import com.ceos.beatbuddy.domain.venue.repository.VenueRepository;
import com.ceos.beatbuddy.domain.venue.repository.VenueReviewRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import com.ceos.beatbuddy.global.config.jwt.TokenProvider;
import com.ceos.beatbuddy.global.config.jwt.redis.RefreshToken;
import com.ceos.beatbuddy.global.config.jwt.redis.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminService {
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final ReportQueryRepository reportQueryRepository;
    private final ReportRepository reportRepository;
    private final MemberService memberService;
    private final FreePostRepository freePostRepository;
    private final PiecePostRepository piecePostRepository;
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final CommentRepository freePostCommentRepository;
    private final EventCommentRepository eventCommentRepository;
    private final VenueReviewRepository venueReviewRepository;

    @Transactional
    public Long createAdmin(String id) {
        if (memberRepository.existsByLoginId(id)) {
            throw new CustomException(MemberErrorCode.LOGINID_ALREADY_EXIST);
        }

        Member member = Member.builder()
                .loginId(id)
                .role(Role.ADMIN)
                .build();

        Member savedMember = memberRepository.save(member);
        return savedMember.getId();
    }

    @Transactional
    public ResponseEntity<AdminResponseDto> createAdminToken(Long memberId, String loginId) {

        RefreshToken byUserId = refreshTokenRepository.findByUserId(memberId);

        if (byUserId != null) {
            refreshTokenRepository.delete(byUserId);
        }

        String access = tokenProvider.createToken("access", memberId, loginId, "ADMIN", 1000 * 60 * 60 * 2L);
        String refresh = tokenProvider.createToken("refresh", memberId, loginId, "ADMIN", 1000 * 3600 * 24 * 14L);

        RefreshToken refreshToken = new RefreshToken(refresh, memberId);
        refreshTokenRepository.save(refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refresh", refresh)
                .path("/")
                .httpOnly(true)
                .sameSite("None")
                .secure(true)
                .maxAge(60 * 60 * 24 * 14)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok().headers(headers).body(AdminResponseDto.builder()
                .access(access)
                .build());
    }

    public Long findAdmin(String id) {
        Member member = memberRepository.findByLoginId(id).orElseThrow(
                () -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST)
        );

        if(member.getRole() != Role.ADMIN) {
            throw new CustomException(MemberErrorCode.NOT_ADMIN);
        }

        return member.getId();
    }

    // 관리자 권한이 있는지 확인
    public void validateAdmin(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        if (member.getRole() != Role.ADMIN) {
            throw new CustomException(MemberErrorCode.NOT_ADMIN);
        }
    }

    // 신고 목록 확인
    public List<ReportSummaryDTO> getAllReports(Long memberId) {
        // 관리자 권한이 있는지 확인
        validateAdmin(memberId);
        // 신고 목록 조회
        List<Report> reports = reportQueryRepository.getAllReports();
        return reports.stream().map(ReportSummaryDTO::toDTO).collect(Collectors.toList());
    }

    // 신고에서 삭제
    @Transactional
    public void deleteReport(Long reportId, Long memberId) {
        validateAdmin(memberId);
        reportRepository.deleteById(reportId);
    }

    @Transactional
    public void processReport(Long reportId, Long memberId) {
        // 관리자 권한 확인
        validateAdmin(memberId);

        // 신고 조회
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ReportErrorCode.REPORT_NOT_FOUND));

        // 원글 삭제
        switch (report.getTargetType()) {
            case FREE_POST -> {
                FreePost post = freePostRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new CustomException(ReportErrorCode.TARGET_NOT_FOUND));
                freePostRepository.delete(post);
            }
            case PIECE_POST -> {
                PiecePost post = piecePostRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new CustomException(ReportErrorCode.TARGET_NOT_FOUND));
                piecePostRepository.delete(post);
            }
            case EVENT -> {
                Event event = eventRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new CustomException(ReportErrorCode.TARGET_NOT_FOUND));
                eventRepository.delete(event);
            }
            case VENUE -> {
                Venue venue = venueRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new CustomException(ReportErrorCode.TARGET_NOT_FOUND));
                venueRepository.delete(venue);
            }
            case FREE_POST_COMMENT -> {
                Comment comment = freePostCommentRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new CustomException(ReportErrorCode.TARGET_NOT_FOUND));
                freePostCommentRepository.delete(comment);
            }
            case EVENT_COMMENT -> {
                EventComment comment = eventCommentRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new CustomException(ReportErrorCode.TARGET_NOT_FOUND));
                eventCommentRepository.delete(comment);
            }
            case VENUE_COMMENT -> {
                VenueReview comment = venueReviewRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new CustomException(ReportErrorCode.TARGET_NOT_FOUND));
                venueReviewRepository.delete(comment);
            }
            default -> throw new CustomException(ReportErrorCode.INVALID_REPORT_TARGET_TYPE);
        }

        // 신고 삭제
        reportRepository.delete(report);
    }
}
