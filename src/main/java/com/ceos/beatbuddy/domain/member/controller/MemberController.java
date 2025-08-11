package com.ceos.beatbuddy.domain.member.controller;

import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.application.OnboardingService;
import com.ceos.beatbuddy.domain.member.dto.*;
import com.ceos.beatbuddy.domain.member.dto.api.MemberProfileSummaryApi;
import com.ceos.beatbuddy.domain.member.dto.api.ResponseApi;
import com.ceos.beatbuddy.domain.member.entity.PostProfileInfo;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.ResponseTemplate;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Tag(name = "Member Controller", description = "사용자 컨트롤러\n"
        + "현재는 회원가입 관련 로직만 작성되어 있습니다\n"
        + "추후 사용자 상세 정보, 아카이브를 조회하는 기능이 추가될 수 있습니다")
public class MemberController implements MemberApiDocs{
    private final MemberService memberService;
    private final OnboardingService onboardingService;

    @GetMapping("/onboarding")
    @Operation(summary = "사용자 온보딩 완료 현황 조회", description = "사용자의 완료한 온보딩 단계를 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 온보딩 현황 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OnboardingResponseDto.class)))
            ,
            @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<OnboardingResponseDto> getOnboardingSet() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(onboardingService.isOnboarding(memberId));
    }

