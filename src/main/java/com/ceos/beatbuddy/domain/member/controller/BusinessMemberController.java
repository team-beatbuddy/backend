package com.ceos.beatbuddy.domain.member.controller;

import com.ceos.beatbuddy.domain.member.application.BusinessMemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business")
@RequiredArgsConstructor
@Tag(name = "Business Controller", description = "비즈니스 사용자 컨트롤러\n")
//        + "현재는 회원가입 관련 로직만 작성되어 있습니다\n"
//        + "추후 사용자 상세 정보, 아카이브를 조회하는 기능이 추가될 수 있습니다")
public class BusinessMemberController {
    private final BusinessMemberService businessMemberService;
}
