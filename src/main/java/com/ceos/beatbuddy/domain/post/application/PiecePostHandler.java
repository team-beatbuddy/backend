package com.ceos.beatbuddy.domain.post.application;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.dto.PostCreateRequestDTO;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.PiecePost;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.domain.post.exception.PostErrorCode;
import com.ceos.beatbuddy.domain.post.repository.PiecePostRepository;
import com.ceos.beatbuddy.domain.venue.application.VenueInfoService;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component("piece")
@RequiredArgsConstructor
public class PiecePostHandler implements PostTypeHandler{
    private final PiecePostRepository piecePostRepository;
    private final VenueInfoService venueInfoService;
    private final PieceService pieceService;
    @Override
    public boolean supports(Post post) {
        return post instanceof PiecePost;
    }

    @Override
    public Post createPost(PostCreateRequestDTO dto, Member member, List<String> imageUrls) {
        return null;
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
        if (isWriter(post, member)) {
            piecePostRepository.deleteById(post.getId());
        }
    }

    public Post validateAndGetPost(Long postId) {
        return piecePostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_EXIST));
    }

    @Override
    public Boolean isWriter(Post post, Member member) {
        if (post.getMember().equals(member)) {
            throw new CustomException(PostErrorCode.MEMBER_NOT_MATCH);
        }
        return true;
    }
}