    @PostMapping("/onboarding/consent")
    @Operation(summary = "사용자 약관 동의", description = "어플리케이션 약관 동의")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "약관 동의 저장 성공"
                    , content = @Content(mediaType = "application/json"
                    , schema = @Schema(implementation = MemberResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<MemberResponseDTO> saveTermConsent(@RequestBody MemberConsentRequestDTO memberConsentRequestDTO) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(onboardingService.saveMemberConsent(memberId, memberConsentRequestDTO));
    }

    @GetMapping("/onboarding/consent")
    @Operation(summary = "사용자 약관 동의 여부", description = "사용자의 약관 동의 여부를 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "약관 동의 여부 확인 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<Boolean> getTermConsentSet() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(onboardingService.isTermConsent(memberId));
    }

    @PostMapping("/onboarding/nickname/duplicate")
    @Operation(summary = "사용자 닉네임 중복확인", description = "사용자가 입력한 닉네임 중복 여부 확인")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "닉네임 중복이 아니면 true를 반환합니다."
                    , content = @Content(mediaType = "application/json"
                    , schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class))),
            @ApiResponse(responseCode = "409", description = "중복된 닉네임입니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<Boolean> isNicknameDuplicate(@RequestBody NicknameDTO nicknameDTO) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(onboardingService.isDuplicate(memberId, nicknameDTO));
    }

    @PostMapping("/onboarding/nickname/validate")
    @Operation(summary = "사용자 닉네임 오류 확인", description = "사용자가 입력한 닉네임 사용 가능 확인")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용가능한 닉네임이면 true를 반환합니다."
                    , content = @Content(mediaType = "application/json"
                    , schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class))),
            @ApiResponse(responseCode = "400", description = "특수문자, 공백, 길이 조건에 위배되는 것이 있습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<Boolean> isNicknameValidate(@RequestBody NicknameDTO nicknameDTO) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(onboardingService.isValidate(memberId, nicknameDTO));
    }

    @PostMapping("/onboarding/nickname")
    @Operation(summary = "사용자 닉네임 저장", description = "사용자가 입력한 닉네임으로 저장")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "닉네임을 저장에 성공하면 유저의 정보를 반환합니다."
                    , content = @Content(mediaType = "application/json"
                    , schema = @Schema(implementation = MemberResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<MemberResponseDTO> saveNickname(@RequestBody NicknameDTO nicknameDTO) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(onboardingService.saveNickname(memberId, nicknameDTO));
    }

    @GetMapping("/onboarding/nickname")
    @Operation(summary = "사용자 닉네임 설정 여부", description = "사용자가 닉네임을 설정했는 지 여부를 확인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 닉네임 설정 여부 확인 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<Boolean> getNicknameSet() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(memberService.getNicknameSet(memberId));
    }

    @PostMapping("/onboarding/regions")
    @Operation(summary = "사용자 관심지역 설정", description = "사용자의 관심지역을 설정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관심지역 설정에 성공하였습니다."
                    , content = @Content(mediaType = "application/json"
                    , schema = @Schema(implementation = MemberResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<MemberResponseDTO> saveRegions(@RequestBody RegionRequestDTO regionRequestDTO) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(onboardingService.saveRegions(memberId, regionRequestDTO));
    }

    @GetMapping("/nickname")
    @Operation(summary = "사용자 닉네임 조회", description = "사용자의 닉네임을 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 닉네임 조회에 성공하였습니다."
                    , content = @Content(mediaType = "application/json"
                    , schema = @Schema(implementation = NicknameDTO.class))),
            @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<NicknameDTO> getNickname() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(memberService.getNickname(memberId));
    }

    @GetMapping("/preferences")
    @Operation(summary = "사용자 관심 리스트 반환", description = "사용자가 가장 최근에 설정한 관심지역, 장르, 무드 취향 리스트를 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 취향 리스트 조회에 성공했습니다."
                    , content = @Content(mediaType = "application/json"
                    , schema = @Schema(implementation = MemberResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다 or 유저가 설정한 장르 취향이 존재하지 않습니다 or 유저가 설정한 무드 취향이 존재하지 않습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<List<String>> getPreferences() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(memberService.getPreferences(memberId));
    }
    @Override
    @PatchMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO<String>> uploadProfileImage(
            @RequestPart("image") MultipartFile image) throws IOException {

        Long memberId = SecurityUtils.getCurrentMemberId(); // 현재 로그인된 사용자 ID
        memberService.uploadProfileImage(memberId, image);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_UPLOAD_PROFILE_IMAGE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_UPLOAD_PROFILE_IMAGE, "프로필 사진 업로드 완료"));
    }

    @GetMapping("/profile/summary")
    @Operation(summary = "회원 프로필 요약 정보 조회", description = "회원의 프로필 요약 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 요약 정보 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MemberProfileSummaryApi.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청: memberId를 입력하지 않음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class))),
            @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<ResponseDTO<MemberProfileSummaryDTO>> getProfileSummary(
        @RequestParam(required = false) Long memberId
    ) {
        if (memberId == null) {
            memberId = SecurityUtils.getCurrentMemberId(); // 현재 로그인된 사용자 ID
        }
        MemberProfileSummaryDTO result = memberService.getProfileSummary(memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_PROFILE_SUMMARY.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_PROFILE_SUMMARY, result));
    }

    @Override
    @PatchMapping("/nickname")
    public ResponseEntity<ResponseDTO<MemberResponseDTO>> updateNickname(
        @Valid @RequestBody NicknameDTO nicknameDTO
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId(); // 현재 로그인된 사용자 ID
        MemberResponseDTO result = null;
        try {
            result = memberService.updateNickname(memberId, nicknameDTO);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new CustomException(MemberErrorCode.NICKNAME_CONFLICT); // 사용자에게 충돌 알림
        }

        return ResponseEntity
                .status(SuccessCode.SUCCESS_UPDATE_NICKNAME.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_UPDATE_NICKNAME, result));
    }

    @Operation(
        summary = "FCM 토큰 업데이트",
        description = "사용자의 FCM 토큰을 업데이트합니다. 이 토큰은 푸시 알림 전송에 사용됩니다."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "FCM 토큰 업데이트 성공",
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                responseCode = "400",
                description = "잘못된 요청: 토큰이 비어있거나 형식이 잘못됨",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    @PostMapping("/fcm-token")
    public ResponseEntity<Void> updateFcmToken(@RequestBody @Valid FcmTokenUpdateDTO dto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        memberService.updateFcmToken(memberId, dto.getToken());

        return ResponseEntity.ok().build();
    }


    // ============ Member Post Profile Endpoints ============
    @PostMapping(value = "/post-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "사용자 게시물 프로필 정보 저장", description = "사용자의 게시물 프로필 정보를 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시물 프로필 정보 저장 성공",
                    content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ResponseApi.class))),
            @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<ResponseDTO<String>> savePostProfile(@Valid @RequestPart("postProfileRequestDTO") PostProfileRequestDTO postProfileRequestDTO,
                                                             @RequestPart(value = "postProfileImage", required = false) MultipartFile postProfileImage) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        onboardingService.savePostProfile(memberId, postProfileRequestDTO, postProfileImage);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_SAVE_POST_PROFILE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_SAVE_POST_PROFILE, "게시물 프로필 정보 저장 성공"));
    }

    @PatchMapping(value = "/post-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "게시물 프로필 수정", description = "사용자의 게시물 프로필 닉네임 및 이미지를 수정합니다. 각 필드는 null이 아니고 비어있지 않을 때만 수정됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시물 프로필 수정 성공",
                    content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<ResponseDTO<String>> updatePostProfile(
            @RequestPart(value = "postProfileRequestDTO", required = false) PostProfileRequestDTO postProfileRequestDTO,
            @RequestPart(value = "postProfileImage", required = false) MultipartFile postProfileImage) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        onboardingService.updatePostProfile(memberId, postProfileRequestDTO, postProfileImage);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_UPDATE_POST_PROFILE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_UPDATE_POST_PROFILE, "게시물 프로필 수정 성공"));
    }

    @GetMapping("/post-profile")
    @Operation(summary = "사용자 게시물 프로필 정보 조회", description = "사용자의 게시물 프로필 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시물 프로필 정보 조회 성공",
                    content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = PostProfileInfo.class))),
            @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<ResponseDTO<PostProfileInfo>> getPostProfile() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        PostProfileInfo postProfileInfo = memberService.getPostProfile(memberId);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_POST_PROFILE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_POST_PROFILE, postProfileInfo));
    }

    // ============= Member Blocking Endpoints =============

    @Override
    @PostMapping("/block")
    public ResponseEntity<ResponseDTO<String>> blockMember(@Valid @RequestBody MemberBlockRequestDTO request) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        memberService.blockMember(memberId, request.getBlockedMemberId());
        return ResponseEntity
                .status(SuccessCode.SUCCESS_BLOCK_MEMBER.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_BLOCK_MEMBER, "성공적으로 차단했습니다."));
    }
    
