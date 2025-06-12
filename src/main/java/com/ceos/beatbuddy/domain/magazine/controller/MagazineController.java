package com.ceos.beatbuddy.domain.magazine.controller;

import com.ceos.beatbuddy.domain.magazine.application.MagazineService;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineRequestDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineResponseDTO;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
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
public class MagazineController implements MagazineApiDocs{
    private final MagazineService magazineService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO<?>> addMagazine(
            @Valid @RequestPart("magazineRequestDTO") MagazineRequestDTO magazineRequestDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        MagazineResponseDTO result = magazineService.addMagazine(memberId, magazineRequestDTO, images);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_CREATED_MAGAZINE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_CREATED_MAGAZINE, result));
    }
}
