package com.ceos.beatbuddy.domain.report.service;

import com.ceos.beatbuddy.domain.admin.dto.TitleContent;
import com.ceos.beatbuddy.domain.comment.entity.Comment;
import com.ceos.beatbuddy.domain.comment.exception.CommentErrorCode;
import com.ceos.beatbuddy.domain.comment.repository.CommentRepository;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventComment;
import com.ceos.beatbuddy.domain.event.exception.EventErrorCode;
import com.ceos.beatbuddy.domain.event.repository.EventCommentRepository;
import com.ceos.beatbuddy.domain.event.repository.EventRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.PiecePost;
import com.ceos.beatbuddy.domain.post.exception.PostErrorCode;
import com.ceos.beatbuddy.domain.post.repository.FreePostRepository;
import com.ceos.beatbuddy.domain.post.repository.PiecePostRepository;
import com.ceos.beatbuddy.domain.report.dto.ReportRequestDTO;
import com.ceos.beatbuddy.domain.report.entity.Report;
import com.ceos.beatbuddy.domain.report.entity.ReportTargetType;
import com.ceos.beatbuddy.domain.report.exception.ReportErrorCode;
import com.ceos.beatbuddy.domain.report.repository.ReportRepository;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.domain.venue.entity.VenueReview;
import com.ceos.beatbuddy.domain.venue.exception.VenueErrorCode;
import com.ceos.beatbuddy.domain.venue.repository.VenueRepository;
import com.ceos.beatbuddy.domain.venue.repository.VenueReviewRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.ceos.beatbuddy.domain.report.entity.ReportTargetType.FREE_POST;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final MemberService memberService;
    private final FreePostRepository freePostRepository;
    private final PiecePostRepository piecePostRepository;
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final CommentRepository freePostCommentRepository;
    private final EventCommentRepository eventCommentRepository;
    private final VenueReviewRepository venueReviewRepository;

    // 신고를 접수하는 메서드
    public void submitReport(ReportRequestDTO reportRequestDTO, Long reporterId) {
        // 멤버 유효성 검사
        Member reporter = memberService.validateAndGetMember(reporterId);

        // 타입 유효성 검사
        TitleContent titleContent = resolveTitleContent(ReportTargetType.from(reportRequestDTO.getTargetType()), reportRequestDTO.getTargetId());

        // ReportRequestDTO를 Report 엔티티로 변환
        Report report = ReportRequestDTO.toEntity(reportRequestDTO, titleContent.title(), titleContent.content());

        // 신고자를 설정
        report.setReporter(reporter);

        // 신고를 저장
        reportRepository.save(report);
    }

    private TitleContent resolveTitleContent(ReportTargetType type, Long targetId) {
        return switch (type) {
            case FREE_POST -> {
                FreePost post = freePostRepository.findById(targetId)
                        .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_EXIST));
                yield new TitleContent(post.getTitle(), post.getContent());
            }
            case PIECE_POST -> {
                PiecePost post = piecePostRepository.findById(targetId)
                        .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_EXIST));
                yield new TitleContent(post.getTitle(), post.getContent());
            }
            case EVENT -> {
                Event event = eventRepository.findById(targetId)
                        .orElseThrow(() -> new CustomException(EventErrorCode.NOT_FOUND_EVENT));
                yield new TitleContent(event.getTitle(), event.getContent());
            }
            case VENUE -> {
                Venue venue = venueRepository.findById(targetId)
                        .orElseThrow(() -> new CustomException(VenueErrorCode.VENUE_NOT_EXIST));
                yield new TitleContent(venue.getKoreanName(), venue.getDescription());
            }
            case FREE_POST_COMMENT -> {
                Comment comment = freePostCommentRepository.findById(targetId)
                        .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));
                yield new TitleContent("자유게시판 댓글", comment.getContent());
            }
            case EVENT_COMMENT -> {
                EventComment comment = eventCommentRepository.findById(targetId)
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COMMENT));
                yield new TitleContent("이벤트 댓글", comment.getContent());
            }
            case VENUE_COMMENT -> {
                VenueReview comment = venueReviewRepository.findById(targetId)
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COMMENT));
                yield new TitleContent("베뉴 댓글", comment.getContent());
            }
            default -> throw new CustomException(ReportErrorCode.INVALID_REPORT_TARGET_TYPE);
        };
    }

}
