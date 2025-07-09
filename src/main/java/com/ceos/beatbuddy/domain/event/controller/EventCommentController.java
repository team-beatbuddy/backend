package com.ceos.beatbuddy.domain.event.controller;

import com.ceos.beatbuddy.domain.event.application.EventCommentService;
import com.ceos.beatbuddy.domain.event.dto.EventCommentCreateRequestDTO;
import com.ceos.beatbuddy.domain.event.dto.EventCommentResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventCommentTreeResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventCommentUpdateDTO;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event-comments/")
@Tag(name = "Event Comment Controller", description = "이벤트 댓글 관련 컨트롤러입니다.")
@RequiredArgsConstructor
public class EventCommentController implements EventCommentApiDocs {
    private final EventCommentService eventCommentService;

    @Override
    @PostMapping("/{eventId}/comments")
    public ResponseEntity<ResponseDTO<EventCommentResponseDTO>> createComment(
            @PathVariable Long eventId,
            @Valid @RequestBody EventCommentCreateRequestDTO dto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventCommentResponseDTO result = eventCommentService.createComment(eventId, memberId, dto, dto.getParentCommentId());

        return ResponseEntity
                .status(SuccessCode.SUCCESS_CREATED_COMMENT.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_CREATED_COMMENT, result));
    }

    @Override
    @DeleteMapping("/{eventId}/comments/{commentId}")
    public ResponseEntity<ResponseDTO<String>> deleteComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId) {

        Long memberId = SecurityUtils.getCurrentMemberId();
        eventCommentService.deleteComment(eventId, commentId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_DELETE_COMMENT.getHttpStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_DELETE_COMMENT, "댓글 삭제 완료"));
    }


    @Override
    @GetMapping("/{eventId}/comments")
    public ResponseEntity<ResponseDTO<List<EventCommentTreeResponseDTO>>> getEventComments(@PathVariable Long eventId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        List<EventCommentTreeResponseDTO> result = eventCommentService.getSortedEventComments(memberId, eventId);

        if (result.isEmpty()) {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_BUT_EMPTY_LIST.getHttpStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_BUT_EMPTY_LIST, result));
        }
        else {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_GET_EVENT_COMMENTS.getHttpStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_EVENT_COMMENTS, result));
        }
    }


    @Override
    @PatchMapping("/{eventId}/comments/{commentId}")
    public ResponseEntity<ResponseDTO<EventCommentResponseDTO>> updateComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId,
            @Valid @RequestBody EventCommentUpdateDTO dto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventCommentResponseDTO result = eventCommentService.updateComment(eventId, commentId, memberId, dto);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_UPDATE_COMMENT.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_UPDATE_COMMENT, result));
    }
}
