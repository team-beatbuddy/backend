package com.ceos.beatbuddy.domain.post.application;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.dto.FreePostRequestDTO;
import com.ceos.beatbuddy.domain.post.dto.PostCreateRequestDTO;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.domain.post.exception.PostErrorCode;
import com.ceos.beatbuddy.domain.post.repository.FreePostRepository;
import com.ceos.beatbuddy.domain.venue.application.VenueInfoService;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component("free")
@RequiredArgsConstructor
public class FreePostHandler implements PostTypeHandler{
    private final FreePostRepository freePostRepository;
    private final VenueInfoService venueInfoService;

    @Override
    public boolean supports(Post post) {
        return post instanceof FreePost;
    }

    @Override
    public Post createPost(PostCreateRequestDTO dto, Member member, List<String> imageUrls) {
        if (!(dto instanceof FreePostRequestDTO freeDto)) {
            throw new CustomException(PostErrorCode.INVALID_DTO_TYPE);
        }

        Venue venue = Optional.ofNullable(dto.venueId())
                .map(venueInfoService::validateAndGetVenue)
                .orElse(null);

        FreePost freePost = FreePostRequestDTO.toEntity(freeDto, imageUrls, member, venue);
        return freePostRepository.save(freePost);
    }

    @Override
    public Page<? extends Post> readAllPosts(Pageable pageable) {
        return freePostRepository.findAll(pageable);
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
        freePostRepository.deleteById(post.getId());
    }

    public Post validateAndGetPost(Long postId) {
        return freePostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_EXIST));
    }

    @Override
    public void validateWriter(Post post, Member member) {
        if (!post.getMember().equals(member)) {
            throw new CustomException(PostErrorCode.MEMBER_NOT_MATCH);
        }
    }

}
