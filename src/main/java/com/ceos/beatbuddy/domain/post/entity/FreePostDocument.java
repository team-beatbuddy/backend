package com.ceos.beatbuddy.domain.post.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

@Document(indexName = "post")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FreePostDocument {
    @Id
    private Long id;

    private String title;
    private String content;
    private List<String> hashtags;

    public static FreePostDocument toDTO(FreePost post) {
        return FreePostDocument.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .hashtags(post.getHashtag() != null ?
                        post.getHashtag().stream()
                                .map(FixedHashtag::getDisplayName)
                                .toList() : List.of())
                .build();
    }
}