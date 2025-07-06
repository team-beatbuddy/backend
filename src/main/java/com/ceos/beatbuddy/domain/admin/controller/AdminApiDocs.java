package com.ceos.beatbuddy.domain.admin.controller;

import com.ceos.beatbuddy.domain.coupon.dto.CouponCreateRequestDTO;
import com.ceos.beatbuddy.domain.member.dto.AdminResponseDto;
import com.ceos.beatbuddy.domain.venue.dto.LoginRequest;
import com.ceos.beatbuddy.domain.venue.dto.VenueRequestDTO;
import com.ceos.beatbuddy.global.SwaggerExamples;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AdminApiDocs {
    @Operation(summary = "베뉴 정보 등록", description = "베뉴 정보를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "베뉴 정보 등록 성공"),
            @ApiResponse(responseCode = "400", description = "틀린 이미지 형식"),
            @ApiResponse(responseCode = "500", description = "이미지 업로드 실패")
    })
    ResponseEntity<Long> PostVenueInfo(@RequestBody VenueRequestDTO venueRequestDTO,
                                              @Parameter(description = "로고 이미지", required = false,
                                                      content = @Content(mediaType = "multipart/form-data"))
                                              @RequestParam(value = "file", required = false) MultipartFile logoImage,
                                              @Parameter(description = "배경 이미지, 비디오 파일", required = false,
                                                      content = @Content(mediaType = "multipart/form-data"))
                                              @RequestParam(value = "file", required = false) List<MultipartFile> backgroundImage)
            throws IOException;

    @Operation(summary = "베뉴 정보 수정", description = "베뉴 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "베뉴 정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "틀린 이미지 형식"),
            @ApiResponse(responseCode = "404", description = "베뉴 정보가 존재하지 않음"),
            @ApiResponse(responseCode = "500", description = "이미지 업로드 실패")
    })
    ResponseEntity<Long> updateVenueInfo(@PathVariable Long venueId,
                                                @RequestBody VenueRequestDTO venueRequestDTO,
                                                @Parameter(description = "로고 이미지", required = false,
                                                        content = @Content(mediaType = "multipart/form-data"))
                                                @RequestParam(value = "file", required = false) MultipartFile logoImage,
                                                @Parameter(description = "배경 이미지, 비디오 파일", required = false,
                                                        content = @Content(mediaType = "multipart/form-data"))
                                                @RequestParam(value = "file", required = false) List<MultipartFile> backgroundImage)
            throws IOException;

    @Operation(summary = "id를 통한 토큰 발급", description = "기존에 생성된 id를 통해 토큰을 발급받습니다.")
    @Parameter(description = "미리 생성된 id"
            , content = @Content(mediaType = "text/plain")
            , schema = @Schema(implementation = LoginRequest.class))
    @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AdminResponseDto.class)))
    ResponseEntity<AdminResponseDto> login(@RequestBody LoginRequest request);

    @Operation(summary = "베뉴 정보 삭제", description = "베뉴 정보를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "베뉴 정보 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "베뉴 정보가 존재하지 않음")
    })
    ResponseEntity<Long> DeleteVenueInfo(@PathVariable Long venueId);


    @Operation(summary = "쿠폰 등록", description = """
            새로운 쿠폰을 등록합니다.
            - 쿠폰 등록 시 쿠폰의 이름, 설명, 만료일, 수량 등을 포함해야 합니다.
            - active: DAILY, ONCE (매일 새롭게 발급되는 쿠폰, 아니면 한 번만 발급되는 쿠폰)
            - 매일 새롭게 발급되는 쿠폰의 경우 당일에 발급을 받았으면 다시 발급받을 수 없습니다. 하루가 지나면 발급 가능해집니다.
            - 한 번만 발급되는 쿠폰의 경우, 발급받으면 다시 발급받을 수 없습니다.
            - 쿠폰 등록 시, 쿠폰의 수량이 초기화되어야 합니다.
            - 쿠폰 등록 후, 쿠폰의 수량이 초기화되지 않으면 쿠폰을 발급받을 수 없습니다.
            """)
    @ApiResponse(responseCode = "200", description = "쿠폰 등록 성공",
            content = @Content(mediaType = "application/json",
                                 examples = @ExampleObject(name = "쿠폰 등록 성공 예시",
                            value = """
                                    {
                                      "status": 201,
                                      "code": "SUCCESS_CREATE_COUPON",
                                      "message": "쿠폰을 성공적으로 등록했습니다.",
                                      "data": "쿠폰 등록 성공"
                                    }
                                    """)))
    @ApiResponse(responseCode = "400", description = "쿠폰 등록 실패",
            content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "쿠폰 수량 초기화 X", value = SwaggerExamples.COUPON_QUOTA_NOT_INITIALIZED, description = "쿠폰 수량이 초기화되지 않았을 때의 응답 예시"),
                            @ExampleObject(name = "쿠폰을 만료된 날짜로 등록 시", value = SwaggerExamples.COUPON_EXPIRED, description = "쿠폰이 만료되었을 때의 응답 예시"),
                            @ExampleObject(name = "잘못된 쿠폰 정책", value = SwaggerExamples.COUPON_INVALID_POLICY, description = "쿠폰 정책이 잘못되었을 때의 응답 예시"),
                    }))
    @ApiResponse(responseCode = "404", description = "리소스 없음",
            content = @Content(mediaType = "application/json",
                    examples = {@ExampleObject(name = "존재하지 않는 베뉴", value = SwaggerExamples.VENUE_NOT_EXIST, description = "존재하지 않는 업장에 쿠폰을 등록하려 할 때의 응답 예시"),
                            @ExampleObject(name = "존재하지 않는 멤버", value = SwaggerExamples.MEMBER_NOT_EXIST, description = "존재하지 않는 멤버에 쿠폰을 등록하려 할 때의 응답 예시")}))
    ResponseEntity<ResponseDTO<String>> createCoupon(@RequestBody CouponCreateRequestDTO request);


    @Operation(summary = "쿠폰 사용", description = "쿠폰을 사용합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "쿠폰 사용 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "쿠폰 사용 성공 예시",
                                    value = """
                                            {
                                              "status": 200,
                                              "code": "SUCCESS_USE_COUPON",
                                              "message": "쿠폰을 성공적으로 사용했습니다.",
                                              "data": "쿠폰 사용 성공"
                                            }
                                            """))),
            @ApiResponse(responseCode = "404", description = "쿠폰을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            examples = {@ExampleObject(name = "존재하지 않는 쿠폰", value = SwaggerExamples.NOT_FOUND_COUPON_RECEIVE, description = "존재하지 않는 쿠폰을 사용하려 할 때의 응답 예시"),
                                    @ExampleObject(name = "admin이 등록하지 않은 쿠폰", value = SwaggerExamples.COUPON_NOT_FOUND, description = "존재하지 않는 쿠폰을 사용하려 할 때의 응답 예시"),
                                    @ExampleObject(name = "존재하지 않는 멤버", value = SwaggerExamples.MEMBER_NOT_EXIST, description = "존재하지 않는 멤버에 쿠폰을 사용하려 할 때의 응답 예시")}))
    })
    @ApiResponse(responseCode = "400", description = "쿠폰 사용 실패",
            content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "쿠폰 만료", value = SwaggerExamples.COUPON_EXPIRED, description = "쿠폰이 만료되었을 때의 응답 예시"),
                            @ExampleObject(name = "이미 사용된 쿠폰", value = SwaggerExamples.COUPON_ALREADY_USED, description = "이미 사용된 쿠폰을 다시 사용하려 할 때의 응답 예시")
                    }))
    ResponseEntity<ResponseDTO<String>> useCoupon(
            @PathVariable Long receiveCouponId);
}
