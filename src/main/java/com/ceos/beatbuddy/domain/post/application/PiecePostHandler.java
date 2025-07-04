package com.ceos.beatbuddy.domain.post.application;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.dto.PostCreateRequestDTO;
import com.ceos.beatbuddy.domain.post.dto.PostListResponseDTO;
import com.ceos.beatbuddy.domain.post.dto.UpdatePostRequestDTO;
import com.ceos.beatbuddy.domain.post.entity.PiecePost;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.domain.post.exception.PostErrorCode;
import com.ceos.beatbuddy.domain.post.repository.PiecePostRepository;
import com.ceos.beatbuddy.domain.venue.application.VenueInfoService;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("piece")
@RequiredArgsConstructor
public class PiecePostHandler implements PostTypeHandler {
    private final PiecePostRepository piecePostRepository;
    private final VenueInfoService venueInfoService;
    private final PieceService pieceService;

    @Override
    public boolean supports(Post post) {
        return post instanceof PiecePost;
    }

    @Override
    public PostListResponseDTO hashTagPostList(List<String> hashtags, int page, int size, Member member) {
        throw new UnsupportedOperationException("PiecePost 생성 기능이 아직 구현되지 않았습니다.");

    }

    @Override
    public Post createPost(PostCreateRequestDTO dto, Member member, List<String> imageUrls) {
        throw new UnsupportedOperationException("PiecePost 생성 기능이 아직 구현되지 않았습니다.");
    }

    @Override
    public Page<? extends Post> readAllPosts(Pageable pageable) {
        return piecePostRepository.findAll(pageable);
    }

    @Override
    public Post readPost(Long postId) {
        Post post = validateAndGetPost(postId);

        post.increaseView();
        return post;
    }

    @Override
    public void deletePost(Long postId, Member member) {
        Post post = validateAndGetPost(postId);
        validateWriter(post, member);
        piecePostRepository.deleteById(post.getId());
    }

    @Override
    public Post validateAndGetPost(Long postId) {
        return piecePostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_EXIST));
    }

    @Override
    public void validateWriter(Post post, Member member) {
        if (!post.getMember().equals(member)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }
    }

    @Override
    public Post updatePost(UpdatePostRequestDTO dto, Post post, Member member) {
        throw new UnsupportedOperationException("PiecePost 생성 기능이 아직 구현되지 않았습니다.");
    }
}
