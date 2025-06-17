package com.ceos.beatbuddy.domain.mypage.controller;

import com.ceos.beatbuddy.domain.event.controller.EventApiDocs;
import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.domain.mypage.application.MyPageService;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
@Tag(name = "MyPage Controller", description = "마이페이지 기능, 규민이 새롭게 만든 기능만 여기에 있습니다.\n")
public class MyPageController implements MyPageApiDocs {
    private final MyPageService myPageService;

    @Override
    @GetMapping("/events")
    public ResponseEntity<ResponseDTO<Map<String, List<EventResponseDTO>>>> getMyEvents() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        Map<String, List<EventResponseDTO>> result = myPageService.getMyPageEvents(memberId);

        if (result.isEmpty()) {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_BUT_EMPTY_LIST.getHttpStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MY_EVENTS, result));
        }
        else {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_GET_MY_EVENTS.getHttpStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MY_EVENTS, result));
        }
    }
}
