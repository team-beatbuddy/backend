package com.ceos.beatbuddy.domain.member.application;

import com.ceos.beatbuddy.domain.member.dto.danal.DanalLoginProperties;
import com.ceos.beatbuddy.global.config.DanalConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DanalAuthService {

    @Qualifier("danalRestTemplate")
    private final RestTemplate danalRestTemplate;

    private final DanalLoginProperties props;

    public String requestPhoneAuth(String carrier, boolean mvno,
                                   String phoneDigits, String iden7, String name, String userId) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/x-www-form-urlencoded; charset=EUC-KR"));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("TXTYPE",   props.getTxType());
        body.add("SERVICE",  props.getService());
        body.add("AUTHTYPE", props.getAuthType());
        body.add("CPID",     props.getCpid());
        body.add("CPPWD",    props.getCppwd());
        body.add("IPADDR",   props.getIpaddr());
        body.add("CPTITLE",  props.getCptitle());

        body.add("CARRIER",  carrier);                 // SKT | KT | LGT
        if (mvno) body.add("SUBTXTYPE", "MVNODELIVER");

        body.add("PHONE",    phoneDigits);             // 01012345678
        body.add("IDEN",     iden7);                   // YYMMDDX
        body.add("NAME",     name);                    // 가입자명(한글)

        if (userId != null) body.add("USERID", userId);
        //if (orderId != null)  body.add("ORDERID", orderId);
        //if (ageLimit != null) body.add("AGELIMIT", ageLimit);
        return danalRestTemplate.postForObject(props.getUrl(), new HttpEntity<>(body, headers), String.class);
    }

    // 받은 인증 코드 전달
    public Map<String, String> requestPhoneAuthResult(String tid, String authCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/x-www-form-urlencoded; charset=EUC-KR"));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("TXTYPE",   "SIMCONFIRM");
        body.add("TID", tid);
        body.add("OTP", authCode); // 인증 코드

        String result = danalRestTemplate.postForObject(props.getUrl(), new HttpEntity<>(body, headers), String.class);
        return DanalConfig.parseResponse(result);
    }

}