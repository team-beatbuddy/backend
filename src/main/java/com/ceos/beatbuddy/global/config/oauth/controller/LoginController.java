package com.ceos.beatbuddy.global.config.oauth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
    @GetMapping("/login/oauth2/callback/{provider}")
    public ResponseEntity<String> handleCallback(@PathVariable String provider,
                                                 @RequestParam String access) {
        // access를 검증하거나, 리디렉트 처리
        return ResponseEntity.ok("OAuth2 callback handled for " + provider);
    }
}