//    @DeleteMapping("/block/{blockedMemberId}")
//    public ResponseEntity<ResponseDTO<String>> unblockMember(@PathVariable @NotNull(message = "차단을 해제할 멤버 ID 는 필수입니다.") Long blockedMemberId) {
//        Long memberId = SecurityUtils.getCurrentMemberId();
//        memberService.unblockMember(memberId, blockedMemberId);
//
//        return ResponseEntity
//                .status(SuccessCode.SUCCESS_UNBLOCK_MEMBER.getStatus().value())
//                .body(new ResponseDTO<>(SuccessCode.SUCCESS_UNBLOCK_MEMBER, "성공적으로 차단을 해제했습니다."));
//    }
//
//    @GetMapping("/blocked")
//    public ResponseEntity<ResponseDTO<List<MemberBlockResponseDTO>>> getBlockedMembers() {
//        Long memberId = SecurityUtils.getCurrentMemberId();
//        List<Long> blockedMemberIds = memberService.getBlockedMemberIds(memberId);
//
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/blocked/{targetMemberId}")
//    @Operation(summary = "특정 멤버 차단 여부 확인", description = "특정 멤버가 차단되어 있는지 확인합니다.")
//    @ApiResponses({
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "차단 여부 확인 성공",
//                    content = @Content(mediaType = "application/json"))
//    })
//    public ResponseEntity<Boolean> isBlockedMember(@PathVariable Long targetMemberId) {
//        Long memberId = SecurityUtils.getCurrentMemberId();
//        boolean isBlocked = memberService.isBlocked(memberId, targetMemberId);
//
//        return ResponseEntity.ok(isBlocked);
//    }
}
