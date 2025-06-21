package com.ceos.beatbuddy.domain.post.application;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.dto.PostCreateRequestDTO;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostTypeHandler {
    // New version
    Post createPost (PostCreateRequestDTO dto, Member member, List<String> imageUrls);
    Post readPost(Long postId);
    void deletePost(Long postId, Member member);
    Post validateAndGetPost(Long postId);
    Boolean isWriter(Post post, Member member);
    Page<? extends Post> readAllPosts(Pageable pageable);
    boolean supports(Post post);
}
