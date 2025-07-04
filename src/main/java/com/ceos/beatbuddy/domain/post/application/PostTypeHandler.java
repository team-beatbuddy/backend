package com.ceos.beatbuddy.domain.post.application;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.dto.PostCreateRequestDTO;
import com.ceos.beatbuddy.domain.post.dto.PostListResponseDTO;
import com.ceos.beatbuddy.domain.post.dto.UpdatePostRequestDTO;
import com.ceos.beatbuddy.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
/**
 * 게시글 타입별 처리를 담당하는 핸들러 인터페이스
 * 각 게시글 타입(Free, Piece 등)에 대한 구체적인 구현체를 제공합니다.
 */
public interface PostTypeHandler {
    /**
     * 새로운 게시글을 생성합니다.
     * @param dto 게시글 생성 요청 DTO
     * @param member 작성자
     * @param imageUrls 이미지 URL 목록
     * @return 생성된 게시글
     */
    Post createPost (PostCreateRequestDTO dto, Member member, List<String> imageUrls);
    /**
     * 게시글을 조회합니다.
     * @param postId 게시글 ID
     * @return 조회된 게시글
     */
    Post readPost(Long postId);
    /**
     * 게시글을 삭제합니다.
     * @param postId 게시글 ID
     * @param member 삭제 요청자
     * */
    void deletePost(Long postId, Member member);
    /**
     * 게시글 존재 여부를 검증하고 조회합니다.
     * @param postId 게시글 ID
     * @return 검증된 게시글
     */
    Post validateAndGetPost(Long postId);
    /**
     * 작성자 권한을 검증합니다.
     * @param post 게시글
     * @param member 검증할 회원
     */
    void validateWriter(Post post, Member member);
    /**
     * 모든 게시글을 페이지네이션으로 조회합니다.
     * @param pageable 페이지네이션 정보
     * @return 페이지네이션된 게시글 목록
     */
    Page<? extends Post> readAllPosts(Pageable pageable);
    /**
     * 해당 게시글 타입을 이 핸들러가 지원하는지 확인합니다.
     * @param post 확인할 게시글
     * @return 지원 여부
     */
    boolean supports(Post post);

    PostListResponseDTO hashTagPostList(List<String> hashtags, Pageable pageable,  Member member);

    Post updatePost(UpdatePostRequestDTO dto, Post post, Member member);

    Page<? extends Post> readAllPostsByUserExcludingAnonymous(Long userId, Pageable pageable);
}
