package com.ceos.beatbuddy.domain.post.application;

import com.ceos.beatbuddy.domain.comment.repository.CommentRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.dto.*;
import com.ceos.beatbuddy.domain.post.dto.PostRequestDto.PiecePostRequestDto;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.Piece;
import com.ceos.beatbuddy.domain.post.entity.PiecePost;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.domain.post.exception.PostErrorCode;
import com.ceos.beatbuddy.domain.post.repository.*;
import com.ceos.beatbuddy.domain.scrapandlike.entity.PostInteractionId;
import com.ceos.beatbuddy.domain.scrapandlike.entity.PostLike;
import com.ceos.beatbuddy.domain.scrapandlike.entity.PostScrap;
import com.ceos.beatbuddy.domain.scrapandlike.repository.PostLikeRepository;
import com.ceos.beatbuddy.domain.scrapandlike.repository.PostScrapRepository;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.UploadUtil;
import com.ceos.beatbuddy.global.code.ErrorCode;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final FreePostRepository freePostRepository;
    private final PiecePostRepository piecePostRepository;
    private final MemberService memberService;
    private final PieceService pieceService;
    private final PostLikeRepository postLikeRepository;
    private final PostScrapRepository postScrapRepository;
    private final PostQueryRepository postQueryRepository;
    private final CommentRepository commentRepository;
    private final PostTypeHandlerFactory postTypeHandlerFactory;
    private final UploadUtil uploadUtil;
    private final PostRepository postRepository;

    private static final List<String> VALID_POST_TYPES = List.of("free", "piece");


    // 프론트 개발 완료 후 삭제 예정
    @Transactional
    public Post addPost(Long memberId, String type, PostRequestDto requestDto) {
        Member member = memberService.validateAndGetMember(memberId);
        List<String> imageUrls = uploadUtil.uploadImages(requestDto.images(), UploadUtil.BucketType.MEDIA, "post");

        return switch (type) {
            case "free" -> createFreePost(member, requestDto, imageUrls);
            case "piece" -> createPiecePost(member, requestDto, imageUrls);
            default -> throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
        };
    }

    @Transactional
    public ResponsePostDto addNewPost(String type, PostCreateRequestDTO dto, Long memberId, List<MultipartFile> images) {
        validatePostType(type);
        Member member = memberService.validateAndGetMember(memberId);
        // 이미지 s3 올리기
        List<String> imageUrls = uploadUtil.uploadImages(images, UploadUtil.BucketType.MEDIA, "post");

        // 내부에서 공장 선택
        PostTypeHandler handler = postTypeHandlerFactory.getHandler(type);
        // 선택된 공장은 모르지만, createPost 하도록 인터페이스만 제공
        Post post = handler.createPost(dto, member, imageUrls);
        
        return ResponsePostDto.of(post);
    }

    
    // 추후 삭제 예정
    public Post readPost(String type, Long postId) {
        switch (type) {
            case "free" -> {
                FreePost post = freePostRepository.findById(postId)
                        .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_EXIST));
                post.increaseView();  // 예: 조회수 증가
                return post;
            }
            case "piece" -> {
                PiecePost post = piecePostRepository.findById(postId)
                        .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_EXIST));
                post.increaseView();  // 예: 조회수 증가
                return post;
            }
            default -> throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
        }
    }

    @Transactional
    public PostPageResponseDTO newReadPost(String type, Long postId, Long memberId) {
        // 1. 게시글 조회 및 조회수 증가
        PostTypeHandler handler = postTypeHandlerFactory.getHandler(type);
        Post post = handler.readPost(postId);

        // 2. 사용자가 좋아요 / 스크랩 / 댓글 달았는지 여부
        PostInteractionId interactionId = new PostInteractionId(memberId, postId);

        boolean isLiked = postLikeRepository.existsById(interactionId);
        boolean isScrapped = postScrapRepository.existsById(interactionId);
        boolean hasCommented = commentRepository.existsByPost_IdAndMember_Id(postId, memberId);

        // 3. DTO 변환
        return PostPageResponseDTO.toDTO(post, isLiked, isScrapped, hasCommented);
    }

    // 삭제 예정
    public Page<ResponsePostDto> readAllPosts(String type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return switch (type) {
            case "free" -> freePostRepository.findAll(pageable).map(ResponsePostDto::of);
            case "piece" -> piecePostRepository.findAll(pageable).map(ResponsePostDto::of);
            default -> throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
        };
    }

    public PostListResponseDTO readAllPostsSort(Long memberId, String type, String sort, int page, int size) {
        Sort sortOption = getSortOption(sort);
        Pageable pageable = PageRequest.of(page, size, sortOption);

        Member member = memberService.validateAndGetMember(memberId);

        PostTypeHandler handler = postTypeHandlerFactory.getHandler(type);
        Page<? extends Post> postPage = handler.readAllPosts(pageable);

        List<? extends Post> posts = postPage.getContent();
        List<Long> postIds = posts.stream().map(Post::getId).toList();

        // 좋아요/스크랩/댓글 여부를 IN 쿼리로 한 번에 조회
        Set<Long> likedPostIds = getLikedPostIds(member.getId(), postIds);

        Set<Long> scrappedPostIds = getScrappedPostIds(member.getId(), postIds);

        Set<Long> commentedPostIds = getCommentedPostIds(member.getId(), postIds);


        List<PostPageResponseDTO> dtoList = posts.stream()
                .map(post -> PostPageResponseDTO.toDTO(
                        post,
                        likedPostIds.contains(post.getId()),
                        scrappedPostIds.contains(post.getId()),
                        commentedPostIds.contains(post.getId())
                ))
                .toList();

        return PostListResponseDTO.builder()
                .totalPost((int) postPage.getTotalElements())
                .page(postPage.getNumber())
                .size(postPage.getSize())
                .responseDTOS(dtoList)
                .build();
    }


    private Sort getSortOption(String sort) {
        return switch (sort) {
            case "latest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "popular" -> Sort.by(Sort.Direction.DESC, "likes");
            default -> throw new CustomException(PostErrorCode.INVALID_SORT_TYPE);
        };
    }


    public List<PostPageResponseDTO> getHotPosts(Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);
        List<Post> posts = postQueryRepository.findHotPostsWithin12Hours();

        List<Long> postIds = posts.stream()
                .map(Post::getId)
                .toList();

        Set<Long> likedPostIds = getLikedPostIds(member.getId(), postIds);

        Set<Long> scrappedPostIds = getScrappedPostIds(member.getId(), postIds);

        Set<Long> commentedPostIds = getCommentedPostIds(member.getId(), postIds);


        return posts.stream()
                .map(post -> PostPageResponseDTO.toDTO(
                        post,
                        likedPostIds.contains(post.getId()),
                        scrappedPostIds.contains(post.getId()),
                        commentedPostIds.contains(post.getId())
                ))
                .toList();
    }

    @Transactional
    public void deletePost(String type, Long postId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        Post post = readPost(type, postId);

        if (!post.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        PostTypeHandler handler = postTypeHandlerFactory.getHandler(type);
        handler.deletePost(postId, member);
    }

    // 이후 삭제할 예정
    private FreePost createFreePost(Member member, PostRequestDto requestDto, List<String> imageUrls) {
        FreePost post = FreePost.builder()
                .title(requestDto.title())
                .content(requestDto.content())
                .imageUrls(imageUrls)
                .member(member)
                .anonymous(requestDto.anonymous())
                .build();
        return freePostRepository.save(post);
    }

    // 삭제 예정
    private PiecePost createPiecePost(Member member, PostRequestDto requestDto, List<String> imageUrls) {
        Piece piece = pieceService.createPiece(member, requestDto.venueId(), (PiecePostRequestDto) requestDto);

        PiecePost post = PiecePost.builder()
                .title(requestDto.title())
                .content(requestDto.content())
                .imageUrls(imageUrls)
                .member(member)
                .anonymous(requestDto.anonymous())
                .piece(piece)
                .build();
        return piecePostRepository.save(post);
    }

    // 삭제 예정
    public Post findPostByIdWithDiscriminator(Long postId) {
        // 먼저 자유 게시글 확인
        Optional<FreePost> freePost = freePostRepository.findById(postId);
        if (freePost.isPresent()) {
            return freePost.get();
        }

        // 없다면 조각모집 게시글 확인
        Optional<PiecePost> piecePost = piecePostRepository.findById(postId);
        if (piecePost.isPresent()) {
            return piecePost.get();
        }

        throw new CustomException(PostErrorCode.POST_NOT_EXIST);
    }


    @Transactional
    public void likePost(Long postId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        Post post = findPostByIdWithDiscriminator(postId);

        PostInteractionId likeId = new PostInteractionId(memberId, post.getId());
        if (postLikeRepository.existsById(likeId)) {
            throw new CustomException(PostErrorCode.ALREADY_LIKED);
        }

        PostLike postLike = PostLike.builder()
                .post(post)
                .member(member)
                .id(likeId)
                .build();

        postLikeRepository.save(postLike);
        postRepository.increaseLike(postId);
    }


    @Transactional
    public void deletePostLike(Long postId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);


        Post post = findPostByIdWithDiscriminator(postId);

        PostInteractionId likeId = new PostInteractionId(member.getId(), post.getId());
        PostLike postLike = postLikeRepository.findById(likeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_LIKE));

        postLikeRepository.delete(postLike);
        postRepository.decreaseLike(postId);

    }

    @Transactional
    public void scrapPost(Long postId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);


        Post post = findPostByIdWithDiscriminator(postId);

        PostInteractionId scrapId = new PostInteractionId(memberId, post.getId());
        if (postScrapRepository.existsById(scrapId)) {
            throw new CustomException(PostErrorCode.ALREADY_SCRAPPED);
        }

        PostScrap postScrap = PostScrap.builder()
                .post(post)
                .member(member)
                .id(scrapId)
                .build();

        postScrapRepository.save(postScrap);
    }


    @Transactional
    public void deletePostScrap(Long postId, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);


        Post post = findPostByIdWithDiscriminator(postId);

        PostInteractionId scrapId = new PostInteractionId(member.getId(), post.getId());
        PostScrap postScrap = postScrapRepository.findById(scrapId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SCRAP));

        postScrapRepository.delete(postScrap);
    }

    @Transactional(readOnly = true)
    public PostListResponseDTO getScrappedPostsByType(Long memberId, String type, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);


        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 1. 스크랩한 게시글 페이징 조회
        Page<Post> postPage = postScrapRepository.findPostsByMemberId(member.getId(), pageable);
        List<Post> posts = postPage.getContent();

        // 2. 게시글 타입 필터링
        PostTypeHandler handler = postTypeHandlerFactory.getHandler(type);
        List<Post> filteredPosts = posts.stream()
                .filter(handler::supports)
                .toList();

        // 3. 최적화용 postIds 추출
        List<Long> postIds = filteredPosts.stream()
                .map(Post::getId)
                .toList();

        // 4. 연관 정보 bulk 조회
        Set<Long> likedPostIds = getLikedPostIds(member.getId(), postIds);

        Set<Long> commentedPostIds = getCommentedPostIds(member.getId(), postIds);

        // 5. DTO 매핑
        List<PostPageResponseDTO> dtos = filteredPosts.stream()
                .map(post -> PostPageResponseDTO.toDTO(
                        post,
                        likedPostIds.contains(post.getId()),
                        true, // 어차피 스크랩된 게시글이니까
                        commentedPostIds.contains(post.getId())
                ))
                .toList();

        return PostListResponseDTO.builder()
                .totalPost((int) postPage.getTotalElements())
                .page(postPage.getNumber())
                .size(postPage.getSize())
                .responseDTOS(dtos)
                .build();
    }



    @Transactional(readOnly = true)
    public PostListResponseDTO getMyPostsByType(Long memberId, String type, int page, int size) {
        Member member = memberService.validateAndGetMember(memberId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<? extends Post> postPage;

        PostTypeHandler handler = postTypeHandlerFactory.getHandler(type);
        postPage = handler.readAllPosts(pageable);

        List<? extends Post> posts = postPage.getContent();
        List<Long> postIds = posts.stream().map(Post::getId).toList();

        // 연관 정보 bulk 조회
        Set<Long> likedPostIds = getLikedPostIds(member.getId(), postIds);

        Set<Long> scrappedPostIds = getScrappedPostIds(member.getId(), postIds);

        Set<Long> commentedPostIds = getCommentedPostIds(member.getId(), postIds);

        // DTO 매핑
        List<PostPageResponseDTO> dtos = posts.stream()
                .map(post -> PostPageResponseDTO.toDTO(
                        post,
                        likedPostIds.contains(post.getId()),
                        scrappedPostIds.contains(post.getId()),
                        commentedPostIds.contains(post.getId())
                ))
                .toList();

        return PostListResponseDTO.builder()
                .totalPost((int) postPage.getTotalElements())
                .page(postPage.getNumber())
                .size(postPage.getSize())
                .responseDTOS(dtos)
                .build();
    }

    private Set<Long> getLikedPostIds(Long memberId, List<Long> postIds) {
        return postLikeRepository.findAllByMember_IdAndPost_IdIn(memberId, postIds)
                .stream()
                .map(pl -> pl.getPost().getId())
                .collect(Collectors.toSet());
    }

    private Set<Long> getCommentedPostIds(Long memberId, List<Long> postIds) {
        return commentRepository.findAllByMember_IdAndPost_IdIn(memberId, postIds)
                .stream()
                .map(c -> c.getPost().getId())
                .collect(Collectors.toSet());
    }

    private Set<Long> getScrappedPostIds(Long memberId, List<Long> postIds) {
        return postScrapRepository.findAllByMember_IdAndPost_IdIn(memberId, postIds)
                .stream()
                .map(ps -> ps.getPost().getId())
                .collect(Collectors.toSet());
    }

    // 주어진 타입이 올바른지 확인
    private void validatePostType(String type) {
        if (!VALID_POST_TYPES.contains(type)) {
            throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
        }
    }

    public Post validateAndGetPost(Long postId) {
        return  postRepository.findById(postId).orElseThrow(
                () -> new CustomException(PostErrorCode.POST_NOT_EXIST)
        );
    }

}