package com.ceos.beatbuddy.domain.post.application;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.dto.PostCreateRequestDTO;
import com.ceos.beatbuddy.domain.post.dto.UpdatePostRequestDTO;
import com.ceos.beatbuddy.domain.post.entity.FixedHashtag;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.domain.post.exception.PostErrorCode;
import com.ceos.beatbuddy.domain.post.repository.FreePostRepository;
import com.ceos.beatbuddy.domain.venue.application.VenueInfoService;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component("free")
@RequiredArgsConstructor
public class FreePostHandler implements PostTypeHandler{
    private final FreePostRepository freePostRepository;
    private final VenueInfoService venueInfoService;
    private final FreePostSearchService freePostSearchService;

    @Override
    public boolean supports(Post post) {
        return post instanceof FreePost;
    }

    @Override
    @Transactional
    public Post createPost(PostCreateRequestDTO dto, Member member, List<String> imageUrls) {
        Venue venue = Optional.ofNullable(dto.getVenueId())
                .map(venueInfoService::validateAndGetVenue)
                .orElse(null);


        List<FixedHashtag> hashtags = validateAndGetHashtags(dto.getHashtags());

        FreePost freePost = PostCreateRequestDTO.toEntity(dto, imageUrls, member, venue, hashtags);

        freePost = freePostRepository.save(freePost);
        freePostSearchService.save(freePost); // 게시글 생성 시 검색 인덱스에 저장
        return freePost;
    }

    @Override
    public Page<? extends Post> readAllPosts(Pageable pageable) {
        return freePostRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Post readPost(Long postId) {
        Post post = validateAndGetPost(postId);

        post.increaseView();
        return post;
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Member member) {
        Post post = validateAndGetPost(postId);
        validateWriter(post, member);
        freePostRepository.deleteById(post.getId());
        freePostSearchService.delete(postId); // 게시글 삭제 시 검색 인덱스에서 제거
    }

    @Override
    public Post validateAndGetPost(Long postId) {
        return freePostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_EXIST));
    }

    @Override
    public void validateWriter(Post post, Member member) {
        if (!post.getMember().equals(member)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }
    }



    /**
     * 업데이트 요청 DTO로부터 제목과 내용을 가져와, null이 아니고 공백이 아닌 경우에만 게시글의 제목과 내용을 수정합니다.
     *
     * DTO의 각 필드 값이 null이 아니고 앞뒤 공백을 제외했을 때 비어 있지 않은 경우에만 해당 필드를 업데이트합니다.
     */
    protected void updateCommonFields(UpdatePostRequestDTO dto, Post post) {
        if ((dto.getTitle() != null) && (!dto.getTitle().trim().isEmpty())) {
            post.updateTitle(dto.getTitle());
        }
        if ((dto.getContent() != null) && (!dto.getContent().trim().isEmpty())) {
            post.updateContent(dto.getContent());
        }
    }

    @Override
    @Transactional
    public Post updatePost(UpdatePostRequestDTO dto, Post post, Member member) {
        updateCommonFields(dto, post);
        return post;
    }

    public List<FixedHashtag> validateAndGetHashtags(List<String> hashtags) {
        if (hashtags == null || hashtags.isEmpty()) {
            return List.of();
        }

        // 중복 체크
        long distinctCount = hashtags.stream().distinct().count();
        if (distinctCount != hashtags.size()) {
            throw new CustomException(PostErrorCode.DUPLICATE_HASHTAG_NOT_ALLOWED);
        }

        return hashtags.stream()
                .map(FixedHashtag::fromDisplayName)
                .toList();
    }

}
