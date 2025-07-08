package com.ceos.beatbuddy.domain.magazine.application;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.magazine.exception.MagazineErrorCode;
import com.ceos.beatbuddy.domain.magazine.repository.MagazineRepository;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MagazineValidator {
    private final MagazineRepository magazineRepository;

    /**
     * ID를 기반으로 표시 가능한(visible) 매거진을 조회하며, 존재하지 않거나 표시 불가능한 경우 예외를 발생시킵니다.
     *
     * @param magazineId 조회할 매거진의 ID
     * @return 존재하고 표시 가능한 매거진 엔티티
     * @throws CustomException 매거진이 존재하지 않거나 표시 불가능한 경우
     */
    protected Magazine validateAndGetMagazineVisibleTrue(Long magazineId) {
        return magazineRepository.findByIdAndIsVisibleTrue(magazineId).orElseThrow(() ->
                new CustomException(MagazineErrorCode.MAGAZINE_NOT_EXIST));
    }

    /**
     * 표시 여부와 관계없이 ID를 기반으로 매거진을 조회합니다.
     *
     * @param magazineId 조회할 매거진의 ID
     * @return 해당 ID를 가진 매거진 엔티티
     * @throws CustomException 매거진이 존재하지 않는 경우
     */
    protected Magazine validateAndGetMagazine(Long magazineId) {
        return magazineRepository.findById(magazineId).orElseThrow(() ->
                new CustomException(MagazineErrorCode.MAGAZINE_NOT_EXIST));
    }

}
