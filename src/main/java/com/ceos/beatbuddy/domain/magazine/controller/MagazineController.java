package com.ceos.beatbuddy.domain.magazine.controller;

import com.ceos.beatbuddy.domain.magazine.application.MagazineService;
import com.ceos.beatbuddy.domain.magazine.dto.*;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/magazines")
@RequiredArgsConstructor
@Tag(name = "Magazine Controller", description = "매거진 기능\n")
public class MagazineController implements MagazineApiDocs{
    private final MagazineService magazineService;

    // 매거진 작성
    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO<MagazineDetailDTO>> addMagazine(
            @Valid @RequestPart("magazineRequestDTO") MagazineRequestDTO magazineRequestDTO,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        MagazineDetailDTO result = magazineService.addMagazine(memberId, magazineRequestDTO, images, thumbnailImage);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_CREATED_MAGAZINE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_CREATED_MAGAZINE, result));
    }

    // 매거진 불러오기 (admin 이 가능하게 한 것만 + 홈에 보이게 한 것만 pinned)
    @Override
    @GetMapping("/home")
    public ResponseEntity<ResponseDTO<List<MagazineHomeResponseDTO>>> readMagazineList() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        List<MagazineHomeResponseDTO> result = magazineService.readHomeMagazines(memberId);

        if (result.isEmpty()) {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_BUT_EMPTY_LIST.getStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_BUT_EMPTY_LIST, result));
        }

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_MAGAZINE_LIST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MAGAZINE_LIST, result));
    }

    // 매거진 전체 불러오기도 필요함.
    // 매거진 전체 불러오기
    @Override
    @GetMapping("/all")
    public ResponseEntity<ResponseDTO<MagazinePageResponseDTO>> readAllMagazines(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        MagazinePageResponseDTO result = magazineService.readAllMagazines(memberId, page, size);

        if (result.getMagazines().isEmpty()) {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_BUT_EMPTY_LIST.getStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_BUT_EMPTY_LIST, result));
        }

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_MAGAZINE_LIST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MAGAZINE_LIST, result));
    }

    // 매거진 상세 보기
    @Override
    @GetMapping("/{magazineId}")
    public ResponseEntity<ResponseDTO<MagazineDetailDTO>> readDetailMagazine(@PathVariable Long magazineId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        MagazineDetailDTO result = magazineService.readDetailMagazine(memberId, magazineId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_MAGAZINE_LIST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MAGAZINE_LIST, result));
    }

    // 매거진 수정
    @Override
    @PatchMapping(value = "/{magazineId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO<MagazineDetailDTO>> updateMagazine(
            @PathVariable Long magazineId,
            @RequestPart("magazineRequestDTO") MagazineUpdateRequestDTO magazineRequestDTO,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        MagazineDetailDTO result = magazineService.updateMagazine(memberId, magazineId, magazineRequestDTO, images, thumbnailImage);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_UPDATE_MAGAZINE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_UPDATE_MAGAZINE, result));
    }

    // 매거진 삭제
    @Override
    @DeleteMapping("/{magazineId}")
    public ResponseEntity<ResponseDTO<String>> deleteMagazine(@PathVariable Long magazineId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        magazineService.deleteMagazine(memberId, magazineId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_DELETE_MAGAZINE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_DELETE_MAGAZINE, "매거진이 성공적으로 삭제되었습니다."));
    }
}
