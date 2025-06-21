package com.ceos.beatbuddy.domain.post.application;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.dto.PostRequestDto;
import com.ceos.beatbuddy.domain.post.entity.Piece;
import com.ceos.beatbuddy.domain.post.exception.PostErrorCode;
import com.ceos.beatbuddy.domain.post.repository.PieceRepository;
import com.ceos.beatbuddy.domain.venue.application.VenueInfoService;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PieceService {
    private final VenueInfoService venueInfoService;
    private final PieceRepository pieceRepository;

    public Piece createPiece(Member member, Long venueId, PostRequestDto.PiecePostRequestDto dto) {
        Venue venue = venueInfoService.validateAndGetVenue(venueId);
        Piece piece = Piece.builder()
                .member(member)
                .venue(venue)
                .eventDate(dto.eventDate())
                .totalPrice(dto.totalPrice())
                .totalMembers(dto.totalMembers())
                .build();
        return pieceRepository.save(piece);
    }

    public Piece validateAndGetPiece(Long pieceId) {
        return pieceRepository.findById(pieceId).orElseThrow(
                () -> new CustomException(PostErrorCode.PIECE_NOT_EXIST)
        );
    }
}
