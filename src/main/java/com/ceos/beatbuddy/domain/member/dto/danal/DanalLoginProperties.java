package com.ceos.beatbuddy.domain.member.dto.danal;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter @Setter
@Component
@ConfigurationProperties(prefix = "danal.login")
public class DanalLoginProperties {
    private String url;
    private String txType;     // SIMDELIVER
    private String service;    // UAS
    private String authType;   // 36
    private String cpid;
    private String cppwd;
    private String ipaddr;
    private String cptitle;    // 앱명 또는 등록 FQDN
}