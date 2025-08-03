package com.ceos.beatbuddy.global.util;

import com.ceos.beatbuddy.domain.post.entity.FixedHashtag;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

@Converter
public class FixedHashtagListConverter implements AttributeConverter<List<FixedHashtag>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<FixedHashtag> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            // Enum을 String으로 변환
            List<String> hashtagNames = attribute.stream()
                    .map(FixedHashtag::name)
                    .toList();
            return objectMapper.writeValueAsString(hashtagNames);
        } catch (Exception e) {
            throw new IllegalArgumentException("List<FixedHashtag> → JSON 변환 실패", e);
        }
    }

    @Override
    public List<FixedHashtag> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            // String을 Enum으로 변환
            List<String> hashtagNames = objectMapper.readValue(dbData, new TypeReference<List<String>>() {});
            return hashtagNames.stream()
                    .map(FixedHashtag::valueOf)
                    .toList();
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON → List<FixedHashtag> 변환 실패", e);
        }
    }
}