package com.ceos.beatbuddy.global.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
@Slf4j
public class TestController {

    @PostMapping("/error500")
    @Operation(summary = "500 에러 테스트", 
               description = "body와 query 파라미터를 받아서 의도적으로 500 에러를 발생시킵니다.")
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
    public ResponseEntity<?> testError500(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type
    ) {
        log.info("테스트 요청 받음 - body: {}, query: {}, type: {}", body, query, type);
        
        // 의도적으로 500 에러 발생
        throw new RuntimeException("테스트용 500 에러입니다! body: " + body + ", query: " + query + ", type: " + type);
    }

    @GetMapping("/error500-get")
    @Operation(summary = "GET 방식 500 에러 테스트")
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
    public ResponseEntity<?> testError500Get(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String message
    ) {
        log.info("GET 테스트 요청 받음 - query: {}, type: {}, message: {}", query, type, message);
        
        // 의도적으로 500 에러 발생
        throw new RuntimeException("GET 테스트용 500 에러입니다! query: " + query + ", type: " + type + ", message: " + message);
    }

    @PostMapping("/normal")
    @Operation(summary = "정상 응답 테스트")
    public ResponseEntity<?> testNormal(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok().body(Map.of(
                "status", "success", 
                "message", "정상 처리되었습니다",
                "received", body
        ));
    }
}