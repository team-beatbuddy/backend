package com.ceos.beatbuddy.domain.magazine.controller;

import com.ceos.beatbuddy.domain.magazine.application.MagazineService;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineDetailDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineHomeResponseDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineRequestDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineResponseDTO;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.Getter;
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

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO<MagazineResponseDTO>> addMagazine(
            @Valid @RequestPart("magazineRequestDTO") MagazineRequestDTO magazineRequestDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        MagazineResponseDTO result = magazineService.addMagazine(memberId, magazineRequestDTO, images);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_CREATED_MAGAZINE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_CREATED_MAGAZINE, result));
    }

    @Override
    @GetMapping()
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

    @Override
    @GetMapping("/{magazineId}")
    public ResponseEntity<ResponseDTO<MagazineDetailDTO>> readDetailMagazine(@PathVariable Long magazineId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        MagazineDetailDTO result = magazineService.readDetailMagazine(memberId, magazineId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_MAGAZINE_LIST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MAGAZINE_LIST, result));
    }

    @PostMapping("/{magazineId}")
    public ResponseEntity<ResponseDTO<MagazineDetailDTO>> scrapMagazine(@PathVariable Long magazineId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        MagazineDetailDTO result = magazineService.scrapMagazine(memberId, magazineId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_MAGAZINE_LIST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MAGAZINE_LIST, result));
    }
}
