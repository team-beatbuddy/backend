package com.ceos.beatbuddy.global.translation;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "papago")
@Getter
@Setter
public class PapagoProperties {
    private String clientId;
    private String clientSecret;
}