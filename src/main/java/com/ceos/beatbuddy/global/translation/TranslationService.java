package com.ceos.beatbuddy.global.translation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private final PapagoProperties papagoProperties;
    
    private static final String PAPAGO_URL = "https://papago.apigw.ntruss.com/nmt/v1/translation";
    
    /**
     * 한국어 텍스트를 영어로 번역
     */
    public String translateToEnglish(String koreanText) {
        if (koreanText == null || koreanText.trim().isEmpty()) {
            return null;
        }
        
        return translate(koreanText, "auto", "en");
    }
    
    /**
     * 번역 API 호출
     */
    private String translate(String text, String sourceLang, String targetLang) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("X-NCP-APIGW-API-KEY-ID", papagoProperties.getClientId());
            headers.set("X-NCP-APIGW-API-KEY", papagoProperties.getClientSecret());
            
            Map<String, String> body = new HashMap<>();
            body.put("source", sourceLang);
            body.put("target", targetLang);
            body.put("text", text);
            
            String encodedBody = body.entrySet().stream()
                    .map(entry -> {
                        try {
                            return entry.getKey() + "=" + java.net.URLEncoder.encode(entry.getValue(), "UTF-8");
                        } catch (java.io.UnsupportedEncodingException e) {
                            return entry.getKey() + "=" + entry.getValue();
                        }
                    })
                    .reduce((p1, p2) -> p1 + "&" + p2)
                    .orElse("");
            
            HttpEntity<String> request = new HttpEntity<>(encodedBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(PAPAGO_URL, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
                Map<String, Object> message = (Map<String, Object>) responseMap.get("message");
                Map<String, String> result = (Map<String, String>) message.get("result");
                
                String translatedText = result.get("translatedText");
                log.debug("번역 완료: '{}' -> '{}' ({}->{})", 
                    text, translatedText, result.get("srcLangType"), result.get("tarLangType"));
                
                return translatedText;
            }
            
            log.warn("번역 API 응답 실패: {}", response.getStatusCode());
            return null;
            
        } catch (Exception e) {
            log.error("번역 중 오류 발생: text={}, error={}", text, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 여러 텍스트를 한 번에 번역 (배치 처리)
     */
    public Map<String, String> translateBatch(Map<String, String> texts) {
        Map<String, String> translations = new HashMap<>();
        
        for (Map.Entry<String, String> entry : texts.entrySet()) {
            String translated = translateToEnglish(entry.getValue());
            if (translated != null) {
                translations.put(entry.getKey(), translated);
            }
        }
        
        return translations;
    }
}