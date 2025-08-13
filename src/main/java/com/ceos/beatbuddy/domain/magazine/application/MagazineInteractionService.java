package com.ceos.beatbuddy.domain.magazine.application;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.magazine.repository.MagazineRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.MagazineLike;
import com.ceos.beatbuddy.domain.scrapandlike.repository.MagazineLikeRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MagazineInteractionService {
    private final MagazineLikeRepository magazineLikeRepository;
    private final MemberService memberService;
    private final MagazineRepository magazineRepository;
    private final MagazineValidator magazineValidator;


    /**
     * 지정된 회원이 해당 매거진에 좋아요를 등록합니다.
     *
     * @param magazineId 좋아요를 등록할 매거진의 ID
     * @param memberId 좋아요를 수행하는 회원의 ID
     * @return 좋아요 등록 후의 매거진 상세 DTO
     * @throws CustomException 매거진이 존재하지 않거나, 표시되지 않거나, 이미 좋아요를 등록한 경우
     */
    @Transactional
    public void likeMagazine(Long magazineId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        // 엔티티 검색
        Magazine magazine = magazineValidator.validateAndGetMagazineVisibleTrue(magazineId);

        // 좋아요 증가 (이미 좋아요가 있으면 예외처리
        boolean alreadyLiked = magazineLikeRepository.existsByMemberIdAndMagazineId(memberId, magazineId);

        if (alreadyLiked) {
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        }

        MagazineLike entity = MagazineLike.toEntity(member, magazine);
        magazineLikeRepository.save(entity);
        magazineRepository.increaseLike(magazineId);
    }

    /**
     * 지정된 매거진에 대해 해당 회원의 좋아요를 제거합니다.
     *
     * @param magazineId 좋아요를 제거할 매거진의 ID
     * @param memberId 좋아요를 제거하는 회원의 ID
     * @return 좋아요 제거 후의 매거진 상세 DTO
     * @throws CustomException 좋아요가 존재하지 않거나, 매거진/회원이 존재하지 않는 경우
     */
    @Transactional
    public void deleteLikeMagazine(Long magazineId, Long memberId) {
        memberService.validateAndGetMember(memberId);

        // 엔티티 검색
        magazineValidator.validateAndGetMagazine(magazineId);

        // 좋아요 삭제 (실제 삭제된 행 수 확인)
        int deletedCount = magazineLikeRepository.deleteByMemberIdAndMagazineId(memberId, magazineId);
        if (deletedCount == 0) {
            throw new CustomException(ErrorCode.NOT_FOUND_LIKE);
        }
        
        if (deletedCount > 1) {
            log.warn("Multiple magazine likes deleted for single request - magazineId: {}, memberId: {}, deletedCount: {}", 
                    magazineId, memberId, deletedCount);
        }

        // 실제 삭제된 수만큼 카운트 감소
        if (deletedCount > 1) {
            magazineRepository.decreaseLike(magazineId, deletedCount);
        } else {
            magazineRepository.decreaseLike(magazineId);
        }
    }
}
